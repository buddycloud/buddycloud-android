package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SearchChannelsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;

public class SearchChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private static final String SEARCH = "SEARCH";
	
	private static final String METADATA_TYPE = "metadata";
	private static final int SEARCH_THRESHOLD = 5;
	
	private final List<String> allChannels = new ArrayList<String>();
	private String myChannel;
	
	public SearchChannelsAdapter() {
		setCategoryOrder(PERSONAL, SUBSCRIBED, SEARCH);
	}
	
	public void load(final Context context) {
		this.myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		SubscribedChannelsModel.getInstance().refresh(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					String channel = response.optString(i);
					allChannels.add(channel);
					filter(context, "");
				}
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}

	public void filter(final Context context, String q) {
		clearChannels();
		int matchedChannels = 0;
		for (String channel : allChannels) {
			if (!channel.contains(q)) {
				continue;
			}
			if (!channel.equals(myChannel)) {
				addChannel(SUBSCRIBED, channel);
			} else {
				addChannel(PERSONAL, channel);
			}
			matchedChannels++;
		}
		if (matchedChannels <= SEARCH_THRESHOLD && q.length() > 0) {
			SearchChannelsModel.getInstance().refresh(context, new ModelCallback<JSONArray>() {
				@Override
				public void success(JSONArray response) {
					for (int i = 0; i < response.length(); i++) {
						String channel = response.optString(i);
						if (!hasChannel(channel)) {
							addChannel(SEARCH, channel);
							fetchMetadata(context, channel);
						}
					}
				}

				@Override
				public void error(Throwable throwable) {
					
				}
			}, METADATA_TYPE, q);
		}
	}
	
	private void fetchMetadata(Context context, String channel) {
		ChannelMetadataModel.getInstance().refresh(context,
				new ModelCallback<JSONObject>() {
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
