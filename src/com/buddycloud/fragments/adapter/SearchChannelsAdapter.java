package com.buddycloud.fragments.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SearchChannelsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;

public class SearchChannelsAdapter extends GenericChannelAdapter {

	private static final String PERSONAL = "PERSONAL";
	private static final String SUBSCRIBED = "SUBSCRIBED";
	private static final String METADATA_SEARCH = "METADATA SEARCH";
	private static final String CONTENT_SEARCH = "CONTENT SEARCH";
	
	private static final int SEARCH_THRESHOLD = 5;
	
	private final List<String> allSubscribedChannels = new ArrayList<String>();
	private final Map<String, String> plainMetadata = new HashMap<String, String>();
	private String myChannel;
	private View view;
	
	private boolean remoteSearch = true;
	private List<String> affiliationsToDisplay = new LinkedList<String>();
	private JSONObject allAffiliations;
	
	public SearchChannelsAdapter() {
		setCategoryOrder(PERSONAL, SUBSCRIBED, METADATA_SEARCH, CONTENT_SEARCH);
	}
	
	@Override
	public void configure(View view) {
		super.configure(view);
	}

	public String getTitle(final Context context) {
		return (context != null) ? context.getResources().getString(R.string.menu_search) : null;
	}
	
	public void addAffiliationToDisplay(String affiliation) {
		this.affiliationsToDisplay.add(affiliation);
	}
	
	public void enableRemoteSearch(boolean enabled) {
		this.remoteSearch = enabled;
	}
	
	@Override
	public void load(final Context context) {
		this.myChannel = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
		JSONObject subscriptions = SubscribedChannelsModel.getInstance().getFromCache(context);
		JSONArray channels = subscriptions.names();
		if (channels == null) {
			return;
		}
		
		for (int i = 0; i < channels.length(); i++) {
			final String channel = channels.optString(i);
			allSubscribedChannels.add(channel);
			JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(context, channel);
			plainMetadata.put(channel, getPlainMetadata(context, metadata));
		}

		if (!affiliationsToDisplay.isEmpty()) {
			this.allAffiliations = SubscribedChannelsModel.getInstance().getFromCache(context);
		}
		
		filter(context, "");
		notifyDataSetChanged();
	}

	@SuppressLint("DefaultLocale")
	public void filter(final Context context, String q) {
		setLoading(context);
		clearChannels();
		
		int matchedChannels = 0;
		for (String channel : allSubscribedChannels) {
			String channelPlainMetadata = plainMetadata.get(channel);
			String lowerQ = low(q);
			if (!low(channel).contains(lowerQ) 
					&& !low(channelPlainMetadata).contains(lowerQ)) {
				continue;
			}
			if (!channel.equals(myChannel)) {
				if (affiliationsToDisplay.isEmpty() || 
						affiliationsToDisplay.contains(getRole(context, channel))) {
					addChannel(SUBSCRIBED, createChannelItem(channel), context);
				}
			} else {
				addChannel(PERSONAL, createChannelItem(channel), context);
			}
			matchedChannels++;
		}
		if (matchedChannels <= SEARCH_THRESHOLD && q.length() > 0 && remoteSearch) {
			setLoading(context);
			Semaphore s = new Semaphore(1);
			search(context, q, SearchChannelsModel.METADATA_TYPE, METADATA_SEARCH, s);
			search(context, q, SearchChannelsModel.POST_TYPE, CONTENT_SEARCH, s);
		}
	}
	
	private String getRole(Context context, String channelJid) {
		return allAffiliations.optString(channelJid, "");
	}

	private static String low(String q) {
		return q.toLowerCase(Locale.US);
	}

	@SuppressWarnings("unchecked")
	private String getPlainMetadata(final Context context, JSONObject metadata) {
		if (metadata == null) {
			return "";
		}
		Iterator<String> keys = metadata.keys();
		StringBuilder allValues = new StringBuilder();
		while (keys.hasNext()) {
			String key = keys.next();
			allValues.append(metadata.optString(key));
		}
		return allValues.toString();
	}

	private void search(final Context context, String q, String type, final String category, final Semaphore s) {
		SearchChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					JSONObject channelItem = response.optJSONObject(i);
					String channelJid = channelItem.optString("jid");
					if (!hasChannel(channelJid)) {
						addChannel(category, channelItem, context);
					}
				}
				if (!s.tryAcquire()) {
					setLoaded(context, response.length() == 0);
				}
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, context.getString(
						R.string.message_search_failed), 
						Toast.LENGTH_LONG).show();
				if (!s.tryAcquire()) {
					setLoaded(context, true);
				}
			}
		}, type, q);
	}

	private void setLoading(final Context context) {
		ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.channelListProgress);
		ExpandableListView elv = (ExpandableListView)view.findViewById(R.id.channelListView);
		View noResultsFound = view.findViewById(R.id.channelListEmpty);
		
		if (elv != null) {
			progressBar.setVisibility(View.VISIBLE);
			elv.setEmptyView(progressBar);
			
			if (noResultsFound != null) {
				noResultsFound.setVisibility(View.GONE);
			}
		}
	}
	
	private void setLoaded(final Context context, final boolean isShow) {
		ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.channelListProgress);
		progressBar.setVisibility(View.GONE);
		
		if (isShow) {	
			showNoResultsFoundView(context.getString(R.string.message_search_no_result));
		}
	}

	public void configure(GenericChannelsFragment genericChannelFrag, View view) {
		this.view = view;
	}
}
