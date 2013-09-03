package com.buddycloud.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SearchChannelsModel extends AbstractModel<JSONArray, JSONArray, String> {

	public static final String METADATA_TYPE = "metadata";
	public static final String POST_TYPE = "content";
	
	private static final String ENDPOINT = "/search"; 
	private static final int MAX = 5;
	private static SearchChannelsModel instance;
	
	private SearchChannelsModel() {}
	
	public static SearchChannelsModel getInstance() {
		if (instance == null) {
			instance = new SearchChannelsModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONArray> callback, String... p) {
		final String type = p[0];
		final String q = p[1];
		BuddycloudHTTPHelper.getObject(url(context, type, q), context, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						List<JSONObject> channelItems = new ArrayList<JSONObject>();
						JSONArray items = response.optJSONArray("items");
						for (int i = 0; i < items.length(); i++) {
							Map<String, String> channelItem = new HashMap<String, String>();
							JSONObject item = items.optJSONObject(i);
							if (type.equals(METADATA_TYPE)) {
								channelItem.put("jid", item.optString("jid"));
							} else if (type.equals(POST_TYPE)) {
								channelItem.put("jid", item.optString("parent_simpleid"));
								channelItem.put("post_id", item.optString("id"));
							}
							channelItems.add(new JSONObject(channelItem));
						}
						callback.success(new JSONArray(channelItems));
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}

	private static String url(Context context, String type, String q) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT + "?type=" + type + "&q=" + q + "&max=" + MAX;
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

	@Override
	public void delete(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}
}
