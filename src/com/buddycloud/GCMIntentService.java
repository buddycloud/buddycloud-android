package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;
import com.buddycloud.notifications.GCMEvent;
import com.buddycloud.notifications.GCMFollowRequestApprovedNotificationListener;
import com.buddycloud.notifications.GCMFollowRequestNotificationListener;
import com.buddycloud.notifications.GCMNotificationListener;
import com.buddycloud.notifications.GCMPostNotificationListener;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_NOTIFICATION_EVENT = "com.buddycloud.GCM_NOTIFICATION_EVENT";
	private static final String TAG = GCMIntentService.class.getName();
	
	@Override
	protected void onError(Context arg0, String arg1) {
		Log.e(TAG, "GCM error " + arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent message) {
		Log.d(TAG, "GCM reveived " + message);
		
		GCMEvent event = GCMEvent.valueOf(message.getStringExtra("event"));
		GCMNotificationListener notificationListener = createNotificationListener(event);
		notificationListener.onMessage(event, arg0, message);
	}

	private GCMNotificationListener createNotificationListener(GCMEvent event) {
		switch (event) {
		case POST_AFTER_MY_POST:
		case POST_ON_SUBSCRIBED_CHANNEL:
		case POST_ON_MY_CHANNEL:
		case MENTION:
			return new GCMPostNotificationListener();
		case FOLLOW_REQUEST:
			return new GCMFollowRequestNotificationListener();
		case FOLLOW_REQUEST_APPROVED:
			return new GCMFollowRequestApprovedNotificationListener();
		
		default:
			return null;
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
