package com.buddycloud.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.SearchActivity;
import com.buddycloud.model.SyncModel;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class SubscribedChannelsFragment extends ContentFragment {

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
	
	public void syncd(Context context) {
		genericChannelFrag.load(context);
	}
	
	private void selectChannel(String channelJid) {
		showChannelFragment(channelJid);
		hideMenu();
		SyncModel.getInstance().selectChannel(getActivity(), channelJid);
	}

	private void hideMenu() {
		SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
		if (activity.getSlidingMenu().isMenuShowing()) {
			activity.getSlidingMenu().showContent();
		}
	}
	
	private void showChannelFragment(String channelJid) {
		MainActivity activity = (MainActivity) getActivity();
		activity.showChannelFragment(channelJid).syncd(activity);
	}

	@Override
	public void attached() {
		getSherlockActivity().getSupportActionBar().setTitle("");
	}

	@Override
	public void createOptions(Menu menu) {
		getSherlockActivity().getSupportMenuInflater().inflate(
				R.menu.subscribed_fragment_options, menu);
	}

	@Override
	public boolean menuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.menu_search) {
			Intent searchActivityIntent = new Intent();
			searchActivityIntent.setClass(getActivity(), SearchActivity.class);
			getActivity().startActivityForResult(
					searchActivityIntent, SearchActivity.REQUEST_CODE);
			return true;
		}
		
		return false;
	}

}
