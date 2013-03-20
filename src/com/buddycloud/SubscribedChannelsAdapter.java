package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.SyncModel;

public class SubscribedChannelsAdapter extends BaseAdapter {

	private final Activity parent;
	
	public SubscribedChannelsAdapter(Activity parent) {
		this.parent = parent;
		fetchCounters();
	}
	
	protected void fetchCounters() {
		SyncModel.getInstance().refresh(parent, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				fetchSubscribers();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void fetchSubscribers() {
		SubscribedChannelsModel.getInstance().refresh(parent, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				notifyDataSetChanged();
				fetchMetadata();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private void fetchMetadata() {
		JSONArray subscribedChannels = SubscribedChannelsModel.getInstance().get(parent);
		for (int i = 0; i < subscribedChannels.length(); i++) {
			String channel = subscribedChannels.optString(i);
			
			ChannelMetadataModel.getInstance().refresh(parent, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
					notifyDataSetChanged();
				}
				
				@Override
				public void error(Throwable throwable) {
					// TODO Auto-generated method stub
				}
			}, channel);
		}
	}

	@Override
	public int getCount() {
		return SubscribedChannelsModel.getInstance().get(parent).length();
	}

	@Override
	public Object getItem(int arg0) {
		return SubscribedChannelsModel.getInstance().get(parent).optString(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return SubscribedChannelsModel.getInstance().get(parent).optString(arg0).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {

		ViewHolder holder; 
		
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
			convertView = inflater.inflate(R.layout.subscriber_entry, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String channelJid = SubscribedChannelsModel.getInstance().get(parent).optString(position);
		// Title and description
		loadTitleAndDescription(holder, channelJid);
		// Avatar
		loadAvatar(holder, channelJid);
		// Counters
		loadCounters(holder, channelJid);
		
//		
//		SharedPreferences sharedPreferences = parent.getSharedPreferences(Preferences.PREFS_NAME, 0);
//		String myChannel = sharedPreferences.getString(Preferences.MY_CHANNEL_JID, null);
//		
//		if (subscribedChannel.getJid().equals(myChannel)) {
//			retView.setBackgroundColor(
//					viewGroup.getResources().getColor(R.color.bc_bg_grey));
//			retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
//					viewGroup.getResources().getColor(R.color.bc_orange));
//		} else {
//			if (subscribedChannel.getUnread() == 0) {
//				retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
//						viewGroup.getResources().getColor(R.color.bc_bg_grey));
//			} else {
//				retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
//						viewGroup.getResources().getColor(R.color.bc_green));
//			}
//		}
		
        return convertView;
	}
	
	private static class ViewHolder {
		TextView title;
		TextView description;
		TextView unreadCounter;
		SmartImageView avatar;
	}
	
	private ViewHolder fillHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.title = (TextView) view.findViewById(R.id.bcUserId);
		viewHolder.description = (TextView) view.findViewById(R.id.bcMessage);
		viewHolder.unreadCounter = (TextView) view.findViewById(R.id.unreadCounter);
		viewHolder.avatar = (SmartImageView) view.findViewById(R.id.bcProfilePic);
		return viewHolder;
	}
	
	private void loadTitleAndDescription(ViewHolder holder, String channelJid) {
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
	
	private void loadAvatar(ViewHolder holder, String channelJid) {
		String avatarURL = ChannelMetadataModel.getInstance().avatarURL(parent, channelJid);
		holder.avatar.setImageUrl(avatarURL, R.drawable.personal_50px);
	}
	
	private void loadCounters(ViewHolder holder, String channelJid) {
		JSONObject counters = SyncModel.getInstance().get(parent, channelJid);
		if (counters != null) {
			int totalCount = Integer.parseInt(counters.optString("totalCount"));
			if (totalCount > 30) {
				holder.unreadCounter.setText("30+");
			} else {
				holder.unreadCounter.setText("" + totalCount);
			}
			
		}
	}
}
