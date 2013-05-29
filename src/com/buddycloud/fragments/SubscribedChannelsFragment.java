package com.buddycloud.fragments;

import org.json.JSONObject;

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
import com.buddycloud.fragments.adapter.SubscribedChannelsAdapter;
import com.buddycloud.model.SyncModel;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class SubscribedChannelsFragment extends ContentFragment {

	private SubscribedChannelsAdapter adapter = new SubscribedChannelsAdapter();
	private GenericChannelsFragment genericChannelFrag = new GenericChannelsFragment(adapter) {
		@Override
		public void channelSelected(JSONObject channelItem) {
			selectChannel(channelItem.optString("jid"));
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		adapter.load(getActivity());
		return genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
	}

	private void selectChannel(String channelJid) {
		showChannelFragment(channelJid);
		SyncModel.getInstance().resetCounter(getActivity(), channelJid);
		adapter.sort(getActivity());
		adapter.notifyDataSetChanged();
		hideMenu();
	}

	private void hideMenu() {
		SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
		if (activity.getSlidingMenu().isMenuShowing()) {
			activity.getSlidingMenu().showContent();
		}
	}
	
	private void showChannelFragment(String channelJid) {
		MainActivity activity = (MainActivity) getActivity();
		activity.showChannelFragment(channelJid);
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

	public void syncd() {
		adapter.load(getActivity());
	}

}
