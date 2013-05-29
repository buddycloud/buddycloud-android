package com.buddycloud.model;

import java.util.Iterator;

import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.ChannelMetadataDAO;
import com.buddycloud.preferences.Preferences;

public class ChannelMetadataModel implements Model<JSONObject, JSONObject, String> {

	private static ChannelMetadataModel instance;
	private static final String ENDPOINT = "/metadata/posts"; 
	
	private ChannelMetadataModel() {}
	
	public static ChannelMetadataModel getInstance() {
		if (instance == null) {
			instance = new ChannelMetadataModel();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private void updateMetadata(ChannelMetadataDAO dao, String channel, JSONObject oldMetadata, 
			JSONObject newMetadata, ModelCallback<Void> callback) {

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
			dao.update(channel, newMetadata);
			
			if (callback != null) {
				callback.success(null);
			}
		}
	}
	
	private void insertMetadata(ChannelMetadataDAO dao, String channel, JSONObject newMetadata, 
			ModelCallback<Void> callback) {
		dao.insert(channel, newMetadata);
		if (callback != null) {
			callback.success(null);
		}
	}
	
	@Override
	public JSONObject getFromCache(Context context, String... p) {
		if (p == null || p.length != 1) {
			return null;
		}
		String channel = p[0];
		return ChannelMetadataDAO.getInstance(context).get(channel);
	}

	private void fetchFromServer(Context context,
			final ModelCallback<Void> callback, final String channel) {
		final ChannelMetadataDAO dao = ChannelMetadataDAO.getInstance(context);
		BuddycloudHTTPHelper.getObject(url(context, channel), 
				context, new ModelCallback<JSONObject>() {
			@Override
			public void success(final JSONObject metadata) {
				JSONObject oldMetadata = dao.get(channel);
				if (oldMetadata != null) {
					updateMetadata(dao, channel, oldMetadata, metadata, callback);
				} else {
					insertMetadata(dao, channel, metadata, callback);
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
	public void getFromServer(Context context, ModelCallback<JSONObject> callback,
			String... p) {
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		fetchFromServer(context, callback, p[0]);
	}

}
