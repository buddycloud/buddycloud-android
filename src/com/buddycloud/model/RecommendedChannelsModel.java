package com.buddycloud.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class RecommendedChannelsModel extends AbstractModel<JSONArray, JSONArray, String> {

	private static final String ENDPOINT = "/recommendations"; 
	private static final int MAX = 20;
	private static RecommendedChannelsModel instance;
	
	private RecommendedChannelsModel() {}
	
	public static RecommendedChannelsModel getInstance() {
		if (instance == null) {
			instance = new RecommendedChannelsModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONArray> callback, String... p) {
		BuddycloudHTTPHelper.getObject(url(context), context, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						List<String> channels = new ArrayList<String>();
						JSONArray items = response.optJSONArray("items");
						for (int i = 0; i < items.length(); i++) {
							JSONObject item = items.optJSONObject(i);
							channels.add(item.optString("jid"));
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
		String myJid = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		return apiAddress + ENDPOINT + "?user=" + myJid + "&max=" + MAX;
	}


	@Override
	public void save(Context context, JSONArray object,
			ModelCallback<JSONArray> callback, String... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONArray getFromCache(Context context, String... p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}
}
