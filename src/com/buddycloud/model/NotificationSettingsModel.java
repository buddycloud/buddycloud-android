package com.buddycloud.model;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences.Editor;

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
	}

	@Override
	public void save(Context context, JSONObject object,
			final ModelCallback<JSONObject> callback, String... p) {
		StringEntity requestEntity = null;
		try {
			requestEntity = new StringEntity(object.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
			return;
		}
		requestEntity.setContentType("application/json");
		BuddycloudHTTPHelper.postArray(url(context), true, false, requestEntity, context, 
				new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				callback.success(null);
			}
			
			@Override
			public void error(Throwable throwable) {
				callback.error(throwable);				
			}
		});
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
	public void delete(Context context, final ModelCallback<Void> callback, String... p) {
		JSONObject settings = new JSONObject();
		try {
			settings.put("type", "gcm");
			settings.put("target", p[0]);
		} catch (JSONException e) {
			// Best effort
			return;
		}
		
		StringEntity requestEntity = null;
		try {
			requestEntity = new StringEntity(settings.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
			return;
		}
		requestEntity.setContentType("application/json");
		BuddycloudHTTPHelper.delete(url(context), true, true, requestEntity, 
				context, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				callback.success(null);
			}

			@Override	
			public void error(Throwable throwable) {
				callback.error(throwable);
			}
		});
	}
}
