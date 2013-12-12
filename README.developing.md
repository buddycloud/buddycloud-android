Some notes on the design decisions and architecture of the Android app.

What does it do
----------

Catch up with your buddycloud channels using your Android device.

Description
----------

The Android client implements most of the features provided by the [buddycloud API] (http://buddycloud.org/w/index.php?title=Buddycloud_HTTP_API "buddycloud HTTP API"): create and read channels' content; create, delete and follow channels; search by content and metadata; recommentation and similarity features; channel summary synchronization; push notifications using GCM; API discovery via DNS TXT records; and much more. 

Technologies
----------

Besides the Android SDK and the support library, we use:

* [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock) to implement the action bar design pattern; 
* [SligingMenu](https://github.com/jfeinstein10/SlidingMenu) to implement the side menu;
* [picasso](https://github.com/square/picasso) for displaying and transforming images; and
* [dnsjava](http://www.xbill.org/dnsjava/) for DNS queries.

Security model
----------

As we use HTTPS as default, we expect API server certificates to be valid so that we can start talking to them. If the API certificate is not valid, we prompt the user for confirmation, so that he becomes aware of unsafe connections. If the user agrees to do so (and ask the app to remember this decision), we store it as a preference, and we skip SSL for this server.

Architecture
----------

### Activity flow

```
MainActivity --if not logged------> LoginActivity
             ---------------------> GenericChannelActity --pick a channel--> MainActivity
             ---------------------> SettingsActivity     --save settings---> MainActivity
             ---------------------> SearchActivity       --pick a channel--> MainActivity
             ---------------------> ChannelDetailsActivity  --save or back-> MainActivity
MainActivity --back-menu-visible--> exit

LoginActivity --login-------------> MainActivity
              --not registered----> CreateAccountActivity ------> MainActivity
              --back--------------> exit

SettingsActivity -----------------> ChangePasswordActivity  --save or back-> SettingsActivity
                 -----------------> AboutBuddycloudActivity --save or back-> SettingsActivity
             
ShareActivity --post created------> exit
```

### Fragments inside MainActivity

```
┌-----------┬-------------------┐
│           │                   │
│           │                   │
│           │                   │
│Subscribed │     Channel       │
│ Channels  │     Stream        │
│ Fragment  │     Fragment      │
│           │                   │
│           │                   │
│           │                   │
└-----------┴-------------------┘
```

### Flow of the data

#### Adapters 

An Adapter object acts as a bridge between a View and the underlying data for that view. Adapters provide access to  data items and are also responsible for making a View for each item in the data set. In our app, adapters also work as memory caches, since they fetch and store data items from the SQLite DB. In most of the cases, adapters are also responsible for the sorting of data items. Adapter classes can be found in the package [com.buddycloud.fragments.adapter]{https://github.com/buddycloud/buddycloud-android/tree/master/src/com/buddycloud/fragments/adapter}.

#### Models

Models are the glue between adapters and the SQLite+HTTP layer. Models decide when to use the SQLite cache or when to hit the API. They are aware of the API endpoints and the DB helpers. Model classes can be found in the package [com.buddycloud.model]{https://github.com/buddycloud/buddycloud-android/tree/master/src/com/buddycloud/model}.

#### SQLite database + DAO

The SQLite cache makes the app feels faster, since we almost always have something to show before hitting the API. We have tables for channel metadata, posts, subscribed channels, threads' ids and unread counters. DB helper classes can be found in the package [com.buddycloud.model.db]{https://github.com/buddycloud/buddycloud-android/tree/master/src/com/buddycloud/model/db}.
In order to access data stored in the SQLite cache, we use DAO classes. Those classes assemble SQL queries to read and update the database, thus, there is a corresponding DAO for each SQLite table. DAO classes can be found in the package [com.buddycloud.model.dao]{https://github.com/buddycloud/buddycloud-android/tree/master/src/com/buddycloud/model/dao}.

#### HTTP helpers

Buddycloud android clients talks to the Buddycloud HTTP API. In that sense, the lower level of this app lives in the [BuddycloudHTTPHelper]{https://github.com/buddycloud/buddycloud-android/blob/master/src/com/buddycloud/http/BuddycloudHTTPHelper.java}. We use two Executors for HTTP requests, one for low priority requests and another for high ones. The low priority pool is expected to exhaust sooner, since it runs longer requests, while the high priority one is meant for quick requests updating the UI.


### Pusher + GCM
### Sync
### GenericChannelActivity
### Backstack
### PendingPosts

How to get started
----------

### Building

After checking out the code, you need to download both [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock) and [SligingMenu](https://github.com/jfeinstein10/SlidingMenu) and reference them as libraries in your project. Notice that 
SlidingMenu needs a small [hack]{https://github.com/jfeinstein10/SlidingMenu/blob/master/README.md#setup-with-actionbarsherlock} to work together with ActionBarSherlock.

Then, build: http://developer.android.com/tools/building/index.html. Life is easier if you are using Eclipse.

### Entry points

The main entry points of the app are the MainActivity and the ShareActivity. If the user is not logged yet, the MainActivity will start the LoginActivity, and handle its result.

### Code conventions

Code conventions improve the readability of the software, allowing engineers to understand new code more quickly and thoroughly. Thus, we are using Oracle's [code conventions for Java]{http://www.oracle.com/technetwork/java/codeconv-138413.html} so that our code look beautiful.

Roadmap
----------

We use GitHub issues to track our progress, so check our [milestones](https://github.com/buddycloud/buddycloud-android/issues/milestones) page for release planning. 

Testing
----------

We are using JUnit and [instrumentation](http://developer.android.com/tools/testing/testing_android.html#Instrumentation) to assemble our tests. Source code can be found at https://github.com/buddycloud/buddycloud-android-testing and PRs are welcome :)

Debug help
----------

Debugging TCP and HTTPS session reuse

``` bash
sudo tcptrack -i eth0 port 443
```
