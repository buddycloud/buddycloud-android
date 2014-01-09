package com.buddycloud.utils;

import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.SyncModel;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChannelAdapterHelper {

	public static View createChannelGroupItem(Context context, View convertView,
			ViewGroup viewGroup, String channelGroup) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
		convertView = inflater.inflate(R.layout.channel_group_header, viewGroup, false);
		
		TextView groupHeader = (TextView) convertView.findViewById(R.id.groupHeader);
		groupHeader.setText(channelGroup);
		
		return convertView;
	}
	
	public static View createChannelMenuItem(Context context, View convertView,
			ViewGroup viewGroup, String channelJid) {
		ViewHolder holder = null; 
		boolean firstLoad = convertView == null; 
		
		if (firstLoad) {
			LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
			convertView = inflater.inflate(R.layout.channel_item, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Title and description
		loadTitleAndDescription(context, holder, channelJid);
		
		// Load avatar
		loadAvatar(context, holder, channelJid);
		
		// Counters
		loadCounters(context, holder, channelJid);
		
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
	
	private static void loadTitleAndDescription(Context parent, final ViewHolder holder, final String channelJid) {
		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(parent, channelJid);
		
		String channelTitle = channelJid;
		String channelDescription = null;
		
		if (metadata != null) {
			channelTitle = metadata.optString("title");
			channelDescription = metadata.optString("description");
		}
		
		holder.title.setText(channelTitle);
		holder.description.setText(channelDescription);
		
		
	}

	private static void loadAvatar(Context parent, ViewHolder holder, String channelJid) {
		String avatarURL = AvatarUtils.avatarURL(parent, channelJid);
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.showImageOnFail(R.drawable.personal_50px)
				.showImageOnLoading(R.drawable.personal_50px)
				.build();
		
		ImageLoader.getInstance().displayImage(avatarURL, holder.avatar, dio);
	}
	
	private static void loadCounters(Context context, final ViewHolder holder, final String channelJid) {
		JSONObject allCounters = SyncModel.getInstance().getFromCache(context, channelJid);
		JSONObject counters = allCounters.optJSONObject(channelJid);
		if (counters != null) {
			Integer totalCount = counters.optInt("totalCount");
			if (totalCount > 30) {
				holder.unreadCounter.setText("30+");
			} else {
				holder.unreadCounter.setText(totalCount.toString());
			}
		} else {
			holder.unreadCounter.setText("0");
		}
	}
}
