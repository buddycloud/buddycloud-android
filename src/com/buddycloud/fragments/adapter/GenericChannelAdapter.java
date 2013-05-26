package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.buddycloud.utils.ChannelAdapterHelper;

public abstract class GenericChannelAdapter extends BaseExpandableListAdapter {

	private List<String> categories = new ArrayList<String>();
	private Map<String, List<JSONObject>> channelsPerCategory = new HashMap<String, List<JSONObject>>();
	private Map<String, Integer> categoryOrder = new HashMap<String, Integer>(); 
	
	@Override
	public JSONObject getChild(int groupPosition, int childPosition) {
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
		channelsPerCategory.put(category, new ArrayList<JSONObject>());
		Collections.sort(categories, new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return categoryOrder.get(lhs).compareTo(categoryOrder.get(rhs));
			}
		});
		notifyDataSetChanged();
	}
	
	protected void addChannel(String category, JSONObject channelItem) {
		addCategory(category);
		channelsPerCategory.get(category).add(channelItem);
		notifyDataSetChanged();
	}
	
	protected void clearChannels() {
		channelsPerCategory.clear();
		categories.clear();
		notifyDataSetChanged();
	}
	
	protected boolean hasChannel(String channelJid) {
		for (List<JSONObject> channels : channelsPerCategory.values()) {
			for (JSONObject channel : channels) {
				if (channel.optString("jid").equals(channelJid)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public View getChildView(final int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup viewGroup) {
		JSONObject channelItem = getChild(groupPosition, childPosition);
		String channelJid = channelItem.optString("jid");
		View returningView = ChannelAdapterHelper.createChannelMenuItem(
				viewGroup.getContext(), convertView, viewGroup, channelJid);
		return returningView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return channelsPerCategory.get(categories.get(groupPosition)).size();
	}

	public static JSONObject createChannelItem(String jid) {
		Map<String, String> props = new HashMap<String, String>();
		props.put("jid", jid);
		return new JSONObject(props);
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
