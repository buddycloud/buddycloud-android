package com.buddycloud.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.buddycloud.R;

public abstract class GenericChannelsFragment extends SherlockFragment {

	public final static String CHANNEL = "com.buddycloud.CHANNEL";
	
	private SubscribedChannelsAdapter subscribed = new SubscribedChannelsAdapter();
	private PersonalChannelAdapter personal = new PersonalChannelAdapter();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_subscribed, container, false);
		
		OnItemClickListener channelItemListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
					long arg3) {
				String channelJid = (String) adapterView.getItemAtPosition(position);
				channelSelected(channelJid);
			}
		};
		
		final ListView subscribedChannelsView = (ListView) view.findViewById(R.id.subscribedListView);
		subscribedChannelsView.setEmptyView(view.findViewById(R.id.subscribedProgress));
		subscribedChannelsView.setAdapter(subscribed);
		subscribedChannelsView.setOnItemClickListener(channelItemListener);
		
		ListView personalChannelView = (ListView) view.findViewById(R.id.personalListView);
		personalChannelView.setEmptyView(view.findViewById(R.id.subscribedProgress));
		personalChannelView.setAdapter(personal);
		personalChannelView.setOnItemClickListener(channelItemListener);
		
		return view;
	}
	
	public void load(Context context) {
		subscribed.load(context);
		personal.load(context);
	}
	
	public abstract void channelSelected(String channelJid);

	public void filter(String q) {
		subscribed.setFilter(q);
		subscribed.notifyDataSetChanged();
	}
}
