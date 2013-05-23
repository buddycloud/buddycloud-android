package com.buddycloud.utils;

import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.SyncModel;
import com.squareup.picasso.Picasso;

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
		loadCounters(holder, channelJid);
		
	    return convertView;
	}
	
	private static class ViewHolder {
		TextView title;
		TextView description;
		TextView unreadCounter;
		ImageView avatar;
	}
	
	private static ViewHolder fillHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.title = (TextView) view.findViewById(R.id.bcUserId);
		viewHolder.description = (TextView) view.findViewById(R.id.bcMessage);
		viewHolder.unreadCounter = (TextView) view.findViewById(R.id.unreadCounter);
		viewHolder.avatar = (ImageView) view.findViewById(R.id.bcProfilePic);
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
		String avatarURL = AvatarUtils.avatarURL(parent, channelJid);
		Picasso.with(parent).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(holder.avatar);
	}
	
	private static void loadCounters(ViewHolder holder, String channelJid) {
		JSONObject counters = SyncModel.getInstance().countersFromChannel(channelJid);
		if (counters != null) {
			Integer totalCount = counters.optInt("totalCount");
			if (totalCount > 30) {
				holder.unreadCounter.setText("30+");
			} else {
				holder.unreadCounter.setText(totalCount.toString());
			}
		}
	}
}
