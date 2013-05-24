package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.buddycloud.utils.ChannelAdapterHelper;

public abstract class GenericChannelAdapter extends BaseExpandableListAdapter {

	private List<String> categories = new ArrayList<String>();
	private Map<String, List<String>> channelsPerCategory = new HashMap<String, List<String>>();
	private Map<String, Integer> categoryOrder = new HashMap<String, Integer>(); 
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return channelsPerCategory.get(categories.get(groupPosition)).get(childPosition);
	}
	
	public void setCategoryOrder(String... categoryOrder) {
		for (int i = 0; i < categoryOrder.length; i++) {
			this.categoryOrder.put(categoryOrder[i], i);
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return getChild(groupPosition, childPosition).hashCode();
	}

	private void addCategory(String category) {
		if (channelsPerCategory.containsKey(category)) {
			return;
		}
		categories.add(category);
		channelsPerCategory.put(category, new ArrayList<String>());
		Collections.sort(categories, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return categoryOrder.get(lhs).compareTo(categoryOrder.get(rhs));
			}
		});
		notifyDataSetChanged();
	}
	
	protected void addChannel(String category, String channel) {
		addCategory(category);
		channelsPerCategory.get(category).add(channel);
		notifyDataSetChanged();
	}
	
	protected void clearChannels() {
		channelsPerCategory.clear();
		categories.clear();
		notifyDataSetChanged();
	}
	
	protected boolean hasChannel(String channel) {
		for (List<String> channels : channelsPerCategory.values()) {
			if (channels.contains(channel)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup viewGroup) {
		String channelJid = (String) getChild(groupPosition, childPosition);
		View returningView = ChannelAdapterHelper.createChannelMenuItem(
				viewGroup.getContext(), convertView, viewGroup, channelJid);
		return returningView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return channelsPerCategory.get(categories.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return categories.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return categories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return categories.get(groupPosition).hashCode();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup viewGroup) {
		String channelGroup = (String) getGroup(groupPosition);
		View returningView = ChannelAdapterHelper.createChannelGroupItem(
				viewGroup.getContext(), convertView, viewGroup, channelGroup);
		
		ExpandableListView listView = (ExpandableListView) viewGroup;
	    listView.expandGroup(groupPosition);
	    
		return returningView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
