package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;

import android.content.Context;

public class SearchChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private final List<String> allChannels = new ArrayList<String>();
	private String myChannel;
	
	public SearchChannelsAdapter() {
		addCategory(PERSONAL);
		addCategory(SUBSCRIBED);
	}
	
	public void load(final Context context) {
		this.myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		SubscribedChannelsModel.getInstance().refresh(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					String channel = response.optString(i);
					allChannels.add(channel);
					filter("");
				}
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}

	public void filter(String q) {
		clearChannels();
		for (String channel : allChannels) {
			if (!channel.contains(q)) {
				continue;
			}
			if (!channel.equals(myChannel)) {
				addChannel(SUBSCRIBED, channel);
			} else {
				addChannel(PERSONAL, channel);
			}
		}
	}
}
