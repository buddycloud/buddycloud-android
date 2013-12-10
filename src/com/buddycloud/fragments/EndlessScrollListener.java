package com.buddycloud.fragments;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class EndlessScrollListener implements OnScrollListener {

	private static final int VISIBLE_THRESHOLD = 10;
	private boolean loading;
	private ChannelStreamFragment fragment;

	public EndlessScrollListener(ChannelStreamFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount + VISIBLE_THRESHOLD >= totalItemCount;
		if (loadMore && !loading) {
			fragment.fillMore();
		}
	}
	
	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
