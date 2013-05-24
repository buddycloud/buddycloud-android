package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buddycloud.utils.ChannelAdapterHelper;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

public abstract class GenericChannelAdapter extends BaseExpandableListAdapter {

	private List<String> categories = new ArrayList<String>();
	private Map<String, List<String>> channelsPerCategory = new HashMap<String, List<String>>();
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return channelsPerCategory.get(categories.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return getChild(groupPosition, childPosition).hashCode();
	}

	protected void addCategory(String category) {
		categories.add(category);
		channelsPerCategory.put(category, new ArrayList<String>());
		notifyDataSetChanged();
	}
	
	protected void addChannel(String category, String channel) {
		channelsPerCategory.get(category).add(channel);
		notifyDataSetChanged();
	}
	
	protected void clearChannels() {
		for (List<String> channels : channelsPerCategory.values()) {
			channels.clear();
		}
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
