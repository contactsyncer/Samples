# UserAuthentication
Make sure you're following this guide step by step:-

<b>Prerequisite:-</b>

1) If you haven't added a Firebase project, You have to add a project on Firebase and register PhoneNumber authentication in that project. Follow below steps to add:-  
a) go to https://console.firebase.google.com/u/0/ and click on <b>add project</b>.  
b) Enter project name and select your region, then click <b>create project</b>.  
c) Now in your project dashboard, click on Authentication. Then on right side, click on <b>"Sign-in method"</b> and enable the "Phone" authentication. Initially it should show as "Disabled".  
d) Click on <b>Save</b>.  

2) If you've not requested your ContactSyncer <b>API key and Secret key</b>, please request one from here:-  

<b>Integrating Code:-</b>
1) Git clone or download the sample  
2) Change package name as per your requirement.  
3) Add this package to your firebase project which you have created above. To know how to add, follow below steps:-  
a) Click on <b>"setting"</b> icon on right side of your Firebase project overview section and then click on <b>project settings</b>.  
b) On bottom, click <b>"Add Firebase to your Android Project"</b>  
c) Add package name of your app as per step #2 and make sure you type in your SHA 1. To get SHA1, follow below steps:-  
MAC - open terminal and type:  
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android  
Windows - https://stackoverflow.com/questions/30070264/get-sha1-fingerprint-certificate-in-android-studio-for-google-maps  
d) Click <b>"Register"</b> app and then download and copy google-services.json file in your project folder as shown.  
e) You can skip next step(Add Firebase SDK) as this is already added in this github sample. Click on <b>finish</b>.  

4) Copy your secret key inside Utils.java file and simply run the sample.  


