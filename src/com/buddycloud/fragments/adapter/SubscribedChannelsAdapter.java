package com.buddycloud.fragments.adapter;

import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private String myChannel;
	
	public SubscribedChannelsAdapter() {
		setCategoryOrder(PERSONAL, SUBSCRIBED);
	}
	
	public void load(final Context context) {
		this.myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		JSONArray response = SubscribedChannelsModel.getInstance().getFromCache(context);
		clear();
		for (int i = 0; i < response.length(); i++) {
			String channel = response.optString(i);
			if (!channel.equals(myChannel)) {
				addChannel(SUBSCRIBED, createChannelItem(channel), context);
			} else {
				addChannel(PERSONAL, createChannelItem(channel), context);
			}
		}
		sort(context);
		notifyDataSetChanged();
	}
	
	public void sort(final Context context) {
		final JSONObject allCounters = SyncModel.getInstance().getFromCache(context);
		sort(new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject lhs, JSONObject rhs) {
				
				int countA = getCounter(allCounters, lhs.optString("jid"), "mentionsCount");
				int countB = getCounter(allCounters, rhs.optString("jid"), "mentionsCount");
				int diff = countB - countA;
				
				if (diff == 0) {
					countA = getCounter(allCounters, lhs.optString("jid"), "totalCount");
					countB = getCounter(allCounters, rhs.optString("jid"), "totalCount");
					diff = countB - countA;
				}
				
				if (diff != 0) {
					return diff;
				}
				
				return rhs.optString("jid").compareTo(lhs.optString("jid"));
			}
		});
	}
	
	private int getCounter(JSONObject allCounters, String channel, String key) {
		JSONObject channelCounters = allCounters.optJSONObject(channel);
		if (channelCounters == null) {
			return 0;
		}
		return channelCounters.optInt(key);
	}
}