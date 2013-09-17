package com.buddycloud.fragments;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.buddycloud.fragments.adapter.GenericChannelAdapter;

public class GenericSelectableChannelsFragment extends SherlockFragment {

	private GenericChannelAdapter adapter;
	private GenericChannelsFragment genericChannelFrag;
	
	public void setAdapter(GenericChannelAdapter adapter) {
		this.adapter = adapter;
		this.genericChannelFrag = new GenericChannelsFragment(adapter) {
			@Override
			public void channelSelected(JSONObject channelItem) {
				selectChannel(channelItem.optString("jid"));
			}
		};
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
		adapter.configure(this, view);
		adapter.load(container.getContext());
		return view;
	}
	
	private void selectChannel(String channelJid) {
		finishActivity(channelJid);
	}

	private void finishActivity(String channelJid) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		getActivity().setResult(0, returnIntent);
		getActivity().finish();
	}
}
