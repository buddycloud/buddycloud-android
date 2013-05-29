package com.buddycloud.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsModel implements Model<JSONArray, JSONArray, Void> {

	private static SubscribedChannelsModel instance;
	private static final String ENDPOINT = "/subscribed"; 
	private static final String POST_NODE_SUFIX = "/posts";
	
	private SubscribedChannelsModel() {}
	
	public static SubscribedChannelsModel getInstance() {
		if (instance == null) {
			instance = new SubscribedChannelsModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONArray> callback, Void... p) {
		BuddycloudHTTPHelper.getObject(url(context), context, 
				new ModelCallback<JSONObject>() {
					@SuppressWarnings("unchecked")
					@Override
					public void success(JSONObject response) {
						final List<String> channels = new ArrayList<String>();
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String node = (String) keyIterator.next();
							if (node.endsWith(POST_NODE_SUFIX)) {
								channels.add(node.split("/")[0]);
							}
						}
						
						callback.success(new JSONArray(channels));
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
	public void save(Context context, JSONArray object,
			ModelCallback<JSONArray> callback, Void... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray getFromCache(Context context, Void... p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, Void... p) {
		// TODO Auto-generated method stub
		
	}
}
