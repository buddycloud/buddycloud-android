package com.buddycloud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;
import com.buddycloud.model.PostsModel;
import com.buddycloud.utils.GCMUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService {

	public static final String GCM_NOTIFICATION = "com.buddycloud.GCM_NOTIFICATION";
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
		
		GCMUtils.addGCMAuthor(this, authorJid);
		JSONArray gcmAuthors = GCMUtils.getGCMAuthors(this);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.notification_icon)
		        .setLights(Color.GREEN, 1000, 1000);
		
		boolean isAggregate = gcmAuthors.length() > 1;
		
		if (isAggregate) {
			
			Integer gcmAuthorCount = gcmAuthors.length();
			mBuilder.setContentTitle(getString(
					R.string.gcm_multi_notification_title, 
					gcmAuthorCount.toString()));
			
			Set<String> gcmAuthorsUnique = new HashSet<String>();
			for (int i = 0; i < gcmAuthors.length(); i++) {
				gcmAuthorsUnique.add(gcmAuthors.optString(i));
			}
			List<String> gcmAuthorsUniqueList = new ArrayList<String>(
					gcmAuthorsUnique);
			
			StringBuilder allAuthorsButLastBuilder = new StringBuilder();
			allAuthorsButLastBuilder.append(gcmAuthorsUniqueList.get(0));
			
			for (int i = 1; i < gcmAuthorsUniqueList.size() - 1; i++) {
				allAuthorsButLastBuilder.append(", ");
				String gcmAuthor = gcmAuthorsUniqueList.get(i);
				allAuthorsButLastBuilder.append(gcmAuthor);
			}
			
			String lastAuthor = gcmAuthorsUniqueList.get(
					gcmAuthorsUniqueList.size() - 1);
			
			mBuilder.setContentText(getString(R.string.gcm_multi_notification_content, 
					allAuthorsButLastBuilder.toString(), lastAuthor));
		} else {
			mBuilder.setContentTitle(getString(
					R.string.gcm_single_notification_title, authorJid));
			mBuilder.setContentText(content);
		}
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		boolean doVibrate = sharedPrefs.getBoolean("pref_key_enable_vibration", true);
		if (doVibrate) {
			mBuilder.setVibrate(new long[] {500, 500, 500});
		}
		
		boolean playAudio = sharedPrefs.getBoolean("pref_key_enable_sound", true);
		if (playAudio) {
			Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.cp);
			mBuilder.setSound(soundUri);
		}
		
		setPriority(mBuilder);
		
		Intent resultIntent = new Intent(this, MainActivity.class);
		if (!isAggregate) {
			resultIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		}
		resultIntent.putExtra(GCMIntentService.GCM_NOTIFICATION, true);
		
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		
		mBuilder.setDeleteIntent(getDeleteIntent());
		
		Notification notification = mBuilder.build();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		
		NotificationManager mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		
		PostsModel.getInstance().fillMoreAfterLatest(this, new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				// Pretty much best effort here
			}

			@Override
			public void error(Throwable throwable) {
				// Pretty much best effort here
			}
		}, channelJid);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setPriority(NotificationCompat.Builder mBuilder) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
	    }
	}
	
	private PendingIntent getDeleteIntent() {
	    Intent intent = new Intent(this, GCMBroadcastReceiver.class);
	    intent.setAction("com.buddycloud.NOTIFICATION_CANCELLED");
	    return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
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
