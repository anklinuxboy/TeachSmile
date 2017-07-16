# TeachSmile

The app teaches you how to smile. :) There are three emotion settings which you can use - Joy, Surprise and Angry. The 
take photo Floating action button will turn green when the emotion detector thinks that your face is displaying the correct emotion.

### Google Play Services Used
1. Analytics in CameraActivity
2. Location in LoginActivity and shown in Side Navigation Drawer in CameraActivity

### External Libraries Used
1. [Facebook SDK](https://developers.facebook.com/docs/android/)
2. [Affectiva SDK](https://developer.affectiva.com/)

All Widget files are in **Widget** package. The app uses a local database to store the image name, the emotion setting and the full 
path for where it's stored on the device. The app implements a Content Provider to query and insert data. All the Content Provider 
files are in **data** package

### Note:
The app has been tested in RTL Layout mode and works fine. All content description for buttons has been provided for accessibility.
The app theme extends AppCompat and uses Gradle to manage builds. 

#### Issues:
The Anger emotion detector doesn't always work as expected. Not for my face anyways. Maybe it'll work for you ;)
