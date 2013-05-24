package com.buddycloud.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SearchChannelsModel implements Model<JSONArray, JSONArray, String> {

	private static SearchChannelsModel instance;
	private static final String ENDPOINT = "/search"; 
	
	private SearchChannelsModel() {}
	
	public static SearchChannelsModel getInstance() {
		if (instance == null) {
			instance = new SearchChannelsModel();
		}
		return instance;
	}
	
	public void refresh(final Context context, final ModelCallback<JSONArray> callback, String... p) {
		BuddycloudHTTPHelper.getObject(url(context, p[0], p[1]), context, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						List<String> channels = new ArrayList<String>();
						JSONArray channelJson = response.optJSONArray("items");
						for (int i = 0; i < channelJson.length(); i++) {
							channels.add(channelJson.optJSONObject(i).optString("jid"));
						}
						callback.success(new JSONArray(channels));
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}

	private static String url(Context context, String type, String q) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT + "?type=" + type + "&q=" + q;
	}


	@Override
	public void save(Context context, JSONArray object,
			ModelCallback<JSONArray> callback, String... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray get(Context context, String... p) {
		return null;
	}

	
}
