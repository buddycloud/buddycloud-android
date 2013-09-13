package com.buddycloud.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.buddycloud.GCMIntentService;
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
		if (regId.equals("")) {
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
				GCMIntentService.GCM_NOTIFICATION, 
				new JSONArray().toString());
	}

	public static JSONArray getGCMAuthors(Context context) {
		String gcmStrArray = Preferences.getPreference(context, 
				GCMIntentService.GCM_NOTIFICATION, 
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
		Preferences.setPreference(context, GCMIntentService.GCM_NOTIFICATION, 
				gcmAuthors.toString());
	}

}
