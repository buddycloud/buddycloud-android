package com.buddycloud;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.buddycloud.notifications.GCMUtils;

public class GCMBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		GCMUtils.clearGCMAuthors(context);
	}

}
