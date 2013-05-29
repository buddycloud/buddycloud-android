package com.buddycloud.model;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class NotificationSettingsModel implements Model<JSONObject, JSONObject, String> {

	private static final String ENDPOINT = "/notification_settings";
	private static NotificationSettingsModel instance;
	
	private NotificationSettingsModel() {}

	public static NotificationSettingsModel getInstance() {
		if (instance == null) {
			instance = new NotificationSettingsModel();
		}
		return instance;
	}
	
	@Override
	public void getFromServer(final Context context, final ModelCallback<JSONObject> callback,
			String... p) {
		BuddycloudHTTPHelper.getObject(url(context) + "?type=gcm", true, true, context, callback);
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		StringEntity requestEntity = null;
		try {
			requestEntity = new StringEntity(object.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
			return;
		}
		requestEntity.setContentType("application/json");
		BuddycloudHTTPHelper.post(url(context), true, false, requestEntity, context, callback);
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
