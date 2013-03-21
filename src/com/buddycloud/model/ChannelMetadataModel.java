package com.buddycloud.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class ChannelMetadataModel implements Model<JSONObject, JSONObject, String> {

	private static ChannelMetadataModel instance;
	private static final String ENDPOINT = "/metadata/posts"; 
	
	private Map<String, JSONObject> channelsMetadataMap = new HashMap<String, JSONObject>();
	
	private ChannelMetadataModel() {}
	
	public static ChannelMetadataModel getInstance() {
		if (instance == null) {
			instance = new ChannelMetadataModel();
		}
		return instance;
	}
	
	private void add(String channel, JSONObject response) {
		channelsMetadataMap.put(channel, response);
	}
	
	@Override
	public void refresh(Activity context, final ModelCallback<JSONObject> callback, String... p) {
		if (p != null && p.length == 1) {
			final String channel = p[0];
			
			BuddycloudHTTPHelper.getObject(url(context, channel), 
					context, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
					add(channel, response);
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
	public JSONObject get(Activity context, String... p) {
		return p != null && p.length == 1 ? channelsMetadataMap.get(p[0]) : null;
	}
	
}
