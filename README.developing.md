Some notes on the design decisions and architecture of the Android app.

What does it do
----------

Catch up with your buddycloud channels using your Android device.

Description
----------

The Android client implements most of the features provided by the [buddycloud API] (http://buddycloud.org/w/index.php?title=Buddycloud_HTTP_API "buddycloud HTTP API"): create and read channels' content; create, delete and follow channels; search by content and metadata; recommentation and similarity features; channel summary synchronization; push notifications using GCM; API discovery via DNS TXT records; and much more. 

Technologies
----------

Besides the Android SDK and the support library, we use [ActionBarSherlock](https://github.com/JakeWharton/ActionBarSherlock) to implement the action bar design pattern; [SligingMenu](https://github.com/jfeinstein10/SlidingMenu) to implement the side menu; [picasso](https://github.com/square/picasso) for displaying and transforming images; and [dnsjava](http://www.xbill.org/dnsjava/) for DNS queries.

Security model
----------

Architecture
----------

How to get started
----------

Roadmap
----------

Testing
----------

Debug help
----------

Debugging TCP and HTTPS session reuse

``` bash
sudo tcptrack -i eth0 port 443
```
