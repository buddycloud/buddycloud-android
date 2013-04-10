package com.buddycloud.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.UnreadCountersDAO;
import com.buddycloud.model.db.UnreadCountersTableHelper;
import com.buddycloud.preferences.Preferences;

public class SyncModel implements Model<JSONObject, JSONObject, String> {

	private static SyncModel instance;
	private static final int MAX = 31;
	private static final String ENDPOINT = "/sync?counters=true&max=" + MAX + "&since=" + since(); 
	
	private Map<String, JSONObject> channelsCounters = new HashMap<String, JSONObject>();
	
	private SyncModel() {}
	
	public static SyncModel getInstance() {
		if (instance == null) {
			instance = new SyncModel();
		}
		return instance;
	}
	
	private static String since() {
		return "1970-01-01T00:00:00Z";
	}
	
	@Override
	public void refresh(Context context, final ModelCallback<JSONObject> callback, String... p) {
		// Get info from db first
		final UnreadCountersDAO dao = UnreadCountersDAO.getInstance(context);
		channelsCounters = dao.getAll();
		
		// Fetch from server
		BuddycloudHTTPHelper.getObject(url(context), context,
				new ModelCallback<JSONObject>() {
			
					private void insertCounter(String channel, JSONObject counter) {
						channelsCounters.put(channel, counter);
						// Db insert
						dao.insert(channel, counter);
					}
					
					private void updateCounter(String channel, JSONObject oldCounter, JSONObject newCounter) {
						int mentionsCount = oldCounter.optInt("mentionsCount") + newCounter.optInt("mentionsCount");
						int totalCount = oldCounter.optInt("totalCount") + newCounter.optInt("totalCount");

						// Update old counter
						try {
							oldCounter.put("mentionsCount", mentionsCount);
							oldCounter.put("totalCount", totalCount);
						} catch (JSONException e) {}
						
						// Db update
						dao.update(channel, oldCounter);
					}
			
					@SuppressWarnings("unchecked")
					@Override
					public void success(JSONObject response) {
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String node = (String) keyIterator.next();
							String channel = node.split("/")[2];
							
							JSONObject counter = channelsCounters.get(channel);
							if (counter != null) {
								updateCounter(channel, counter, response.optJSONObject(node));
							} else {
								insertCounter(channel, response.optJSONObject(node));
							}
							
						}
						callback.success(response);
					}

					@Override
					public void error(Throwable throwable) {
						if (callback != null) {
							callback.error(throwable);
						}
					}
				});
	}
	
	public void reset(Activity context, String channel) {
		JSONObject counter = new JSONObject();
		try {
			counter.putOpt(UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, 0);
			counter.putOpt(UnreadCountersTableHelper.COLUMN_TOTAL_COUNT, 0);
		} catch (JSONException e) {}
		
		// Reset map
		channelsCounters.put(channel, counter);
		
		// Reset db
		UnreadCountersDAO.getInstance(context).update(channel, counter);
	}

	private static String url(Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT;
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		// Do nothing
	}

	@Override
	public JSONObject get(Context context, String... p) {
		return p != null && p.length == 1 ? channelsCounters.get(p[0]) : null;
	}
	
}
