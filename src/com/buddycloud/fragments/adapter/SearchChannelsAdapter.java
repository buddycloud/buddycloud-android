package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SearchChannelsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;

public class SearchChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private static final String METADATA_SEARCH = "METADATA SEARCH";
	private static final String CONTENT_SEARCH = "CONTENT SEARCH";
	
	private static final int SEARCH_THRESHOLD = 5;
	
	private final List<String> allSubscribedChannels = new ArrayList<String>();
	private String myChannel;
	
	public SearchChannelsAdapter() {
		setCategoryOrder(PERSONAL, SUBSCRIBED, METADATA_SEARCH, CONTENT_SEARCH);
	}
	
	public void load(final Context context) {
		this.myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		SubscribedChannelsModel.getInstance().refresh(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					String channel = response.optString(i);
					allSubscribedChannels.add(channel);
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

	@SuppressLint("DefaultLocale")
	public void filter(final Context context, String q) {
		clearChannels();
		int matchedChannels = 0;
		for (String channel : allSubscribedChannels) {
			String plainMetadata = getPlainMetadata(context, channel);
			String lowerQ = low(q);
			if (!low(channel).contains(lowerQ) 
					&& !low(plainMetadata).contains(lowerQ)) {
				continue;
			}
			if (!channel.equals(myChannel)) {
				addChannel(SUBSCRIBED, createChannelItem(channel));
			} else {
				addChannel(PERSONAL, createChannelItem(channel));
			}
			matchedChannels++;
		}
		if (matchedChannels <= SEARCH_THRESHOLD && q.length() > 0) {
			search(context, q, SearchChannelsModel.METADATA_TYPE, METADATA_SEARCH);
			search(context, q, SearchChannelsModel.POST_TYPE, CONTENT_SEARCH);
		}
	}

	private static String low(String q) {
		return q.toLowerCase(Locale.US);
	}

	@SuppressWarnings("unchecked")
	private String getPlainMetadata(final Context context, String channel) {
		JSONObject metadata = ChannelMetadataModel.getInstance().get(context, channel);
		if (metadata == null) {
			return "";
		}
		Iterator<String> keys = metadata.keys();
		StringBuilder allValues = new StringBuilder();
		while (keys.hasNext()) {
			String key = keys.next();
			allValues.append(metadata.optString(key));
		}
		return allValues.toString();
	}

	private void search(final Context context, String q, String type, final String category) {
		SearchChannelsModel.getInstance().refresh(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					JSONObject channelItem = response.optJSONObject(i);
					String channelJid = channelItem.optString("jid");
					if (!hasChannel(channelJid)) {
						addChannel(category, channelItem);
						fetchMetadata(context, channelJid);
					}
				}
			}

			@Override
			public void error(Throwable throwable) {
				
			}
		}, type, q);
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
