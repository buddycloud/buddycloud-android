package com.buddycloud.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class SearchChannelsFragment extends SherlockFragment {

	private GenericChannelsFragment genericChannelFrag = new GenericChannelsFragment() {
		@Override
		public void channelSelected(String channelJid) {
			selectChannel(channelJid);
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
	}
	
	private void selectChannel(String channelJid) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		getActivity().setResult(0, returnIntent);
		getActivity().finish();
	}

	public void load(Context context) {
		genericChannelFrag.load(context);
	}

	public void filter(String q) {
		genericChannelFrag.filter(q);
	}
}
