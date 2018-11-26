# NetworkDeviceMonitor
An Android application for tracking devices on the same network using pings. The app is made during my internship for request of my superior.

## Notes
Application is made for a specific device (720 x 1280). Some things may not appear properly with resolutions below that.
You must have a wi-fi connection. You can't use this app otherwise.

## How To Use
##### 1. If device is connected, device's color turns green.

   ![1](https://user-images.githubusercontent.com/14074488/49039585-44d3f300-f1d1-11e8-9ad7-7f08bf85d29a.jpg)

#### 2. After unsuccessful 10 pings, connection device will be considered lost and color will turn red. A new image will appear that indicates device's disconnection.

   ![7](https://user-images.githubusercontent.com/14074488/49039592-46052000-f1d1-11e8-9617-73df8b384308.jpg)

#### 3. If you click on that image, Wake-On-Lan request can be send.

   ![9](https://user-images.githubusercontent.com/14074488/49039594-469db680-f1d1-11e8-956f-e1e22ce0d1bb.jpg)

#### 4. Interface to add devices

   ![3](https://user-images.githubusercontent.com/14074488/49039588-456c8980-f1d1-11e8-8a00-8e943ad91326.jpg)

#### 5. After clicking a row

   ![5](https://user-images.githubusercontent.com/14074488/49039590-456c8980-f1d1-11e8-8f94-8a6eec5b04d4.jpg)

#### 6. If a device is not connected on the same network

   ![8](https://user-images.githubusercontent.com/14074488/49039593-469db680-f1d1-11e8-9c12-d860dc0154d4.jpg)

## Known Issues
* Application crashes sometimes, reason is unknown yet.
* Last ping value won't show up from time to time.

## 3RD Party Libraries Used
* [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* [Android Network Tools](https://github.com/stealthcopter/AndroidNetworkTools)
