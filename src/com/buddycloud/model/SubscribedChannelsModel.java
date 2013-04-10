package com.buddycloud.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsModel implements Model<JSONArray, JSONArray, Void> {

	private static SubscribedChannelsModel instance;
	private static final String ENDPOINT = "/subscribed"; 
	private static final String POST_NODE_SUFIX = "/posts";
	
	private JSONArray subscribedChannels = new JSONArray();
	private JSONArray subscribedChannelsButMine = new JSONArray();
	
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
						List<String> sortedChannelsButMine = new ArrayList<String>(sortedChannels);
						sortedChannelsButMine.remove(Preferences.getPreference(context, Preferences.MY_CHANNEL_JID));
						
						subscribedChannels = new JSONArray(sortedChannels);
						subscribedChannelsButMine = new JSONArray(sortedChannelsButMine);
						callback.success(subscribedChannels);
					}
					
					private List<String> sort(List<String> channels) {
						Collections.sort(channels, new Comparator<String>() {
							@Override
							public int compare(String lhs, String rhs) {
								try {
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
								} catch (JSONException e) {/*Do nothing*/}
								
								return rhs.compareTo(lhs);
							}
							
							private int getCounter(String channel, String key) throws JSONException {
								SyncModel sync = SyncModel.getInstance();
								return sync.get(context, channel) != null ? sync.get(context, channel).getInt(key) : 0;
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
	
	public JSONArray getAllButMine(Activity context) {
		return subscribedChannelsButMine;
	}
}
