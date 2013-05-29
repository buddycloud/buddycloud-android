package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SimilarChannelsModel;

public class SimilarChannelsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "SIMILAR";
	private static final String SIMILAR = "SIMILAR";
	private String channelJid;
	
	public SimilarChannelsAdapter(String channelJid) {
		this.channelJid = channelJid;
		setCategoryOrder(SIMILAR);
	}
	
	public void load(final Context context) {
		SimilarChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					final String channel = response.optString(i);
					addChannel(SIMILAR, createChannelItem(channel), context);
					notifyDataSetChanged();
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		}, channelJid);
	}
}
