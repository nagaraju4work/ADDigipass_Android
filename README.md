# Description

This sample demonstrates how to integrate **Mobile Orchestration SDK** on an Android device  
so as to interact with **VASCO Adaptive Authentication Service**.  

The provided features are :  

1. Activation
2. Remote Authentication with user authentication methods :
  
  - No Protection  
  - Password Protection  
  - Fingerprint Protection  
  - Face Recognition 

3. User's face enrollment for Face Recognition
4. Change Password  


# Prerequisites

Before running, you need to customize Constants.java :    

1. Move to directory `app/src/main/java/com/vasco/orchestration/sample/`   
 and open `Constants.java`  

2. Replace the values below :  

| Key                                  | Value  
|:------------------------------------ |:---------------:  
| `ACCOUNT_IDENTIFIER`                 | Identifier of your VASCO Developer Account as defined during your account creation on the [VASCO Developer Portal](https://devportal.tid.vasco.cloud).
| `SALT_STORAGE` & `SALT_DIGIPASS`     | Two different random strings of 64 hexadecimal characters  
| `ANDROID_PROJECT_NUMBER`             | The project number of your Android project, retrieved from the Google API Console

- The Minimum SDK Version is 16  

# Build and run  

## In Android Studio  

It can be directly build and run with **Android Studio IDE**   

## In your Terminal   

- Set your variable environment `ANDROID_HOME`
- Type `./gradlew assembleDebug`   

> **Note:** In case of "Permission denied" exception, use **chmod 755 gradlew**

- Move to directory `app/build/outputs/apk/`
- Plug an Android device to your computer   
- Verify that your device is attached with `adb devices` 
- Type `adb install app-debug.apk` 


# How to use  

## Registration  
- Click on button `START REGISTRATION`  
- Specify the following parameters retrieved by calling the User Registration service of **VASCO Adaptive Authentication Service** at [https://sample.tid.vasco.cloud](https://sample.tid.vasco.cloud):
 - User Identifier
 - Activation Password  

To be able to receive push notifications, you must register your application at [https://devportal.tid.vasco.cloud/Board](https://devportal.tid.vasco.cloud/Board)  
(click `Register my app`).

## Remote Authentication
**Prerequisite**:   
Register a device with push notfication properly registered.  

**Process**:  

- On the sample web site, click on the button `LOGIN`  
- Your device will receive a notification. Open it.  
- The Login Activity is displayed with 2 accept / reject buttons. 
  - `Accept` : 
    
     1. The user authentication method pushed by **VASCO Adaptive Authentication Service** is applied. 
     2. Upon Success, a validation command is generated and sent to the server for validation. 
     3. A success message is displayed on your device and the user is automatically logged on the web site.    
  - `Reject` :   
    In this integration, the process is locally cancelled.  


# Note 

In order to enable fingerprint recognition on Samsung devices  
older than Android 6.0 please make sure to add the [Pass SDK](http://developer.samsung.com/galaxy/pass) as a dependency.  




