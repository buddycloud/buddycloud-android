package com.buddycloud.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsModel extends AbstractModel<JSONArray, JSONObject, String> {

	private static SubscribedChannelsModel instance;
	
	public static final String ROLE_MEMBER = "member";
	public static final String ROLE_PRODUCER = "producer";
	public static final String ROLE_NONE = "none";
	public static final String POST_NODE_SUFIX = "/posts";
	
	private static final String TAG = SubscribedChannelsModel.class.getName();
	private static final String ENDPOINT = "/subscribed"; 
	
	private JSONArray subscribed;
	
	private SubscribedChannelsModel() {}
	
	public static SubscribedChannelsModel getInstance() {
		if (instance == null) {
			instance = new SubscribedChannelsModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONArray> callback, String... p) {
		
	}

	private static String url(Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT;
	}


	@Override
	public void save(final Context context, JSONObject object,
			final ModelCallback<JSONObject> callback, String... p) {
		if (p.length > 0) {
			return;
		}
		
		try {
			Log.d(TAG, object.toString());
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url(context), true, false, requestEntity, context, new ModelCallback<JSONObject>() {
				@Override
				public void success(final JSONObject subscription) {
					fill(context, new ModelCallback<Void>() {
						@Override
						public void success(Void response) {
							callback.success(subscription);
						}

						@Override
						public void error(Throwable throwable) {
							callback.error(throwable);
						}
					});
				}
				
				@Override
				public void error(Throwable throwable) {
					callback.error(throwable);
				}
			});
		} catch (Exception e) {
			callback.error(e);
		}
	}

	@Override
	public JSONArray getFromCache(Context context, String... p) {
		if (subscribed == null) {
			return new JSONArray();
		}
		return subscribed;
	}

	@Override
	public void fill(Context context, final ModelCallback<Void> callback, String... p) {
		BuddycloudHTTPHelper.getObject(url(context), context, 
				new ModelCallback<JSONObject>() {
					@SuppressWarnings("unchecked")
					@Override
					public void success(JSONObject response) {
						final List<String> channels = new ArrayList<String>();
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String node = (String) keyIterator.next();
							if (node.endsWith(POST_NODE_SUFIX)) {
								channels.add(node.split("/")[0]);
							}
						}
						subscribed = new JSONArray(channels);
						notifyChanged();
						callback.success(null);
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}
}
