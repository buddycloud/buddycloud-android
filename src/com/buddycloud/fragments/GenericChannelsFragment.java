package com.buddycloud.fragments;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.buddycloud.R;
import com.buddycloud.fragments.adapter.GenericChannelAdapter;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.ChannelAdapterHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public abstract class GenericChannelsFragment extends SherlockFragment {

	public final static String CHANNEL = "com.buddycloud.CHANNEL";
	public static final String POST_ID = "com.buddycloud.ITEM";
	public final static String INPUT_ARGS = "com.buddycloud.INPUT_ARGS";
	
	private final GenericChannelAdapter adapter;
	
	public GenericChannelsFragment(GenericChannelAdapter adapter) {
		this.adapter = adapter;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.channel_list, container, false);
		
		OnChildClickListener channelItemListener = new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				JSONObject channelItem = (JSONObject) adapter.getChild(groupPosition, childPosition);
				channelSelected(channelItem);
				return true;
			}
		};
		
		ExpandableListView channelsView = (ExpandableListView) view.findViewById(R.id.channelListView);
		channelsView.setEmptyView(view.findViewById(R.id.channelListProgress));
		channelsView.setAdapter(adapter);
		channelsView.setOnChildClickListener(channelItemListener);
		
		PauseOnScrollListener listener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
		channelsView.setOnScrollListener(listener);
		expandAll(view);
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ImageLoader.getInstance().resume();
	}

	@Override
	public void onStop() {
		super.onStop();
		ImageLoader.getInstance().stop();
	}
	
	private void expandAll(View view) {
		ExpandableListView listView = (ExpandableListView) view.findViewById(
				R.id.channelListView);
		int count = adapter.getGroupCount();
		for (int position = 0; position < count; position++) {
			listView.expandGroup(position);
		}
	}
	
	public abstract void channelSelected(JSONObject channelItem);
}
