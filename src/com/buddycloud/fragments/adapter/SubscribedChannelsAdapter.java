package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
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
		SubscribedChannelsModel.getInstance().getAsync(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				clear();
				for (int i = 0; i < response.length(); i++) {
					String channel = response.optString(i);
					if (!channel.equals(myChannel)) {
						addChannel(SUBSCRIBED, createChannelItem(channel));
					} else {
						addChannel(PERSONAL, createChannelItem(channel));
					}
				}
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
}