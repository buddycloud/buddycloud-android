package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.RecommendedChannelsModel;

public class RecommendedChannelsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "RECOMMENDED";
	private static final String RECOMMENDED = "RECOMMENDED";
	
	public RecommendedChannelsAdapter() {
		setCategoryOrder(RECOMMENDED);
	}
	
	public void load(final Context context) {
		RecommendedChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					final String channel = response.optString(i);
					addChannel(RECOMMENDED, createChannelItem(channel), context);
					notifyDataSetChanged();
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
}
