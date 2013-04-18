package com.buddycloud.utils;

import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.SyncModel;

public class SubscribedChannelUtils {

	public static View createSubscribedChannelMenuItem(Activity parent, View convertView,
			ViewGroup viewGroup, String channelJid, boolean isScrolling) {
		ViewHolder holder = null; 
		boolean firstLoad = convertView == null; 
		
		if (firstLoad) {
			LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
			convertView = inflater.inflate(R.layout.subscriber_entry, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Title and description
		loadTitleAndDescription(parent, holder, channelJid);
		
		// Load avatar
		loadAvatar(parent, holder, channelJid, isScrolling);
		
		// Counters
		loadCounters(parent, holder, channelJid);
		
	    return convertView;
	}
	
	private static class ViewHolder {
		TextView title;
		TextView description;
		TextView unreadCounter;
		SmartImageView avatar;
	}
	
	private static ViewHolder fillHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.title = (TextView) view.findViewById(R.id.bcUserId);
		viewHolder.description = (TextView) view.findViewById(R.id.bcMessage);
		viewHolder.unreadCounter = (TextView) view.findViewById(R.id.unreadCounter);
		viewHolder.avatar = (SmartImageView) view.findViewById(R.id.bcProfilePic);
		return viewHolder;
	}
	
	private static void loadTitleAndDescription(Activity parent, ViewHolder holder, String channelJid) {
		String channelTitle = channelJid;
		String channelDescription = null;
		JSONObject metadata = ChannelMetadataModel.getInstance().get(parent, channelJid);
		
		if (metadata != null) {
			channelTitle = metadata.optString("title");
			channelDescription = metadata.optString("description");
		}
		
		holder.title.setText(channelTitle);
		holder.description.setText(channelDescription);
	}
	
	private static void loadAvatar(Activity parent, ViewHolder holder, String channelJid, 
			boolean isScrolling) {
		if (!isScrolling) {
			String avatarURL = AvatarUtils.avatarURL(parent, channelJid);
			holder.avatar.setImageUrl(avatarURL, R.drawable.personal_50px);
		} else {
			holder.avatar.setImageResource(R.drawable.personal_50px);
		}
	}
	
	private static void loadCounters(Activity parent, ViewHolder holder, String channelJid) {
		JSONObject counters = SyncModel.getInstance().get(parent, channelJid);
		if (counters != null) {
			Integer totalCount = Integer.parseInt(counters.optString("totalCount"));
			if (totalCount > 30) {
				holder.unreadCounter.setText("30+");
			} else {
				holder.unreadCounter.setText(totalCount.toString());
			}
		}
	}
}
