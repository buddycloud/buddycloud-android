package com.buddycloud.fragments;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.SubscribedChannelUtils;

public class SubscribedChannelsAdapter extends BaseAdapter {

	private List<String> allChannels = new ArrayList<String>();
	private List<String> filteredChannels = new ArrayList<String>();
	
	public void load(Context context) {
		fetchSubscribers(context);
	}
	
	private void fetchSubscribers(final Context context) {
		SubscribedChannelsModel.getInstance().refresh(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				notifyDataSetChanged();
				fetchMetadata(context);
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	public void setFilter(String filter) {
		filteredChannels.clear();
		for (String channel : allChannels) {
			if (channel.contains(filter)) {
				filteredChannels.add(channel);
			}
		}
		notifyDataSetChanged();
	}
	
	private void fetchMetadata(Context context) {
		JSONArray subscribedChannels = SubscribedChannelsModel.getInstance().get(context);
		allChannels.clear();
		filteredChannels.clear();
		for (int i = 0; i < subscribedChannels.length(); i++) {
			String channel = subscribedChannels.optString(i);
			String myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
			if (!channel.equals(myChannel)) {
				allChannels.add(channel);
			}
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
		filteredChannels.addAll(allChannels);
	}

	@Override
	public int getCount() {
		return filteredChannels.size();
	}

	@Override
	public Object getItem(int arg0) {
		return filteredChannels.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return filteredChannels.get(arg0).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		String channelJid = (String) getItem(position);
		View returningView = SubscribedChannelUtils.createSubscribedChannelMenuItem(
				viewGroup.getContext(), convertView, viewGroup, channelJid);
		return returningView;
	}
}
