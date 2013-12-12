Some notes on the design decisions and architecture of the Android app.

What does it do
----------

Catch up with your buddycloud channels using your Android device.

Description
----------

The Android client implements most of the features provided by the buddycloud API: 
create and read channels' content; create, delete and follow channels; search by 
content and metadata; recommentation and similarity features; channel summary 
synchronization; push notifications using GCM; API discovery via DNS TXT records; 
and much more. 

Technologies
----------

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
