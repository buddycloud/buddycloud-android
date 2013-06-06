package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;

import com.buddycloud.model.FollowersModel;
import com.buddycloud.model.ModelCallback;

public class FollowersAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "FOLLOWERS";
	private static final String FOLLOWERS = "FOLLOWERS";
	private String channelJid;
	
	public FollowersAdapter(String channelJid) {
		this.channelJid = channelJid;
		setCategoryOrder(FOLLOWERS);
	}
	
	public void load(final Context context) {
		FollowersModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					final String channel = response.optString(i);
					addChannel(FOLLOWERS, createChannelItem(channel), context);
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
