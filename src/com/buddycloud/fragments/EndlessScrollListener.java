package com.buddycloud.fragments;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class EndlessScrollListener implements OnScrollListener {

	private static final int VISIBLE_THRESHOLD = 10;
	private boolean loading;
	private boolean refreshing;
	private ChannelStreamFragment fragment;

	public EndlessScrollListener(ChannelStreamFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		boolean loadMore = firstVisibleItem + visibleItemCount + VISIBLE_THRESHOLD >= totalItemCount;
		if (loadMore && !loading && !refreshing) {
			fragment.fillMore();
		}
		
		SwipeRefreshLayout refreshLayout = fragment.getSwipeRefreshLayout();
		if (firstVisibleItem == 0 && refreshLayout != null) {
			refreshLayout.setEnabled(true);
		} else {
			refreshLayout.setEnabled(false);
		}
	}
	
	public void setLoading(boolean loading) {
		this.loading = loading;
	}
	
	public void setRefreshing(boolean refreshing) {
		this.refreshing = refreshing;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
}
