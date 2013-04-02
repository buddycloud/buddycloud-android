package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.utils.SubscribedChannelUtils;

public class SubscribedChannelsAdapter extends BaseAdapter {

	private final Activity parent;
	
	public SubscribedChannelsAdapter(Activity parent) {
		this.parent = parent;
	}
	
	public void syncd() {
		fetchSubscribers();
	}
	
	private void fetchSubscribers() {
		SubscribedChannelsModel.getInstance().refresh(parent, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				notifyDataSetChanged();
				fetchMetadata();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void fetchMetadata() {
		JSONArray subscribedChannels = SubscribedChannelsModel.getInstance().get(parent);
		for (int i = 0; i < subscribedChannels.length(); i++) {
			String channel = subscribedChannels.optString(i);
			
			ChannelMetadataModel.getInstance().refresh(parent, new ModelCallback<JSONObject>() {
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

	@Override
	public int getCount() {
		return SubscribedChannelsModel.getInstance().getAllButMine(parent).length();
	}

	@Override
	public Object getItem(int arg0) {
		return SubscribedChannelsModel.getInstance().getAllButMine(parent).optString(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return SubscribedChannelsModel.getInstance().getAllButMine(parent).optString(arg0).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		String channelJid = (String) getItem(position);
		return SubscribedChannelUtils.createSubscribedChannelMenuItem(
				parent, convertView, viewGroup, channelJid);
	}
	
}
