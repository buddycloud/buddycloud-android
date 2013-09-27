package com.buddycloud.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.buddycloud.GCMIntentService;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.fragments.GenericChannelsFragment;

public class GCMFollowRequestApprovedNotificationListener implements GCMNotificationListener {

	private static final int NOTIFICATION_ID = 1003;
	
	@Override
	public void onMessage(GCMEvent event, Context context, Intent message) {
		
		String channelJid = message.getStringExtra("CHANNEL_JID");
		String followerJid = message.getStringExtra("FOLLOWER_JID");
		
		if (followerJid == null || channelJid == null) {
			return;
		}
		
		NotificationCompat.Builder mBuilder = GCMUtils.createNotificationBuilder(context);
		mBuilder.setContentTitle(context.getString(R.string.gcm_follow_request_approved_notification_title));
		mBuilder.setContentText(context.getString(R.string.gcm_follow_request_approved_notification_content, 
				channelJid));
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.putExtra(GCMIntentService.GCM_NOTIFICATION_EVENT, event.toString());
		resultIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		
		Notification notification = GCMUtils.build(context, mBuilder, resultIntent);
		
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

}
