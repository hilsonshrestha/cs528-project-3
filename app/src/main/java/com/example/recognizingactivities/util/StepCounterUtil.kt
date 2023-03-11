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

    private val smoothingWindowSize = 20;
    private val rawAccValues = FloatArray(3)

    private var mGraph1LastXValue = 0.0
    private var mGraph2LastXValue = 0.0

    private val accValueHistory = Array(3) {
        FloatArray(
            smoothingWindowSize
        )
    }
    private val runningAccTotal = FloatArray(3)
    private val currentAccAvg = FloatArray(3)
    private var currentReadIndex = 0

    private var stepCounter = 0f

    private var seriesOne: LineGraphSeries<DataPoint>? = null
    private var seriesTwo: LineGraphSeries<DataPoint>? = null

    private var avgMagnitude = 0.0
    private var netMagnitude = 0.0

    private var lastXPoint = 1.0
    var stepThreshold = 1.0
    var noiseThreshold = 2.0
    private val windowSize = 10

    init{

        seriesOne = LineGraphSeries()
        seriesTwo = LineGraphSeries()
    }

    fun detectSteps(event: SensorEvent?): Int {
        if (event != null) {
            rawAccValues[0] = event.values[0];
            rawAccValues[1] = event.values[1];
            rawAccValues[2] = event.values[2];
            var lastMag : Double =
                kotlin.math.sqrt(((rawAccValues[0] * rawAccValues[0] + rawAccValues[1] * rawAccValues[1] + rawAccValues[2] * rawAccValues[2]).toDouble()))

            for (i in 0..2) {
                runningAccTotal[i] = runningAccTotal[i] - accValueHistory[i][currentReadIndex]
                accValueHistory[i][currentReadIndex] = rawAccValues[i]
                runningAccTotal[i] = runningAccTotal[i] + accValueHistory[i][currentReadIndex]
                currentAccAvg[i] = runningAccTotal[i] / smoothingWindowSize
            }

            currentReadIndex++;
            if(currentReadIndex >= smoothingWindowSize){
                currentReadIndex = 0;
            }

            avgMagnitude =
                kotlin.math.sqrt((currentAccAvg[0] * currentAccAvg[0] + currentAccAvg[0] * currentAccAvg[0] + currentAccAvg[0] * currentAccAvg[0]).toDouble());

            netMagnitude = lastMag - avgMagnitude;

            mGraph1LastXValue += 1.0
            seriesOne?.appendData(DataPoint(mGraph1LastXValue, lastMag), true, 60)

            mGraph2LastXValue += 1.0
            seriesTwo?.appendData(DataPoint(mGraph2LastXValue, netMagnitude), true, 60)

        }

        peakDetection();

        return stepCounter.toInt();

    }

    private fun peakDetection(){
        val highestValX = seriesTwo?.highestValueX;

        if (highestValX != null) {
            if(highestValX - lastXPoint < windowSize){
                return;
            }
        }

        val valuesInWindow = highestValX?.let {
            seriesTwo!!.getValues(
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
                downwardSlope = dataPointList[i].y - dataPointList[i - 1].y
                if (forwardSlope < 0 && downwardSlope > 0 && dataPointList[i].y > stepThreshold && dataPointList[i].y < noiseThreshold) {
                    stepCounter += 1
                }
            }
        }
    }

}