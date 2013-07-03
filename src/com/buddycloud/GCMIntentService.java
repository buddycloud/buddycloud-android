package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = GCMIntentService.class.getName();
	private static final int NOTIFICATION_ID = 1001;
	
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.e(TAG, "GCM error " + arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent message) {
		Log.d(TAG, "GCM reveived " + message);
		
		String content = message.getStringExtra("CONTENT");
		String authorJid = message.getStringExtra("AUTHOR_JID");
		String channelJid = message.getStringExtra("CHANNEL_JID");
		
		if (content == null || authorJid == null || channelJid == null) {
			return;
		}
		
		Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cp);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.notification_icon)
		        .setContentTitle("Post from " + authorJid)
		        .setLights(Color.GREEN, 1000, 1000)
		        .setVibrate(new long[] {500, 500, 500})
		        .setSound(soundUri)
		        .setContentText(content);
		
		setPriority(mBuilder);
		
		Intent resultIntent = new Intent(this, MainActivity.class);
		resultIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		
		Notification notification = mBuilder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setPriority(NotificationCompat.Builder mBuilder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
	    }
	}

	@Override
	protected void onRegistered(Context arg0, String regId) {
		sendToPusher(getApplicationContext(), regId);
	}

	public static void sendToPusher(final Context context, String regId) {
		JSONObject settings = new JSONObject();
		try {
			settings.put("type", "gcm");
			settings.put("target", regId);
			settings.put("postOnSubscribedChannel", true);
		} catch (JSONException e) {
			Log.e(TAG, "Failure to register GCM settings.", e);
			return;
		}
		
		NotificationSettingsModel.getInstance().save(
				context, settings, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Log.i(TAG, "Succesfully registered GCM settings.");
						GCMRegistrar.setRegisteredOnServer(context, true);
					}
					@Override
					public void error(Throwable throwable) {
						Log.e(TAG, "Failure to register GCM settings.", throwable);
					}
				});
	}

	@Override
	protected void onUnregistered(Context arg0, String regId) {
		Log.w(TAG, "Unregistered from GCM " + regId);
	}

}
