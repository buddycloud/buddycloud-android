package com.buddycloud.fragments;

import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.SubscribedChannelUtils;

public class PersonalChannelAdapter extends BaseAdapter {

	private String myChannelJid;
	
	@Override
	public int getCount() {
		return myChannelJid == null ? 0 : 1;
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
				viewGroup.getContext(), convertView, viewGroup, myChannelJid);
	}

	public void load(Context context) {
		this.myChannelJid = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		notifyDataSetChanged();
		fetchMetadata(context);
	}
	
	private void fetchMetadata(Context context) {
		ChannelMetadataModel.getInstance().refresh(
				context, new ModelCallback<JSONObject>() {
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
