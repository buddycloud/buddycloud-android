package com.buddycloud.utils;

import java.util.EmptyStackException;
import java.util.Stack;

import android.content.Intent;
import android.os.Bundle;

import com.buddycloud.MainActivity;
import com.buddycloud.SearchActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.SearchChannelsFragment;

public class Backstack {
	
	private static final String TYPE = "type";
	private static final String ARGS = "args";
	
	private static final String TYPE_CHANNEL_STREAM = "CHANNEL_STREAM";
	private static final String TYPE_SEARCH = "SEARCH";
	
	private Stack<Bundle> backStack = new Stack<Bundle>();
	private MainActivity activity;

	public Backstack(MainActivity activity) {
		this.activity = activity;
	}
	
	public boolean pop() {
		try {
			Bundle pop = backStack.pop();
			process(pop);
			return true;
		} catch (EmptyStackException e) {
			return false;
		}
	}
	
	public void clear() {
		backStack.clear();
	}
	
	private void process(Bundle stackBundle) {
		Bundle args = stackBundle.getBundle(ARGS);
		if (stackBundle.getString(TYPE).equals(TYPE_CHANNEL_STREAM)) {
			popChannel(args);
		} else if (stackBundle.getString(TYPE).equals(TYPE_SEARCH)) {
			popSearch(args);
		}
	}

	private void popSearch(Bundle args) {
		Intent searchActivityIntent = new Intent();
		searchActivityIntent.putExtra(SearchChannelsFragment.FILTER, 
				args.getString(SearchChannelsFragment.FILTER));
		searchActivityIntent.setClass(activity, SearchActivity.class);
		activity.startActivityForResult(
				searchActivityIntent, SearchActivity.REQUEST_CODE);
	}

	private void popChannel(Bundle args) {
		activity.showChannelFragment(
				args.getString(GenericChannelsFragment.CHANNEL));
	}

	public void pushChannel(String channelJid) {
		Bundle args = new Bundle();
		args.putString(GenericChannelsFragment.CHANNEL, channelJid);
		push(TYPE_CHANNEL_STREAM, args);
	}
	
	public void pushSearch(String filter) {
		Bundle args = new Bundle();
		args.putString(SearchChannelsFragment.FILTER, filter);
		push(TYPE_SEARCH, args);
	}

	private void push(String type, Bundle args) {
		Bundle stackBundle = new Bundle();
		stackBundle.putString(TYPE, type);
		stackBundle.putBundle(ARGS, args);
		backStack.push(stackBundle);
	}
}
