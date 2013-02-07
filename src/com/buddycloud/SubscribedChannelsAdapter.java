package com.buddycloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.http.ProfilePicCache;
import com.buddycloud.model.Channel;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsAdapter extends BaseAdapter {

	private static final int MAX_COUNTER = 50;
	private static final double AVATAR_DIP = 75.;
	
	private List<Channel> subscribed = new ArrayList<Channel>();
	private ProfilePicCache picCache = new ProfilePicCache();
	private final Activity parent;
	
	public SubscribedChannelsAdapter(Activity parent) {
		this.parent = parent;
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				fetchSubscribers();
				return null;
			}
			
		}.execute();
	}
	
	@SuppressWarnings("unchecked")
	private void fetchSubscribers() {
		subscribed.clear();
		
		String apiAddress = Preferences.getPreference(parent, Preferences.API_ADDRESS);

		String lastUpdate = Preferences.getPreference(parent, Preferences.LAST_UPDATE);
		if (lastUpdate == null) {
			lastUpdate = Preferences.DEFAUL_LAST_UPDATE;
		}
		JSONObject syncObject = BuddycloudHTTPHelper.get(apiAddress + 
				"/sync?since=" + lastUpdate + "&max=" + (MAX_COUNTER + 1) + 
				"&counters=true", true, parent);
			
		Preferences.setPreference(parent, Preferences.LAST_UPDATE, 
				Preferences.ISO_8601.format(new Date()));
		
		JSONObject jsonObject = BuddycloudHTTPHelper.get(
				apiAddress + "/subscribed", true, parent);
		
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String nodeJid = (String) keys.next();
			if (!nodeJid.endsWith("/posts")) {
				continue;
			}
			String channelJid = nodeJid.split("/")[0];
			Channel channel = new Channel(channelJid);
			if (!subscribed.contains(channel)) {
				channel.setUnread(syncObject.optInt("/user/" + channelJid + "/posts"));
				channel.setAvatar(fetchAvatar(channelJid, apiAddress));
				channel.setDescription(fetchDescription(channelJid, apiAddress));
				notifyChanged(channel);
			}
		}
	}

	private void notifyChanged(final Channel channel) {
		parent.findViewById(R.id.contentListView).post(new Runnable() {
			@Override
			public void run() {
				subscribed.add(channel);
				sortChannels();
				notifyDataSetChanged();
			}
		});
	}

	private void sortChannels() {
		Collections.sort(subscribed, new Comparator<Channel>() {
			@Override
			public int compare(Channel arg0, Channel arg1) {
				SharedPreferences sharedPreferences = parent.getSharedPreferences(Preferences.PREFS_NAME, 0);
				String myChannel = sharedPreferences.getString(Preferences.MY_CHANNEL_JID, null);
				if (arg1.getJid().equals(myChannel)) {
					return 1;
				}
				if (arg0.getJid().equals(myChannel)) {
					return -1;
				}
				int unreadCompare = arg1.getUnread().compareTo(arg0.getUnread());
				if (unreadCompare != 0) {
					return unreadCompare;
				}
				
				return arg0.getJid().compareTo(arg1.getJid());
			}
		});
	}
	
	private Bitmap fetchAvatar(String channel, String apiAddress) {
		
		int avatarSize = (int) (AVATAR_DIP * parent.getResources().getDisplayMetrics().density + 0.5);
		
		Bitmap avatar = picCache.getBitmap(apiAddress + "/" + channel + "/media/avatar?maxheight=" + avatarSize);
		if (avatar == null) {
			String fallBackURL = Preferences.FALLBACK_PERSONAL_AVATAR;
			if (channel.contains("@topics.buddycloud.org")) {
				fallBackURL = Preferences.FALLBACK_TOPIC_AVATAR;
			}
			avatar = picCache.getBitmap(fallBackURL);
		}
		return avatar;
	}
	
	private String fetchDescription(String channel, String apiAddress) {
		try {
			JSONObject jsonObject = BuddycloudHTTPHelper.get(apiAddress + "/" + 
					channel + "/metadata/posts", false, parent);
			return jsonObject.optString("description");
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public int getCount() {
		return subscribed.size();
	}

	@Override
	public Object getItem(int arg0) {
		return subscribed.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
		View retView = inflater.inflate(R.layout.subscriber_entry, viewGroup, false);

		Channel subscribedChannel = subscribed.get(position);

		TextView userIdView = (TextView) retView.findViewById(R.id.fbUserId);
		userIdView.setText(subscribedChannel.getJid());
		
		ImageView avatarView = (ImageView) retView.findViewById(R.id.fbProfilePic);
		if (subscribedChannel.getAvatar() != null) {
			avatarView.setImageBitmap(subscribedChannel.getAvatar());
		}
		
		TextView descriptionView = (TextView) retView.findViewById(R.id.fbMessage);
		descriptionView.setText(subscribedChannel.getDescription());
		
		TextView unreadCounterView = (TextView) retView.findViewById(R.id.unreadCounter);
		unreadCounterView.setText(subscribedChannel.getUnread().toString());
		
		SharedPreferences sharedPreferences = parent.getSharedPreferences(Preferences.PREFS_NAME, 0);
		String myChannel = sharedPreferences.getString(Preferences.MY_CHANNEL_JID, null);
		
		if (subscribedChannel.getJid().equals(myChannel)) {
			retView.setBackgroundColor(
					viewGroup.getResources().getColor(R.color.bc_bg_grey));
			retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
					viewGroup.getResources().getColor(R.color.bc_orange));
		} else {
			if (subscribedChannel.getUnread() == 0) {
				retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
						viewGroup.getResources().getColor(R.color.bc_bg_grey));
			} else {
				retView.findViewById(R.id.unreadWrapper).setBackgroundColor(
						viewGroup.getResources().getColor(R.color.bc_green));
			}
		}
		
        return retView;
	}
}
