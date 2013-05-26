package com.buddycloud.model;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.DAOCallback;
import com.buddycloud.model.dao.PostsDAO;
import com.buddycloud.model.dao.UnreadCountersDAO;
import com.buddycloud.model.db.PostsTableHelper;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TimeUtils;

public class SyncModel implements Model<JSONObject, JSONObject, String> {

	private static SyncModel instance;
	private static final int PAGE_SIZE = 31;
	private static final String SYNC_ENDPOINT = "/sync";
	
	private Map<String, JSONObject> channelsCounters = new HashMap<String, JSONObject>();
	
	private SyncModel() {}

	public static SyncModel getInstance() {
		if (instance == null) {
			instance = new SyncModel();
		}
		return instance;
	}
	
	private void parseChannelCounters(UnreadCountersDAO unreadCountersDAO, String channel, JSONArray jsonPosts) {
		JSONObject unreadCounters = channelsCounters.get(channel);
		if (unreadCounters == null) {
			unreadCounters = new JSONObject();
		}

		try {
			unreadCounters.put("totalCount", jsonPosts.length() + unreadCounters.optInt("totalCount"));
			// FIXME: needs to verify if there are mentions
			unreadCounters.put("mentionsCount", 0 + unreadCounters.optInt("mentionsCount"));
		} catch (JSONException e) {/*Do nothing*/}
		
		JSONObject prev = channelsCounters.put(channel, unreadCounters);
		if (prev != null) {
			unreadCountersDAO.update(channel, unreadCounters);
		} else {
			unreadCountersDAO.insert(channel, unreadCounters);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parse(PostsDAO postsDAO, UnreadCountersDAO unreadCountersDAO, JSONObject response, boolean updateDatabase) {
		Iterator<String> keys = response.keys();
		while (keys.hasNext()) {
			String node = keys.next();
			String channel = node.split("/")[2];
			JSONArray jsonPosts = response.optJSONArray(node);
			parseChannelCounters(unreadCountersDAO, channel, jsonPosts);
			PostsModel.getInstance().parseChannelPosts(postsDAO, channel, jsonPosts, updateDatabase);
		}
	}
	
	private void lookupPostsFromDatabase(final Context context, final ModelCallback<JSONObject> callback) {
		final PostsDAO postsDAO = PostsDAO.getInstance(context);
		List<String> channels = postsDAO.getChannels();
		
		if (channels.isEmpty()) {
			fetchUnreadAndSync(context, callback, postsDAO);
			return;
		}
		
		final Semaphore semaphore = new Semaphore(channels.size() - 1);
		for (final String channel : channels) {
			DAOCallback<JSONArray> postCallback = new DAOCallback<JSONArray>() {
				@Override
				public void onResponse(JSONArray response) {
					if (response != null && response.length() > 0) {
						PostsModel.getInstance().parseChannelPosts(postsDAO, channel, response, false);
					}
					if (!semaphore.tryAcquire()) {
						fetchUnreadAndSync(context, callback, postsDAO);
					}
				}
			};
			postsDAO.get(channel, PAGE_SIZE, postCallback);
		}
	}
	
	private void lookupUnreadCountersFromDatabase(final Context context, 
			final ModelCallback<JSONObject> callback) {
		UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
		unreadCountersDAO.getAll(new DAOCallback<Map<String,JSONObject>>() {
			@Override
			public void onResponse(Map<String, JSONObject> response) {
				channelsCounters = response;
				// Fetch server
				sync(context, callback);
			}
		});
	}

	@Override
	public void refresh(Context context, final ModelCallback<JSONObject> callback, String... p) {
		PostsModel.getInstance().expire();
		// Lookup for posts at database
		lookupPostsFromDatabase(context, callback);
	}

	private void fetchUnreadAndSync(Context context,
			final ModelCallback<JSONObject> callback, PostsDAO postsDAO) {
		lookupUnreadCountersFromDatabase(context, callback);
	}
	
	private void sync(final Context context, final ModelCallback<JSONObject> callback) {
		BuddycloudHTTPHelper.getObject(syncUrl(context), context,
				new ModelCallback<JSONObject>() {

			@Override
			public void success(JSONObject response) {
				final PostsDAO postsDAO = PostsDAO.getInstance(context);
				final UnreadCountersDAO unreadCountersDAO = UnreadCountersDAO.getInstance(context);
				parse(postsDAO, unreadCountersDAO, response, true);
				
				if (callback != null) {
					callback.success(response);
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
	
	private String since() {
		Set<String> channels = PostsModel.getInstance().cachedChannels();
		String since = TimeUtils.OLDEST_DATE;
		
		for (String channel : channels) {
			List<String> postsIds = PostsModel.getInstance().cachedPostsFromChannel(channel);
			String temp = null;
			
			if (postsIds != null) {
				JSONObject mostRecentPost = PostsModel.getInstance().postWithId(postsIds.get(0), channel);
				List<JSONObject> comments = PostsModel.getInstance().cachedCommentsFromPost(
						mostRecentPost.optString(PostsTableHelper.COLUMN_ID));
				
				if (comments != null && comments.size() > 0) {
					JSONObject mostRecentComment = comments.get(comments.size() - 1);
					temp = mostRecentComment.optString(PostsTableHelper.COLUMN_UPDATED);
				} else {
					temp = mostRecentPost.optString(PostsTableHelper.COLUMN_UPDATED);
				}
				
			}
			
			if (temp != null) {
				try {
					if (TimeUtils.fromISOToDate(since).compareTo(TimeUtils.fromISOToDate(temp)) < 0) {
						since = temp;
					}
				} catch (ParseException e) {/*Do nothing*/}
			}
		}
		
		return since;
	}

	private String syncUrl(Context context) {
		String params = "?max=" + PAGE_SIZE + "&since=" + since();
		
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + SYNC_ENDPOINT + params;
	}
	
	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
	}

	@Override
	public JSONObject get(Context context, String... p) {
		return null;
	}
	
	public JSONObject countersFromChannel(String channel) {
		if (channel != null) {
			if (channelsCounters.containsKey(channel)) {
				return channelsCounters.get(channel);
			}
		}
		
		return new JSONObject();
	}
	
}