package com.buddycloud;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PostDetailsFragment extends Fragment {

	public static final String POST_ID = "com.buddycloud.POST_ID";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_post_details, container, false);
		final String channelJid = getArguments().getString(POST_ID);
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.VISIBLE);
		
		return view;
	}
	
}
