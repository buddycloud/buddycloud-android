#buddycloud-android 

[![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.buddycloud)

The buddycloud app demonstrate the rich set of features provided by the [Buddycloud REST API] (http://buddycloud.com/api "buddycloud REST API").

![screenshots](https://raw.githubusercontent.com/buddycloud/buddycloud-android/master/screenshots.png)

## Features

* Create and read channels 
* Follow/Unfollow channels
* Post and Share topics on different channels
* Search by content and metadata
* Channels discovery and summary synchronization
* Recommendation and Similar channels
* Media Sharing through 'share-intent' and post topics 
* Push notifications
* Account Management
* Friends Finder (Facebook and addressbook)
* and much more...
 
## FAQ

### General

#### How Buddycloud works?

Start developing your communication layer for your mobile app or for your website with Buddycloud. You can find more about how buddycloud works [here] (http://buddycloud.com/documentation#how_buddycloud_works_ "How Buddycloud Works").


### Development

#### How do I build Buddycloud android app?

Make sure to have ANDROID_HOME point to your Android SDK

	git clone https://github.com/buddycloud/buddycloud-android
	cd buddycloud-android

The buddycloud sources are compaitable with both Eclipse and Android studio. Using anyone of them is completely a matter of software developer choice. For Android studio, the process is straight away....just open the project and build with gradle which will download all the external libraries as well.

	./gradlew build

For Eclipse, you need to first download the list of external libraries (see below) and import in the project.

#### Which external libraries used in the android app? 

Besides the Android SDK and the support library, we use opensource third-party libraries:

* [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock) to implement the action bar design pattern; 
* [SligingMenu](https://github.com/jfeinstein10/SlidingMenu) to implement the side menu;
* [UniversalImageLoader](https://github.com/nostra13/Android-Universal-Image-Loader) for displaying and transforming images;
* [dnsjava](http://www.xbill.org/dnsjava/) for DNS queries;
* [GooglePlayServices](https://developer.android.com/google/play-services/setup.html) setup google play services SDK for GCM;
* [fab](https://github.com/shell-software/fab) for material design floating action button in older android devices; and
* [image-chooser-library](https://github.com/coomar2841/image-chooser-library) for selecting images/videos from gallery/photos/camera.

#### How do I install Buddycloud android app?

If you want to use the app as end user then download it from google playstore. However, if you're software developer and want to install the app then checkout the sources from Github and use gradle to build (show above) and install the .apk file.

	gradle installDebug

#### How do I debug Buddycloud android app?

You can debug the buddycloud android app TCP and HTTPS session usage:

	sudo tcptrack -i eth0 port 443

or, want to find out some information related to UI width adb (android debug bridge):

    adb -d logcat -v time -s buddycloud
    
#### Roadmap

We use GitHub milestones and issues to track our progress, so please check our [milestones](https://github.com/buddycloud/buddycloud-android/milestones) page for release planning. 

#### Reporting Bugs

Please report it to our [issue tracker][issues]. If your app crashes please
provide a stack trace. If you are experiencing misbehaviour please provide
detailed steps to reproduce. Always mention whether you are running the latest
Play Store version or the current HEAD.

[issues]: https://github.com/buddycloud/buddycloud-android/issues
