package com.buddycloud;

import com.buddycloud.notifications.GCMUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GCMBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		GCMUtils.clearGCMAuthors(context);
	}

}
