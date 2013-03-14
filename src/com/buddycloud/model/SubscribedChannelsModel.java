package com.buddycloud.model;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsModel implements Model<JSONArray, JSONArray, Void> {

	private static SubscribedChannelsModel instance;
	private static final String ENDPOINT = "/subscribed"; 
	private static final String POST_NODE_SUFIX = "/posts";
	
	private JSONArray subscribedChannels = new JSONArray();
	
	private SubscribedChannelsModel() {}
	
	public static SubscribedChannelsModel getInstance() {
		if (instance == null) {
			instance = new SubscribedChannelsModel();
		}
		return instance;
	}
	
	@Override
	public void refresh(Activity context, final ModelCallback<JSONArray> callback, Void... p) {
		BuddycloudHTTPHelper.getObject(url(context), 
				true, context, new ModelCallback<JSONObject>() {
					@SuppressWarnings("unchecked")
					@Override
					public void success(JSONObject response) {
						JSONArray channels = new JSONArray();
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String node = (String) keyIterator.next();
							if (node.endsWith(POST_NODE_SUFIX)) {
								channels.put(node.split("/")[0]);
							}
						}
						subscribedChannels = channels;
						callback.success(channels);
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}

	private static String url(Activity context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT;
	}


	@Override
	public void save(Activity context, JSONArray object,
			ModelCallback<JSONArray> callback, Void... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray get(Activity context, Void... p) {
		return subscribedChannels;
	}
	
}
