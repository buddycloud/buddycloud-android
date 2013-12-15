package com.buddycloud.model;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class NotificationSettingsModel extends AbstractModel<JSONObject, JSONObject, String> {

	public static final String PREF_ANYCHANNEL_NOTIFICATION = "pref_key_enable_post_anychannel_notification";
	public static final String PREF_COMMENTS_NOTIFICATION = "pref_key_enable_comments_notification";
	public static final String PREF_FOLLOWER_NOTIFICATION = "pref_key_enable_new_follower_notification";
	public static final String PREF_MYCHANNEL_NOTIFICATION = "pref_key_enable_post_mychannel_notification";
	public static final String PREF_MENTION_NOTIFICATION = "pref_key_enable_mention_notification";
	
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

	public void saveFromPreferences(Context context, ModelCallback<JSONObject> modelCallback) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		JSONObject settings = null;
		try {
			settings = toJSON(sharedPrefs);
		} catch (JSONException e) {
			modelCallback.error(e);
			return;
		}
		save(context, settings, modelCallback);
	}

	private JSONObject toJSON(SharedPreferences sharedPrefs) throws JSONException {
		JSONObject settings = new JSONObject();
		settings.put("type", "gcm");
		settings.put("postMentionedMe", getPref(sharedPrefs, PREF_MENTION_NOTIFICATION));
		settings.put("postOnMyChannel", getPref(sharedPrefs, PREF_MYCHANNEL_NOTIFICATION));
		settings.put("followMyChannel", getPref(sharedPrefs, PREF_FOLLOWER_NOTIFICATION));
		settings.put("postAfterMe", getPref(sharedPrefs, PREF_COMMENTS_NOTIFICATION));
		settings.put("postOnSubscribedChannel", getPref(sharedPrefs, PREF_ANYCHANNEL_NOTIFICATION));
		return settings;
	}

	private String getPref(SharedPreferences sharedPrefs, String key) {
		return Boolean.valueOf(sharedPrefs.getBoolean(key, true)).toString();
	}
	
	public void fillEditor(Editor editor, JSONObject object) {
		editor.putBoolean(PREF_MENTION_NOTIFICATION, 
				object.optBoolean("postMentionedMe"));
		editor.putBoolean(PREF_MYCHANNEL_NOTIFICATION, 
				object.optBoolean("postOnMyChannel"));
		editor.putBoolean(PREF_FOLLOWER_NOTIFICATION, 
				object.optBoolean("followMyChannel"));
		editor.putBoolean(PREF_COMMENTS_NOTIFICATION, 
				object.optBoolean("postAfterMe"));
		editor.putBoolean(PREF_ANYCHANNEL_NOTIFICATION, 
				object.optBoolean("postOnSubscribedChannel"));
	}

	@Override
	public void delete(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}
}
