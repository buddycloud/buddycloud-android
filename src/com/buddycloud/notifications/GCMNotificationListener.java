package com.buddycloud.notifications;

import android.content.Context;
import android.content.Intent;

public interface GCMNotificationListener {

	void onMessage(GCMEvent event, Context context, Intent message);
	
}
