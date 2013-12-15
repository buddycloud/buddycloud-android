package com.buddycloud.model;

import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.UnreadCountersDAO;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.JIDUtils;
import com.buddycloud.utils.TimeUtils;

public class SyncModel extends AbstractModel<JSONObject, JSONObject, String> {

	private static SyncModel instance;
	private static String TAG = SyncModel.class.toString();
	private static final int PAGE_SIZE = 31;
	private static final int PAGE_SIZE_NO_SUMMARY = 10;
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
			String channel = JIDUtils.nodeToChannel(node);
			JSONObject channelSummary = summary.optJSONObject(node);
			
			String channelUpdated = channelSummary.optString("lastUpdated", null);
			if (channelUpdated != null && after(channelUpdated, syncTimestamp)) {
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
		UnreadCountersDAO dao = UnreadCountersDAO.getInstance(context);
		JSONObject summary = dao.get(channelJid);
		try {
			if (summary != null) {
				summary.put("mentionsCount", 0);
				summary.put("totalCount", 0);
				summary.put("replyCount", 0);
				summary.put("visitCount", summary.optInt("visitCount") + 1);
				dao.update(channelJid, summary);
			} else {
				summary = new JSONObject();
				summary.put("visitCount", 1);
				dao.insert(channelJid, summary);
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		notifyChanged();
	}
	
	public void fill(Context context, final ModelCallback<Void> callback, String... p) {
		UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
		sync(unreadCountersDAO.getAll(), context, callback);
	}
	
	private void sync(final Map<String, JSONObject> oldCounters, final Context context, 
			final ModelCallback<Void> callback) {
		
		BuddycloudHTTPHelper.getObject(syncUrlWithSummary(context), context,
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
	
	public void syncNoSummary(final Context context, final ModelCallback<Void> callback) {
		BuddycloudHTTPHelper.getObject(syncUrl(context), context,
				new ModelCallback<JSONObject>() {

			@SuppressWarnings("unchecked")
			@Override
			public void success(final JSONObject newPosts) {
				if (callback != null) {
					callback.success(null);
				}
				new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						PostsModel postsModel = PostsModel.getInstance();
						Iterator<String> keys = newPosts.keys();
						while (keys.hasNext()) {
							String key = keys.next();
							String channelJid = JIDUtils.nodeToChannel(key);
							JSONArray channelPosts = newPosts.optJSONArray(key);
							for (int i = 0; i < channelPosts.length(); i++) {
								try {
									JSONObject post = channelPosts.getJSONObject(i);
									postsModel.persistSinglePost(context, channelJid, post);
								} catch (Exception e) {
									// Best effort
								}
							}
						}
						return null;
					}
				}.execute();
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

	private String syncUrlWithSummary(Context context) {
		String params = "?max=" + PAGE_SIZE + "&since=" + since(context) + "&summary=true";
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + SYNC_ENDPOINT + params;
	}
	
	private String syncUrl(Context context) {
		String params = "?max=" + PAGE_SIZE_NO_SUMMARY + "&since=" + since(context);
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