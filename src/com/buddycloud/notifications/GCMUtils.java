package com.buddycloud.notifications;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.buddycloud.GCMBroadcastReceiver;
import com.buddycloud.GCMIntentService;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationMetadataModel;
import com.buddycloud.preferences.Preferences;
import com.google.android.gcm.GCMRegistrar;

public class GCMUtils {

	private static final String TAG = GCMUtils.class.getName();
	
	public static void issueGCMRegistration(final Context context) {
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		final String regId = GCMRegistrar.getRegistrationId(context);
		if (regId == null || regId.equals("")) {
			NotificationMetadataModel.getInstance().getFromServer(context, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
					String sender = response.optString("google_project_id", null);
					if (sender != null) {
						GCMRegistrar.register(context, sender);
					} else {
						Log.w(TAG, "GCM project id not found.");
					}
				}
				@Override
				public void error(Throwable throwable) {
					Log.e(TAG, "Failed to register in GCM.", throwable);
				}
			});
		} else {
			GCMIntentService.sendToPusher(context, regId);
		}
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
