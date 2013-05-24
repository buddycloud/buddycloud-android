package com.buddycloud.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.buddycloud.fragments.adapter.SearchChannelsAdapter;

public class SearchChannelsFragment extends SherlockFragment {

	private SearchChannelsAdapter adapter = new SearchChannelsAdapter();
	private GenericChannelsFragment genericChannelFrag = new GenericChannelsFragment(adapter) {
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
		adapter.load(context);
	}

	public void filter(String q) {
		adapter.filter(q);
	}
}
