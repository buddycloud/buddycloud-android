package com.buddycloud.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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
	private IBinder windowToken;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
		adapter.load(container.getContext());
		return view;
	}
	
	private void selectChannel(String channelJid) {
		hideKeyboard();
		finishActivity(channelJid);
	}

	private void finishActivity(String channelJid) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		getActivity().setResult(0, returnIntent);
		getActivity().finish();
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(windowToken, 0);
	}

	public void filter(Context context, String q) {
		adapter.filter(context, q);
	}

	public void setWindowToken(IBinder windowToken) {
		this.windowToken = windowToken;
	}
}
