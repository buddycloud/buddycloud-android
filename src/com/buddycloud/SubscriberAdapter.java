package com.buddycloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

public class SubscriberAdapter extends BaseAdapter {

	private List<Channel> subscribed = new ArrayList<Channel>();
	private ProfilePicCache picCache = new ProfilePicCache();
	private final Activity parent;
	
	public SubscriberAdapter(Activity parent) {
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
		
		JSONObject syncObject = BuddycloudHTTPHelper.get(apiAddress + 
				"/sync?since=2013-01-30T00:00:00Z&max=1000&counters=true", true, parent);
		
		JSONObject jsonObject = BuddycloudHTTPHelper.get(
				apiAddress + "/subscribed", true, parent);
		
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String nodeJid = (String) keys.next();
			String channelJid = nodeJid.split("/")[0];
			Channel channel = new Channel(channelJid);
			if (!subscribed.contains(channel)) {
				channel.setUnread(syncObject.optInt("/user/" + channelJid + "/posts"));
				channel.setAvatar(fetchAvatar(channelJid, apiAddress));
				channel.setDescription(fetchDescription(channelJid, apiAddress));
				subscribed.add(channel);
				sortChannels();
				notifyChanged();
			}
		}
		
		
	}

	private void notifyChanged() {
		parent.findViewById(R.id.contentListView).post(new Runnable() {
			@Override
			public void run() {
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
				int unreadCompare = arg1.getUnread().compareTo(arg0.getUnread());
				if (unreadCompare != 0) {
					return unreadCompare;
				}
				
				return arg0.getJid().compareTo(arg1.getJid());
			}
		});
	}
	
	private Bitmap fetchAvatar(String channel, String apiAddress) {
		return picCache.getBitmap(apiAddress + "/" + channel + "/media/avatar?maxwidth=50");
	}
	
	private String fetchDescription(String channel, String apiAddress) {
		JSONObject jsonObject = BuddycloudHTTPHelper.get(apiAddress + "/" + 
				channel + "/metadata/posts", false, parent);
		return jsonObject.optString("description");
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
