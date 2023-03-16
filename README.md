# cs528-project-3
This project demonstrates usage of Android Sensor programming (for step counting), Google Activity Recognition API, Geofences, Audio Player, and Google Android Maps.

## Members
|Username|Lastname|Firstname|
|-------------|-------------|-----------|
|AmeyMore| Amey | More|
|MinhHangwpi| Radetsky| Minh-Hang|
|hilsonshrestha| Shrestha| Hilson|
|jpayvazian| Ayvazian| Jack|

## This project referenced code from: 
1. https://developers.google.com/location-context/activity-recognition
2. https://github.com/googlesamples/easypermissions
3. https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/?fbclid=IwAR3Zg9FUGT88eQP874p2xJzOmlNA4STa7crYW4LvoXTjQ8-2DhyGIbPyz8Q
4. https://github.com/jonfroehlich/CSE590Sp2018/tree/master/Assignments/A01-StepTracker
5. https://developer.android.com/codelabs/activity-recognition-transition#0
6. https://developer.android.com/training/location/geofencing
7. https://developer.android.com/codelabs/advanced-android-kotlin-training-geofencing


## Special instructions to run our submission: 
1. Install the app on your mobile device using the `\hw3.apk` provided
2. When the app is launched, if permissions to use Activity Recognition and Location are not granted, the app will prompt the user to grant those access. When the access is successfully granted, the app will start detecting user's location and activities. For Location permission in particular, be sure to set "allows access all the time" instead of "allows access only while in app" (as shown in the demo app).

*Note: `STILL`, `WALKING`, `IN-VEHICLE` activities can be detected albeit in a slightly delayed manner by simply sitting still, walking, and in-vehicle, respectively.
However, it is tricky to trigger a `RUNNING` detection by just running. Therefore, you can simulate that by moving the phone quickly as if you are drawing many infinity symbols in the air*

*Note: The step count may take a moment to update, and the algorithm may not register light walking. To ensure accurate step counting, take larger strides or swing your arms more while walking*

## Phone tested on:
1. OnePlus Nord N10 5G - android version 11
2. OnePlus 7 Pro - android version 12
3. Pixel 7 - android version 13

## Computer tested on:
1. Asus ZenBook AMD Ryzen 7 5700U with Radeon Graphics, CPU 1.80 GHz, 8GB RAM
2. Lenovo Legion 5 Pro AMD Ryzen 5800 with Radeon Graphics, CPU 3.20 GHz, 16GB RAM
3. Lenovo Thinkpad X1 Carbon 5th Gen, CPU 2.6 GHz, 16GB RAM
4. Lenovo Thinkpad T490, i7 7th Gen, 16GB RAM
