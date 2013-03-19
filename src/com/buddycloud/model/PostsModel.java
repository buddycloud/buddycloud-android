package com.buddycloud.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class PostsModel implements Model<JSONArray, JSONObject, String> {

	private static PostsModel instance;
	private static final String ENDPOINT = "/content/posts?max=31"; 

	private Map<String, JSONArray> channelsPosts = new HashMap<String, JSONArray>();

	private PostsModel() {}

	public static PostsModel getInstance() {
		if (instance == null) {
			instance = new PostsModel();
		}
		return instance;
	}

	@Override
	public void refresh(Activity context, final ModelCallback<JSONArray> callback, String... p) {
		if (p != null && p.length == 1) {
			final String channel = p[0];
			BuddycloudHTTPHelper.getArray(url(context, channel), context,
					new ModelCallback<JSONArray>() {

				@Override
				public void success(JSONArray response) {
					channelsPosts.put(channel, response);
					if (callback != null) {
						callback.success(response);
					}
				}

				@Override
				public void error(Throwable throwable) {
					if (callback != null) {
						callback.error(throwable);
					}
				}
			});
		}
	}

	private static String url(Activity context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT;
	}

	@Override
	public void save(Activity context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		// TODO Auto-generated method stub
	}

	@Override
	public JSONArray get(Activity context, String... p) {
		if (p != null && p.length == 1) {
			String channelJid = p[0];
			if (channelsPosts.containsKey(channelJid)) {
				return channelsPosts.get(channelJid);
			}
		}
		
		return new JSONArray();
	}

}
