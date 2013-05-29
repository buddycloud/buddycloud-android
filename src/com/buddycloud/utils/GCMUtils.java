package com.buddycloud.utils;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.buddycloud.GCMIntentService;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationMetadataModel;
import com.google.android.gcm.GCMRegistrar;

public class GCMUtils {

	private static final String TAG = GCMUtils.class.getName();
	
	public static void issueGCMRegistration(final Context context) {
		GCMRegistrar.checkDevice(context);
		GCMRegistrar.checkManifest(context);
		final String regId = GCMRegistrar.getRegistrationId(context);
		if (regId.equals("")) {
			NotificationMetadataModel.getInstance().getAsync(context, new ModelCallback<JSONObject>() {
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

}
