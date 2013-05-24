package com.buddycloud.fragments.adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private String myChannel;
	
	public SubscribedChannelsAdapter() {
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
					if (!channel.equals(myChannel)) {
						addChannel(SUBSCRIBED, channel);
					} else {
						addChannel(PERSONAL, channel);
					}
				}
				fetchMetadata(context);
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void fetchMetadata(Context context) {
		JSONArray subscribedChannels = SubscribedChannelsModel.getInstance().get(context);
		for (int i = 0; i < subscribedChannels.length(); i++) {
			String channel = subscribedChannels.optString(i);
			ChannelMetadataModel.getInstance().refresh(context, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
					notifyDataSetChanged();
				}
				
				@Override
				public void error(Throwable throwable) {
					// TODO Auto-generated method stub
				}
			}, channel);
		}
	}
}