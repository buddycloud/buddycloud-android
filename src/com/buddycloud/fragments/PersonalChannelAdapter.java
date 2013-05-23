package com.buddycloud.fragments;

import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.SubscribedChannelUtils;

public class PersonalChannelAdapter extends BaseAdapter {

	private final Activity parent;
	private String myChannelJid;
	
	public PersonalChannelAdapter(Activity parent) {
		this.parent = parent;
		this.myChannelJid = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);
	}
	
	@Override
	public int getCount() {
		return Math.min(1, SubscribedChannelsModel.getInstance().get(parent).length());
	}

	@Override
	public Object getItem(int arg0) {
		return myChannelJid;
	}

	@Override
	public long getItemId(int arg0) {
		return myChannelJid.hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		return SubscribedChannelUtils.createSubscribedChannelMenuItem(
				parent, convertView, viewGroup, myChannelJid, false);
	}

	public void syncd() {
		notifyDataSetChanged();
		fetchMetadata();
	}
	
	private void fetchMetadata() {
		ChannelMetadataModel.getInstance().refresh(
				parent, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		}, myChannelJid);
	}
}
