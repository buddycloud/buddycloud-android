package com.buddycloud.notifications;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.buddycloud.init.ApplicationManager;
import com.buddycloud.log.Logger;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.ModelCallbackImpl;
import com.buddycloud.model.NotificationSettingsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.VersionUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMIntentService extends IntentService {

	public static final String GCM_NOTIFICATION_EVENT = "com.buddycloud.GCM_NOTIFICATION_EVENT";
	private static final String TAG = GCMIntentService.class.getName();
	
	public GCMIntentService() {
		super("GCMIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent remoteIntent) {
		
		Logger.info(TAG, "GCM reveived " + remoteIntent.toString());
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(remoteIntent);
        
		Bundle extras = remoteIntent.getExtras();
		if (!extras.isEmpty())
		{
			if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				Logger.info(TAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            	Logger.info(TAG, "Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            	
            	// If it's a regular GCM message, do some work.
        		final Context context = ApplicationManager.getAppContext();
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
        		
        		GCMEvent event = GCMEvent.valueOf(remoteIntent.getStringExtra("event"));
        		GCMNotificationListener notificationListener = createNotificationListener(context, event);
        		if (notificationListener != null) {
        			notificationListener.onMessage(event, context, remoteIntent);
        		}
            }
		}

		 // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(remoteIntent);
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

	public static void registerOnPusher(String regId) {
		
		final Context context = ApplicationManager.getAppContext();
		try {
			sendToPusher(context, regId);
			
			String currentRegId = Preferences.getPreference(context, Preferences.CURRENT_GCM_ID);
			if (currentRegId != null && !regId.equals(currentRegId)) {
				removeFromPusher(context, currentRegId);
			}
			
			int appVersion = VersionUtils.getVersionCode(context);
			Preferences.setPreference(context, Preferences.CURRENT_GCM_ID, regId);
			Preferences.setPreference(context, Preferences.APP_VERSION, String.valueOf(appVersion));
		} catch (NameNotFoundException e) {
			Logger.error(TAG, "App Version code not found.", e);
		}
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
			Logger.error(TAG, "Failure to register GCM settings.", e);
			return;
		}
		
		NotificationSettingsModel.getInstance().save(
				context, settings, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Logger.info(TAG, "Succesfully registered GCM settings.");
					}
					@Override
					public void error(Throwable throwable) {
						Logger.error(TAG, "Failure to register GCM settings.", throwable);
					}
				});
	}

	public static void removeFromPusher(final Context context, String regId) {
		NotificationSettingsModel.getInstance().delete(context, 
				new ModelCallback<Void>() {
					@Override
					public void success(Void response) {
						Logger.info(TAG, "Succesfully removed GCM settings.");
					}
					@Override	
					public void error(Throwable throwable) {
						Logger.error(TAG, "Failed to remove GCM settings.", throwable);
					}
				}, regId);
	}
}
