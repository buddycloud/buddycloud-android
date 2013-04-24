package com.buddycloud.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.ChannelMetadataDAO;
import com.buddycloud.model.dao.DAOCallback;
import com.buddycloud.preferences.Preferences;

public class ChannelMetadataModel implements Model<JSONObject, JSONObject, String> {

	private static ChannelMetadataModel instance;
	private static final String ENDPOINT = "/metadata/posts"; 
	
	private Map<String, JSONObject> channelsMetadataMap = new HashMap<String, JSONObject>();
	
	private ChannelMetadataModel() {}
	
	public static ChannelMetadataModel getInstance() {
		if (instance == null) {
			instance = new ChannelMetadataModel();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private void updateMetadata(ChannelMetadataDAO dao, String channel, JSONObject oldMetadata, 
			JSONObject newMetadata, ModelCallback<JSONObject> callback) {

		// Verify if any of the data has changed
		boolean update = false;
		Iterator<String> keys = oldMetadata.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			
			if (!oldMetadata.optString(key).equals(newMetadata.optString(key))) {
				update = true;
				break;
			}
		}
		
		// Update, if necessary
		if (update) {
			channelsMetadataMap.put(channel, newMetadata);
			dao.update(channel, newMetadata);
			
			if (callback != null) {
				callback.success(null);
			}
		}
	}
	
	private void insertMetadata(ChannelMetadataDAO dao, String channel, JSONObject newMetadata, 
			ModelCallback<JSONObject> callback) {
		
		channelsMetadataMap.put(channel, newMetadata);
		dao.insert(channel, newMetadata);
		
		if (callback != null) {
			callback.success(null);
		}
	}
	
	@Override
	public void refresh(Context context, final ModelCallback<JSONObject> callback, String... p) {
		if (p != null && p.length == 1) {
			final String channel = p[0];
			lookupStoredData(context, callback, channel);
		}
	}

	private void fetchFromServer(Context context,
			final ModelCallback<JSONObject> callback, final String channel) {
		final ChannelMetadataDAO dao = ChannelMetadataDAO.getInstance(context);
		BuddycloudHTTPHelper.getObject(url(context, channel), 
				context, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				JSONObject metadata = channelsMetadataMap.get(channel);
				if (metadata != null) {
					updateMetadata(dao, channel, metadata, response, callback);
				} else {
					insertMetadata(dao, channel, response, callback);
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
	
	private void lookupStoredData(final Context context,
			final ModelCallback<JSONObject> callback, final String channel) {
		final ChannelMetadataDAO dao = ChannelMetadataDAO.getInstance(context);
		dao.getAll(new DAOCallback<Map<String,JSONObject>>() {
			@Override
			public void onResponse(Map<String, JSONObject> response) {
				channelsMetadataMap = response;
				if (channelsMetadataMap != null && callback != null) {
					callback.success(null);
				}
				fetchFromServer(context, callback, channel);
			}
		});
	}
	
	private static String url(Context context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT;
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		// TODO Auto-generated method stub
	}

	@Override
	public JSONObject get(Context context, String... p) {
		return p != null && p.length == 1 ? channelsMetadataMap.get(p[0]) : null;
	}
	
}
