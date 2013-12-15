package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.ModelCallbackImpl;
import com.buddycloud.model.NotificationSettingsModel;
import com.buddycloud.model.SyncModel;
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
	protected void onMessage(final Context context, Intent message) {
		Log.d(TAG, "GCM reveived " + message);
		
		final SyncModel syncModel = SyncModel.getInstance();
		SyncModel.getInstance().syncNoSummary(context, new ModelCallbackImpl<Void>(){
			@Override
			public void success(Void response) {
				syncModel.fill(context, new ModelCallbackImpl<Void>());
			}
			
			@Override
			public void error(Throwable throwable) {
				success(null);
			}
		});
		
		GCMEvent event = GCMEvent.valueOf(message.getStringExtra("event"));
		GCMNotificationListener notificationListener = createNotificationListener(context, event);
		if (notificationListener != null) {
			notificationListener.onMessage(event, context, message);
		}
	}

	private GCMNotificationListener createNotificationListener(
			Context context, GCMEvent event) {
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		switch (event) {
		case POST_AFTER_MY_POST:
			if (!getPref(sharedPrefs, NotificationSettingsModel.PREF_COMMENTS_NOTIFICATION)) {
				return null;
			}
		case POST_ON_SUBSCRIBED_CHANNEL:
			if (!getPref(sharedPrefs, NotificationSettingsModel.PREF_ANYCHANNEL_NOTIFICATION)) {
				return null;
			}
		case POST_ON_MY_CHANNEL:
			if (!getPref(sharedPrefs, NotificationSettingsModel.PREF_MYCHANNEL_NOTIFICATION)) {
				return null;
			}
		case MENTION:
			if (!getPref(sharedPrefs, NotificationSettingsModel.PREF_MENTION_NOTIFICATION)) {
				return null;
			}
			return new GCMPostNotificationListener();
		case FOLLOW_REQUEST:
			if (!getPref(sharedPrefs, NotificationSettingsModel.PREF_FOLLOWER_NOTIFICATION)) {
				return null;
			}
			return new GCMFollowRequestNotificationListener();
		case FOLLOW_REQUEST_APPROVED:
			if (!getPref(sharedPrefs, NotificationSettingsModel.PREF_FOLLOWER_NOTIFICATION)) {
				return null;
			}
			return new GCMFollowRequestApprovedNotificationListener();
		default:
			return null;
		}
	}

	protected boolean getPref(SharedPreferences sharedPrefs, String key) {
		return Boolean.valueOf(sharedPrefs.getBoolean(key, true));
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
			settings.put("postMentionedMe", Boolean.TRUE.toString());
			settings.put("postOnMyChannel", Boolean.TRUE.toString());
			settings.put("followMyChannel", Boolean.TRUE.toString());
			settings.put("postAfterMe", Boolean.TRUE.toString());
			settings.put("postOnSubscribedChannel", Boolean.TRUE.toString());
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
