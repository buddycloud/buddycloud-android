package com.buddycloud.notifications;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;

import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.log.Logger;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationMetadataModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.VersionUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMUtils {

	private static final String TAG = GCMUtils.class.getName();
	
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	public static boolean checkPlayServices(final Context context) {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity)context,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Logger.info(TAG, "This device is not supported (Google Play Services APK)");
	        }
	        return false;
	    }
	    return true;
	}
	
	public static void issueGCMRegistration(final Context context) {
		
		if (checkPlayServices(context)) {
			final String regId = getRegistrationId(context);
			if (regId == null || regId.equals("")) {
				NotificationMetadataModel.getInstance().getFromServer(context, new ModelCallback<JSONObject>() {
					
					@Override
					public void success(JSONObject response) {
					
						String sender = response.optString("google_project_id", null);
						if (sender != null) {
							registerInBackground(context, sender);
						} else {
							Logger.warn(TAG, "GCM project id not found.");
						}
					}
					
					@Override
					public void error(Throwable throwable) {
						Logger.error(TAG, "Failed to register in GCM.", throwable);
					}
				});
			}
			else {
				GCMIntentService.sendToPusher(context, regId);
			}
		}
		else {
			Logger.info(TAG, "No valid Google Play Services APK found.");
		}
	}
	
	/**
	 * This method register for GCM services in the background mode.
	 * 
	 * @param context
	 * @param senderId
	 */
	public static void registerInBackground(final Context context, final String senderId) {

		new AsyncTask<String, Void, String>() {

			@Override
			protected String doInBackground(String... params) {

				String senderId = params[0];	// project sender id
				String regId = null;
				
				try {
					GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
					regId = gcm.register(senderId);	// new GCM registration id
					
					// register on server and save on disk.
					GCMIntentService.registerOnPusher(regId);
				} catch (IOException e) {
					Logger.error(TAG, "GCM Registration failed.", e);
				}
				
				return regId;
			}
		}.execute(senderId);
	}

	/**
	 * This method unregister from the GCM services and also pusher
	 * server in the background mode. 
	 * 
	 * @param context
	 */
	public static void unregisterInBackground(final Context context) {
		
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {

				try {
					GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
					gcm.unregister();
					
					// Unregister the GCM info from pusher server.
					String regId = Preferences.getPreference(context, Preferences.CURRENT_GCM_ID);
					GCMIntentService.removeFromPusher(context, regId);
				} catch (IOException e) {
					Logger.error(TAG, "GCM Un-registration failed.", e);
				} 
				
				return null;
			}
		}.execute();
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private static String getRegistrationId(final Context context) {

	    String registrationId = Preferences.getPreference(context, Preferences.CURRENT_GCM_ID);
	    if (registrationId == null || registrationId.equals("")) {
	        Logger.info(TAG, "Registration not found.");
	        return "";
	    }
	    
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = Integer.MIN_VALUE;
	    int currentVersion;
	    
		try {
			currentVersion = VersionUtils.getVersionCode(context);
		    if (Preferences.getPreference(context, Preferences.APP_VERSION) != null) {
		    	registeredVersion = Integer.valueOf(Preferences.getPreference(context, Preferences.APP_VERSION));
		    }
		    
		    if (registeredVersion != currentVersion) {
		        Logger.info(TAG, "App version changed.");
		        return "";
		    }
		} catch (NameNotFoundException e) {
			Logger.error(TAG, "Version code not found.", e);
		}

	    return registrationId;
	}
	
	public static void clearGCMAuthors(Context context) {
		Preferences.setPreference(context, 
				GCMPostNotificationListener.GCM_NOTIFICATION_POST_AUTHORS, 
				new JSONArray().toString());
	}

	public static JSONArray getGCMAuthors(Context context) {
		String gcmStrArray = Preferences.getPreference(context, 
				GCMPostNotificationListener.GCM_NOTIFICATION_POST_AUTHORS, 
				new JSONArray().toString());
		try {
			return new JSONArray(gcmStrArray);
		} catch (JSONException e) {
			return new JSONArray();
		}
	}
	
	public static void addGCMAuthor(Context context, String author) {
		JSONArray gcmAuthors = getGCMAuthors(context);
		gcmAuthors.put(author);
		Preferences.setPreference(context, 
				GCMPostNotificationListener.GCM_NOTIFICATION_POST_AUTHORS, 
				gcmAuthors.toString());
	}

	public static Builder createNotificationBuilder(Context context) {
		NotificationCompat.Builder builder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.notification_icon)
		        .setLights(Color.GREEN, 1000, 1000);
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		boolean doVibrate = sharedPrefs.getBoolean("pref_key_enable_vibration", true);
		if (doVibrate) {
			builder.setVibrate(new long[] {500, 500, 500});
		}
		
		boolean playAudio = sharedPrefs.getBoolean("pref_key_enable_sound", true);
		if (playAudio) {
			Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.cp);
			builder.setSound(soundUri);
		}
		
		setPriority(builder);
		builder.setDeleteIntent(getDeleteIntent(context));
		
		return builder;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private static void setPriority(NotificationCompat.Builder mBuilder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
	    }
	}
	
	private static PendingIntent getDeleteIntent(Context context) {
	    Intent intent = new Intent(context, GCMBroadcastReceiver.class);
	    intent.setAction("com.buddycloud.NOTIFICATION_CANCELLED");
	    return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public static Notification build(Context context,
			NotificationCompat.Builder mBuilder, Intent resultIntent) {
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
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
		return notification;
	}
	

	

}
