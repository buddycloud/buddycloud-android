package com.buddycloud.fragments.adapter;

import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.ModelCallbackImpl;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private String myChannel;
	
	public SubscribedChannelsAdapter() {
		setCategoryOrder(PERSONAL, SUBSCRIBED);
	}
	
	public void load(final Context context) {
		reload(context);
		loadFromServer(context);
	}

	public void reload(final Context context) {
		this.myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		JSONObject subscriptions = SubscribedChannelsModel.getInstance().getFromCache(context);
		JSONArray channels = subscriptions.names();
		clear();
		
		for (int i = 0; channels != null && i < channels.length(); i++) {
			String channel = channels.optString(i);
			if (!channel.equals(myChannel)) {
				addChannel(SUBSCRIBED, createChannelItem(channel), context);
			} else {
				addChannel(PERSONAL, createChannelItem(channel), context);
			}
		}
		
		sort(context);
		notifyDataSetChanged();
	}

	public void loadFromServer(final Context context) {
		SubscribedChannelsModel.getInstance().fill(context, new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				SyncModel.getInstance().syncNoSummary(
						context, new ModelCallbackImpl<Void>(){
							@Override
							public void success(Void response) {
								SyncModel.getInstance().fill(
										context, new ModelCallbackImpl<Void>());
							}
							
							@Override
							public void error(Throwable throwable) {
								success(null);
							}
						});
			}
			
			@Override
			public void error(Throwable throwable) {
				Log.w(SubscribedChannelsAdapter.class.toString(), 
						throwable.getLocalizedMessage(), throwable);
				Toast.makeText(context, context.getString(
						R.string.message_fetch_subscribed_failed), 
						Toast.LENGTH_LONG).show();
			}
		});
	}
	
	public void sort(final Context context) {
		final JSONObject allCounters = SyncModel.getInstance().getFromCache(context);
		sort(new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject lhs, JSONObject rhs) {
				
				int diff = compareCounters(lhs, rhs, "mentionsCount", "replyCount", 
						"totalCount", "visitCount", "lastWeekActivity");
				if (diff != 0) {
					return diff;
				}
				return lhs.optString("jid").compareTo(rhs.optString("jid"));
			}
			
			private int compareCounters(JSONObject lhs, JSONObject rhs, String... fields) {
				for (String field : fields) {
					int countA = getCounter(allCounters, lhs.optString("jid"), field);
					int countB = getCounter(allCounters, rhs.optString("jid"), field);
					int diff = countB - countA;
					if (diff != 0) {
						return diff;
					}
				}
				return 0;
			}
		});
	}
	
	private static int getCounter(JSONObject allCounters, String channel, String key) {
		JSONObject channelCounters = allCounters.optJSONObject(channel);
		if (channelCounters == null) {
			return 0;
		}
		return channelCounters.optInt(key);
	}
}