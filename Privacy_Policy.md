## Juice Me: Privacy policy

Welcome to the Juice Me app for Android!

This is an open source Android app developed by Happiness Munatsi Ncube. The source code is available on [GitHub](https://github.com/hmncube/JuiceMe).

This application will not send your data to any server, all your data is stored on your **device only**, and can be simply erased by clearing the app's data or uninstalling it.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file:

https://github.com/hmncube/JuiceMe/blob/bddb1fac5f1e0e55c338e147b6d34beaffc3562f/app/src/main/AndroidManifest.xml#L6-L9

<br/>

**You, as the user, or the system, can revoke this permission at any time from Settings.**
<br/>
**The application will continue working in a limited capacity without one or all of the permissions.**

| Permission | Why it is required |
| :---: | --- |
| `android.permission.CAMERA` | This is required to take the image of the recharge card. Without this permission the user has to use another application to take the image of the card |
| `android.permission.WRITE_EXTERNAL_STORAGE` | Permission needed to store the recharge card image in the device if the user wants to|
| `android.permission.CALL_PHONE` | Permission needed to dial the recharge card number |
| `android.permission.ACCESS_NETWORK_STATE` | Permission used to check the network provider in use |
| `android.permission.INTERNET` | Permission used to download additional ML data if need be | 
| `android.permission.READ_EXTERNAL_STORAGE` | Permission used to read stored images from the device |

 <hr style="border:1px solid green">

If you find any security vulnerability or have any question regarding how the app protectes your privacy, please send me an email or open an issue on [GitHub](https://github.com/hmncube/JuiceMe/issues/new), and I will surely try to fix it/help you.

Yours sincerely,  <br/>
Happiness Munatsi Ncube <br/>
Domboshawa, Zimbabwe <br/>
happinessmncube@gmail.com
