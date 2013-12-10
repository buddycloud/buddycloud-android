package com.buddycloud.fragments;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.buddycloud.R;
import com.buddycloud.fragments.adapter.SearchChannelsAdapter;

public class SearchChannelsFragment extends SherlockFragment {

	public static final String FILTER = "com.buddycloud.FILTER";
	public static final String AFFILIATIONS = "com.buddycloud.AFFILIATIONS";
	
	private IBinder windowToken;
	private String lastFilter;
	private String initialFilter;
	private SearchChannelsAdapter adapter = new SearchChannelsAdapter();
	
	private GenericChannelsFragment genericChannelFrag = new GenericChannelsFragment(adapter) {
		
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = super.onCreateView(inflater, container, savedInstanceState);
			final ExpandableListView channelsView = (ExpandableListView) view.findViewById(R.id.channelListView);
			channelsView.setEmptyView(null);
			view.findViewById(R.id.channelListProgress).setVisibility(View.GONE);
			adapter.configure(genericChannelFrag, view);
			return view;
		};
		
		@Override
		public void channelSelected(JSONObject channelItem) {
			if (channelItem.has("post_id")) {
				selectItem(channelItem.optString("post_id"), 
						channelItem.optString("jid"));
			} else {
				selectChannel(channelItem.optString("jid"));
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
		adapter.load(container.getContext());
		if (initialFilter != null) {
			filter(getActivity(), initialFilter);
		}
		return view;
	}
	
	protected void selectItem(String itemId, String channelJid) {
		hideKeyboard();
		finishActivity(itemId, channelJid);
	}

	private void selectChannel(String channelJid) {
		hideKeyboard();
		finishActivity(null, channelJid);
	}

	private void finishActivity(String itemId, String channelJid) {
		Intent returnIntent = new Intent();
		returnIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		returnIntent.putExtra(GenericChannelsFragment.POST_ID, itemId);
		returnIntent.putExtra(SearchChannelsFragment.FILTER, lastFilter);
		getActivity().setResult(0, returnIntent);
		getActivity().finish();
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(windowToken, 0);
	}

	public void filter(Context context, String q) {
		lastFilter = q;
		adapter.filter(context, q);
	}

	public void setWindowToken(IBinder windowToken) {
		this.windowToken = windowToken;
	}

	public void setInitialFilter(String q) {
		initialFilter = q;
	}

	public void setAffiliationsToDisplay(String[] affiliationsToDisplay) {
		adapter.enableRemoteSearch(false);
		for (String affiliation : affiliationsToDisplay) {
			adapter.addAffiliationToDisplay(affiliation);
		}
	}
}
