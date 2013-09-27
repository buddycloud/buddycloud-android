package com.buddycloud.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.buddycloud.GenericChannelActivity;
import com.buddycloud.R;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.adapter.PendingSubscriptionsAdapter;
import com.buddycloud.model.SubscribedChannelsModel;

public class GCMFollowRequestNotificationListener implements GCMNotificationListener {

	private static final int NOTIFICATION_ID = 1002;
	
	@Override
	public void onMessage(GCMEvent event, Context context, Intent message) {
		
		String ownerJid = message.getStringExtra("OWNER_JID");
		String channelJid = message.getStringExtra("CHANNEL_JID");
		String followerJid = message.getStringExtra("FOLLOWER_JID");
		
		if (followerJid == null || ownerJid == null || channelJid == null) {
			return;
		}
		
		NotificationCompat.Builder mBuilder = GCMUtils.createNotificationBuilder(context);
		mBuilder.setContentTitle(context.getString(R.string.gcm_follow_request_notification_title));
		mBuilder.setContentText(context.getString(R.string.gcm_follow_request_notification_content, 
				followerJid, channelJid));
		
		Intent resultIntent = new Intent(context, GenericChannelActivity.class);
		resultIntent.putExtra(GenericChannelActivity.ADAPTER_NAME, 
				PendingSubscriptionsAdapter.ADAPTER_NAME);
		resultIntent.putExtra(GenericChannelsFragment.CHANNEL, 
				channelJid);
		resultIntent.putExtra(SubscribedChannelsModel.ROLE, 
				SubscribedChannelsModel.ROLE_MODERATOR);
		
		Notification notification = GCMUtils.build(context, mBuilder, resultIntent);
		
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

}
