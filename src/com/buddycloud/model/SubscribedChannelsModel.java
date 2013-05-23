package com.buddycloud.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	private JSONArray subscribedChannels = new JSONArray();
	
	private SubscribedChannelsModel() {}
	
	public static SubscribedChannelsModel getInstance() {
		if (instance == null) {
			instance = new SubscribedChannelsModel();
		}
		return instance;
	}
	
	public void refresh(final Context context, final ModelCallback<JSONArray> callback, Void... p) {
		BuddycloudHTTPHelper.getObject(url(context), context, 
				new ModelCallback<JSONObject>() {
					@SuppressWarnings("unchecked")
					@Override
					public void success(JSONObject response) {
						List<String> channels = new ArrayList<String>();
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String node = (String) keyIterator.next();
							if (node.endsWith(POST_NODE_SUFIX)) {
								channels.add(node.split("/")[0]);
							}
						}
						
						List<String> sortedChannels = sort(channels);
						subscribedChannels = new JSONArray(sortedChannels);
						callback.success(subscribedChannels);
					}
					
					private List<String> sort(List<String> channels) {
						Collections.sort(channels, new Comparator<String>() {
							@Override
							public int compare(String lhs, String rhs) {
								int countA = getCounter(lhs, "mentionsCount");
								int countB = getCounter(rhs, "mentionsCount");
								int diff = countB - countA;
								
								if (diff == 0) {
									countA = getCounter(lhs, "totalCount");
									countB = getCounter(rhs, "totalCount");
									diff = countB - countA;
								}
								
								if (diff != 0) {
									return diff;
								}
								
								return rhs.compareTo(lhs);
							}
							
							private int getCounter(String channel, String key) {
								SyncModel syncModel = SyncModel.getInstance();
								return syncModel.countersFromChannel(channel).optInt(key);
							}
						});
						
						return channels;
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
	public JSONArray get(Context context, Void... p) {
		return subscribedChannels;
	}
	
}
