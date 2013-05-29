package com.buddycloud.model;

import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class NotificationMetadataModel implements Model<JSONObject, JSONObject, String> {

	private static final String ENDPOINT = "/notification_metadata?type=gcm";
	private static NotificationMetadataModel instance;
	
	private NotificationMetadataModel() {}

	public static NotificationMetadataModel getInstance() {
		if (instance == null) {
			instance = new NotificationMetadataModel();
		}
		return instance;
	}
	
	@Override
	public void getFromServer(final Context context, final ModelCallback<JSONObject> callback,
			String... p) {
		BuddycloudHTTPHelper.getObject(url(context), true, true, context, callback);
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		// TODO Auto-generated method stub
	}

	private static String url(Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT;
	}

	@Override
	public JSONObject getFromCache(Context context, String... p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}
}
