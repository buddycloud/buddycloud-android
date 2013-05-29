package com.buddycloud.model;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.UnreadCountersDAO;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TimeUtils;

public class SyncModel implements Model<JSONObject, JSONObject, String> {

	private static SyncModel instance;
	private static final int PAGE_SIZE = 31;
	private static final String SYNC_ENDPOINT = "/sync";
	
	private SyncModel() {}

	public static SyncModel getInstance() {
		if (instance == null) {
			instance = new SyncModel();
		}
		return instance;
	}
	
	private void parseChannelCounters(UnreadCountersDAO unreadCountersDAO, String channel, JSONObject oldCounter, 
			int newPostsCount) {
		
		int oldTotalCount = 0;
		int oldMentionsCount = 0;
		
		boolean hasOldCounter = oldCounter != null;
		if (hasOldCounter) {
			oldTotalCount = oldCounter.optInt("totalCount");
			oldMentionsCount = oldCounter.optInt("mentionsCount");
		}

		JSONObject unreadCounters = new JSONObject();
		
		try {
			unreadCounters.put("totalCount", newPostsCount + oldTotalCount);
			// FIXME: needs to verify if there are mentions
			unreadCounters.put("mentionsCount", oldMentionsCount);
		} catch (JSONException e) {/*Do nothing*/}
		
		if (hasOldCounter) {
			unreadCountersDAO.update(channel, unreadCounters);
		} else {
			unreadCountersDAO.insert(channel, unreadCounters);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parse(Context context, JSONObject newCounters, Map<String, JSONObject> oldCounters) {
		
		final UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
		String syncTimestamp = TimeUtils.OLDEST_DATE;
		
		Iterator<String> keys = newCounters.keys();
		while (keys.hasNext()) {
			String node = keys.next();
			String channel = node.split("/")[2];
			JSONArray newPosts = newCounters.optJSONArray(node);
			int newPostsCount = newPosts.length();
			String newPostUpdate = newPosts.optJSONObject(0).optString("updated");
			
			try {
				if (TimeUtils.fromISOToDate(newPostUpdate).after(
						TimeUtils.fromISOToDate(syncTimestamp))) {
					syncTimestamp = newPostUpdate;
				}
			} catch (ParseException e) {
				// TODO Log exception
			}
			parseChannelCounters(unreadCountersDAO, channel, oldCounters.get(channel), newPostsCount);
		}
		
		Preferences.setPreference(context, Preferences.LAST_UPDATE, syncTimestamp);
	}
	
	@Override
	public JSONObject getFromCache(Context context, String... p) {
		UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
		Map<String, JSONObject> counters = unreadCountersDAO.getAll();
		return new JSONObject(counters);
	}
	
	@Override
	public void getFromServer(Context context, final ModelCallback<JSONObject> callback, String... p) {
	
	}

	public void resetCounter(Context context, String channelJid) {
		UnreadCountersDAO.getInstance(context).delete(channelJid);
	}
	
	public void fill(Context context, final ModelCallback<Void> callback, String... p) {
		UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
		sync(unreadCountersDAO.getAll(), context, callback);
	}
	
	private void sync(final Map<String, JSONObject> oldCounters, final Context context, 
			final ModelCallback<Void> callback) {
		
		BuddycloudHTTPHelper.getObject(syncUrl(context), context,
				new ModelCallback<JSONObject>() {

			@Override
			public void success(JSONObject newCounters) {
				parse(context, newCounters, oldCounters);
				if (callback != null) {
					callback.success(null);
				}
			}

			@Override
			public void error(Throwable throwable) {
				if (callback != null) {
					callback.error(throwable);
				}
			}
		});
	}
	
	private String since(Context context) {
		String lastUpdate = Preferences.getPreference(context, Preferences.LAST_UPDATE);
		return lastUpdate == null ? TimeUtils.OLDEST_DATE : lastUpdate;
	}

	private String syncUrl(Context context) {
		String params = "?max=" + PAGE_SIZE + "&since=" + since(context);
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + SYNC_ENDPOINT + params;
	}
	
	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
	}

}