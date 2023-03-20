# JuiceMe

![ic_launcher-web](https://user-images.githubusercontent.com/2725300/211160536-90f13e53-78e6-4245-9978-33f5313bb04c.png)

Android app using computer vision to topup airtime


### To download
[![google_play_badge](https://user-images.githubusercontent.com/2725300/226333269-5d452594-a3b0-4fe8-b305-cf75b6ed75ec.png)](https://play.google.com/store/apps/details?id=com.hmncube.juiceme)

Head over to the release section and select the APK depending on your phones architecture. 
If you are not sure about the architecture or if it is not on the list download the `JuiceMe-universal-release` apk

~~Currently workes on Econet airtime~~ Now supports all networks in Zimbabwe

Minimum android verion Android 5.0

~~The application uses Google ML Kit for Text Recognition and so will require an internet connection at the beginning.~~ App now completely works offline

### Video Demo


https://user-images.githubusercontent.com/2725300/211815032-b7eb0a00-3ba4-4e37-a405-d430b0c21f56.mp4


### Features
- Extract airtime voucher number from the card
- Stores the number
- Can retry the number later on 
- Can automatically topup the airtime if the a valid number is found
- Automatticaly detect network being used and select the appropriate USSD
- User can manually set the network
- User can set custom USSD code
- User can set the length of the recharge card

### TODO
- [X] Add support for another networks
- [X] Code clean up
- [X] Handle dual sims (Best fix while supporting Android 5.0)
- [X] Make it generic
- [ ] Add support for Shona, Ndebele and other local languages
- [ ] Add tests
- [ ] Use Jetpack Compose
- [ ] Batch number extraction
- [ ] ~~Add support for < Android 5.0~~ Adding this will make the app big > 25 mb, still weighing wheather to impliment

### App Icon <a href="https://www.flaticon.com/free-icons/financial-plan" title="financial plan icons">Financial plan icons created by Afian Rochmah Afif - Flaticon</a>
