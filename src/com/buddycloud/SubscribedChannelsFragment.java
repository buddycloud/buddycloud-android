package com.buddycloud;

import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SyncModel;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class SubscribedChannelsFragment extends Fragment {

	public final static String CHANNEL = "com.buddycloud.CHANNEL"; 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_subscribed, container, false);
		
		OnItemClickListener channelItemListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
					long arg3) {
				String channelJid = (String) adapterView.getItemAtPosition(position);
				showChannelFragment(channelJid);
				hideMenu();
			}
		};
		
		final SubscribedChannelsAdapter subscribed = new SubscribedChannelsAdapter(getActivity());
		final PersonalChannelAdapter personal = new PersonalChannelAdapter(getActivity());
		
		SyncModel.getInstance().refresh(getActivity(), new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				subscribed.syncd();
				personal.syncd();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
		
		ListView subscribedChannelsView = (ListView) view.findViewById(R.id.subscribedListView);
		subscribedChannelsView.setEmptyView(view.findViewById(R.id.subscribedProgress));
		subscribedChannelsView.setAdapter(subscribed);
		subscribedChannelsView.setOnItemClickListener(channelItemListener);
		
		ListView personalChannelView = (ListView) view.findViewById(R.id.personalListView);
		personalChannelView.setEmptyView(view.findViewById(R.id.subscribedProgress));
		personalChannelView.setAdapter(personal);
		personalChannelView.setOnItemClickListener(channelItemListener);
		
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
