package com.example.recognizingactivities.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.lang.Math.sqrt

class StepCounterUtil(private val context: Context) {

    private val SMOOTHING_WINDOW_SIZE = 20;
    private val mRawAccelValues = FloatArray(3)

    private var mGraph1LastXValue = 0.0
    private var mGraph2LastXValue = 0.0

    private val mAccelValueHistory = Array(3) {
        FloatArray(
            SMOOTHING_WINDOW_SIZE
        )
    }
    private val mRunningAccelTotal = FloatArray(3)
    private val mCurAccelAvg = FloatArray(3)
    private var mCurReadIndex = 0

    private var mStepCounter = 0f

    private var mSeries1: LineGraphSeries<DataPoint>? = null
    private var mSeries2: LineGraphSeries<DataPoint>? = null

    //private val lastMag = 0.0
    private var avgMag = 0.0
    private var netMag = 0.0

    private var lastXPoint = 1.0
    var stepThreshold = 1.0
    var noiseThreshold = 2.0
    private val windowSize = 10

    init{

        Log.i("Step", "Step counter started")
        //sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        //val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

        mSeries1 = LineGraphSeries()
        mSeries2 = LineGraphSeries()

        //val graph : GraphView? = null;
        //mSeries1 = LineGraphSeries()
        /*if (graph != null) {
            graph.addSeries(mSeries1)
        }
        //graph.title = "Accelerator Signal"
        //graph.gridLabelRenderer.verticalAxisTitle = "Signal Value"
        //graph.viewport.isXAxisBoundsManual = true
        if (graph != null) {
            graph.viewport.setMinX(0.0)
        }
        if (graph != null) {
            graph.viewport.setMaxX(60.0)
        }*/

        //val graph2 : GraphView? = null;
        //mSeries2 = LineGraphSeries()
        //graph2.title = "Smoothed Signal"
        /*if (graph2 != null) {
            graph2.addSeries(mSeries2)
        }
        //graph2.gridLabelRenderer.verticalAxisTitle = "Signal Value"
        //graph2.viewport.isXAxisBoundsManual = true
        if (graph2 != null) {
            graph2.viewport.setMinX(0.0)
        }
        if (graph2 != null) {
            graph2.viewport.setMaxX(60.0)
        }*/
    }

    fun detectSteps(event: SensorEvent?): Int {
        if (event != null) {
            mRawAccelValues[0] = event.values[0];
            mRawAccelValues[1] = event.values[1];
            mRawAccelValues[2] = event.values[2];

            /*Log.i("mRawAccelValues[0]: ", mRawAccelValues[0].toString());
            Log.i("mRawAccelValues[1]: ", mRawAccelValues[1].toString());
            Log.i("mRawAccelValues[2]: ", mRawAccelValues[2].toString());
*/
            var lastMag : Double =
                Math.sqrt(((mRawAccelValues[0] * mRawAccelValues[0] + mRawAccelValues[1] * mRawAccelValues[1] + mRawAccelValues[2] * mRawAccelValues[2]).toDouble()))
            //Log.i("lastMag", lastMag.toString());

            for (i in 0..2) {
                mRunningAccelTotal[i] = mRunningAccelTotal[i] - mAccelValueHistory[i][mCurReadIndex]
                mAccelValueHistory[i][mCurReadIndex] = mRawAccelValues[i]
                mRunningAccelTotal[i] = mRunningAccelTotal[i] + mAccelValueHistory[i][mCurReadIndex]
                mCurAccelAvg[i] = mRunningAccelTotal[i] / SMOOTHING_WINDOW_SIZE
            }

            mCurReadIndex++;
            if(mCurReadIndex >= SMOOTHING_WINDOW_SIZE){
                mCurReadIndex = 0;
            }

            avgMag =
                Math.sqrt((mCurAccelAvg[0] * mCurAccelAvg[0] + mCurAccelAvg[0] * mCurAccelAvg[0] + mCurAccelAvg[0] * mCurAccelAvg[0]).toDouble());

            netMag = lastMag - avgMag;

            mGraph1LastXValue += 1.0
            mSeries1?.appendData(DataPoint(mGraph1LastXValue, lastMag), true, 60)

            mGraph2LastXValue += 1.0
            mSeries2?.appendData(DataPoint(mGraph2LastXValue, netMag), true, 60)

        }

        peakDetection();

        //return mStepCounter.toInt();
        Log.i("mStepCounter", mStepCounter.toString())

        return mStepCounter.toInt();

    }

    private fun peakDetection(){
        val highestValX = mSeries2?.highestValueX;

        if (highestValX != null) {
            if(highestValX - lastXPoint < windowSize){
                return;
            }
        }

        val valuesInWindow = highestValX?.let {
            mSeries2!!.getValues(
                lastXPoint,
                it
            )
        }

        if (highestValX != null) {
            lastXPoint = highestValX
        };

        var forwardSlope = 0.0
        var downwardSlope = 0.0

        val dataPointList: MutableList<DataPoint> = ArrayList()
        valuesInWindow!!.forEachRemaining { e: DataPoint ->
            dataPointList.add(
                e
            )
        }

        for (i in dataPointList.indices) {
            if (i == 0) continue else if (i < dataPointList.size - 1) {
                forwardSlope = dataPointList[i + 1].y - dataPointList[i].y
                //Log.i("dataPointList[i].y < noiseThreshold", (dataPointList[i].y < noiseThreshold).toString())
                downwardSlope = dataPointList[i].y - dataPointList[i - 1].y
                if (forwardSlope < 0 && downwardSlope > 0 && dataPointList[i].y > stepThreshold && dataPointList[i].y < noiseThreshold) {
                    mStepCounter += 1
                }
            }
        }
    }

}