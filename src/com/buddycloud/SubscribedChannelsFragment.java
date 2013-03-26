package com.buddycloud;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class SubscribedChannelsFragment extends Fragment {

	public final static String CHANNEL = "com.buddycloud.CHANNEL"; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_subscribed, container, false);
		
		ListView contentView = (ListView) view.findViewById(R.id.contentListView);
		contentView.setEmptyView(view.findViewById(R.id.subscribedProgress));
		contentView.setAdapter(new SubscribedChannelsAdapter(getActivity()));
		contentView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
					long arg3) {
				String channelJid = (String) adapterView.getItemAtPosition(position);
				showChannelFragment(channelJid);
				hideMenu();
			}
		});
		return view;
	}

	private void hideMenu() {
		SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
		if (activity.getSlidingMenu().isMenuShowing()) {
			activity.getSlidingMenu().showContent();
		}
	}
	
	private void showChannelFragment(String channelJid) {
		Fragment frag = new ChannelStreamFragment();
		Bundle args = new Bundle();
		args.putString(CHANNEL, channelJid);
		frag.setArguments(args);
		MainActivity activity = (MainActivity) getActivity();
		activity.setLeftFragment(frag);
	}

}
