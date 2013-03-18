package com.buddycloud.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SyncModel implements Model<JSONObject, JSONObject, String> {

	private static SyncModel instance;
	private static final String ENDPOINT = "/sync?since=1970-01-01T00:00:00Z&counters=true&max=30"; 
	
	private Map<String, JSONObject> channelsCounters = new HashMap<String, JSONObject>();
	
	private SyncModel() {}
	
	public static SyncModel getInstance() {
		if (instance == null) {
			instance = new SyncModel();
		}
		return instance;
	}
	
	@Override
	public void refresh(Activity context, final ModelCallback<JSONObject> callback, String... p) {
		BuddycloudHTTPHelper.getObject(url(context), true, context,
				new ModelCallback<JSONObject>() {

					@SuppressWarnings("unchecked")
					@Override
					public void success(JSONObject response) {
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String node = (String) keyIterator.next();
							channelsCounters.put(node.split("/")[2],
									response.optJSONObject(node));
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

	private static String url(Activity context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT;
	}

	@Override
	public void save(Activity context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		// TODO Auto-generated method stub
	}

	@Override
	public JSONObject get(Activity context, String... p) {
		return p != null && p.length == 1 ? channelsCounters.get(p[0]) : null;
	}
	
}
