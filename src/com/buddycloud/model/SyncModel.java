package com.buddycloud.model;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.UnreadCountersDAO;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TimeUtils;

public class SyncModel extends AbstractModel<JSONObject, JSONObject, String> {

	private static SyncModel instance;
	private static String TAG = SyncModel.class.toString();
	private static final int PAGE_SIZE = 31;
	private static final String SYNC_ENDPOINT = "/sync";
	
	private SyncModel() {}

	public static SyncModel getInstance() {
		if (instance == null) {
			instance = new SyncModel();
		}
		return instance;
	}
	
	private void parseChannelCounters(UnreadCountersDAO unreadCountersDAO, String channel, 
			JSONObject oldSummary, JSONObject newSummary) {
		
		int oldTotalCount = 0;
		int oldMentionsCount = 0;
		int oldRepliesCount = 0;
		
		boolean hasOldCounter = oldSummary != null;
		if (hasOldCounter) {
			oldTotalCount = oldSummary.optInt("totalCount");
			oldMentionsCount = oldSummary.optInt("mentionsCount");
			oldRepliesCount = oldSummary.optInt("replyCount");
		}

		JSONObject unreadCounters = new JSONObject();
		try {
			unreadCounters.put("totalCount", newSummary.optInt("totalCount") + oldTotalCount);
			unreadCounters.put("mentionsCount", newSummary.optInt("mentionsCount") + oldMentionsCount);
			unreadCounters.put("replyCount", newSummary.optInt("repliesCount") + oldRepliesCount);
			unreadCounters.put("lastWeekActivity", newSummary.optJSONArray("postsThisWeek").length());
		} catch (JSONException e) {/*Do nothing*/}
		
		if (hasOldCounter) {
			unreadCountersDAO.update(channel, unreadCounters);
		} else {
			unreadCountersDAO.insert(channel, unreadCounters);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parse(Context context, JSONObject summary, Map<String, JSONObject> oldCounters) {
		
		final UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
		String lastUpdate = Preferences.getPreference(context, Preferences.LAST_UPDATE, TimeUtils.OLDEST_DATE);
		String syncTimestamp = lastUpdate;
		
		Iterator<String> keys = summary.keys();
		while (keys.hasNext()) {
			String node = keys.next();
			String channel = node.split("/")[2];
			JSONObject channelSummary = summary.optJSONObject(node);
			
			JSONArray postsThisWeek = channelSummary.optJSONArray("postsThisWeek");
			String channelUpdated = TimeUtils.OLDEST_DATE;
			if (postsThisWeek.length() > 0) {
				channelUpdated = postsThisWeek.optString(0);
			}
			
			if (after(channelUpdated, syncTimestamp)) {
				syncTimestamp = channelUpdated;
			}
			
			parseChannelCounters(unreadCountersDAO, channel, 
					oldCounters.get(channel), channelSummary);
		}
		
		Preferences.setPreference(context, Preferences.LAST_UPDATE, syncTimestamp);
	}

	private boolean after(String dateA, String dateB) {
		try {
			return TimeUtils.fromISOToDate(dateA).after(
					TimeUtils.fromISOToDate(dateB));
		} catch (ParseException e) {
			Log.e(TAG, "Could not parse dates.", e);
			return false;
		}
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

	public void visitChannel(Context context, String channelJid) {
		UnreadCountersDAO.getInstance(context).delete(channelJid);
		notifyChanged();
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
				notifyChanged();
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
		String params = "?max=" + PAGE_SIZE + "&since=" + since(context) + "&summary=true";
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + SYNC_ENDPOINT + params;
	}
	
	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
	}

	@Override
	public void delete(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}

}