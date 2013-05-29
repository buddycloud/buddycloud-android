package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.MostActiveChannelsModel;

public class MostActiveChannelsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "MOST_ACTIVE";
	private static final String MOST_ACTIVE = "MOST ACTIVE";
	
	public MostActiveChannelsAdapter() {
		setCategoryOrder(MOST_ACTIVE);
	}
	
	public void load(final Context context) {
		MostActiveChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					final String channel = response.optString(i);
					addChannel(MOST_ACTIVE, createChannelItem(channel), context);
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
