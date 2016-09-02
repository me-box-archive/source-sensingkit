# Databox Mobile Source

An Android app that enables your phone to stream sensor data to a Databox.

NB: All available sensors will be enabled and can stream to your Databox on demand at maximum fidelity. This app is still a WIP, and a newer version will have a UI that gives you more control.

This repo is an Android Studio project, and should work out of the box. A test build is available [here](https://dl.dropboxusercontent.com/u/704818/Temp/databox-source-mobile-debug.apk).

## API Endpoints

This app's intended use is together with its Databox driver. The app will instruct you how to configure your Databox driver.

If your phone is not on the same LAN/VPN as your Databox, or otherwise behind a NAT, forward port 8080 and use your external IP for testing.

For debug purposes, you can query the endpoints manually on port 8080 of your phone (the app will tell you the IP).

These are:

### /

#### Response

  - A list of all available sensors

### /:sensor

#### URL Parameters

##### sensor

Type: string

A lowercase, dash-separated [SensingKit sensor](https://github.com/SensingKit/SensingKit-Android/tree/develop#supported-sensors). One of:

  - accelerometer
  - gravity
  - linear-acceleration
  - gyroscope
  - rotation
  - magnetometer
  - ambient-temperature
  - step-detector
  - step-counter
  - light
  - location
  - motion-activity
  - battery
  - screen-status
  - microphone
  - audio-level
  - bluetooth
  - eddystone
  - ibeacon
  - humidity
  - air-pressure

#### Response

##### Success

  - A CSV stream of timestamped data (formatted based on sensor type as defined by SensingKit). Note that the connection is persistent and will not close unless it's closed client-side. Data will continuously stream in response as long as the connection remains open.

##### Error

  - 404: Sensor Not Found
