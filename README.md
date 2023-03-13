# cs528-project-3
This project demonstrates usage of Android Sensor programming (for step counting), Google Activity Recognition API, Geofences, Audio Player, and Google Android Maps.

## Members
| Username| Lastname| Firstname|
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


## Special instructions to run our submission: 
1. Build the project from the `\src\` and `\hw3.apk` provided
2. Run the app on your mobile device.
3. When the app is launched, if permissions to use Activity Recognition and Location are not granted, the app will prompt the user to grant those access. When the access is successfully granted,
the app will start detecting user's location and activities.

*Note: `STILL`, `WALKING`, `IN-VEHICLE` activities can be detected albeit in a slightly delayed manner by simply sitting still, walking, and in-vehicle, respectively.
However, it is tricky to trigger a `RUNNING` detection by just running. Therefore, you can simulate that by moving the phone quickly as if you are drawing the infinity symbol in the air*

*Note: Total number of steps get updated after some time.*

## Phone tested on:
1. OnePlus Nord N10 5G - android version 11
2. OnePlus 7 Pro - android version 12

## Computer tested on:
1. Asus ZenBook AMD Ryzen 7 5700U with Radeon Graphics, CPU 1.80 GHz, 8GB RAM
2. Lenovo Legion 5 Pro AMD Ryzen 5800 with Radeon Graphics, CPU 3.20 GHz, 16GB RAM
3. Lenovo Thinkpad X1 Carbon 5th Gen, CPU 2.6 GHz, 16GB RAM
