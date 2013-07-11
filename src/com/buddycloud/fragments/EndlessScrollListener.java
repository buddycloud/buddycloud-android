package com.buddycloud.fragments;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class EndlessScrollListener implements OnScrollListener {

	private int visibleThreshold = 5;
	private int previousTotal = 0;
	private boolean loading = true;
	private ChannelStreamFragment fragment;

	public EndlessScrollListener(ChannelStreamFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
			}
		}
		if (!loading
				&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
			// I load the next page of gigs using a background task,
			// but you can call any function here.
			fragment.fillMore();
			loading = true;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
