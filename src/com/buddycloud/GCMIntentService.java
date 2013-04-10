package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = GCMIntentService.class.getName();
	private static final int NOTIFICATION_ID = 1001;
	
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.e(TAG, "GCM error " + arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent message) {
		String content = message.getStringExtra("CONTENT");
		String authorJid = message.getStringExtra("AUTHOR_JID");
		
		if (content == null || authorJid == null) {
			return;
		}
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.notification_icon)
		        .setContentTitle("Post from " + authorJid)
		        .setContentText(content);
		
		Intent resultIntent = new Intent(this, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@Override
	protected void onRegistered(Context arg0, String regId) {
		sendToPusher(getApplicationContext(), regId);
	}

	public static void sendToPusher(Context context, String regId) {
		JSONObject settings = new JSONObject();
		try {
			settings.put("type", "gcm");
			settings.put("target", regId);
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
