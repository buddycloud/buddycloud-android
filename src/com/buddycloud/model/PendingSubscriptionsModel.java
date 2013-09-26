package com.buddycloud.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class PendingSubscriptionsModel extends AbstractModel<JSONArray, JSONArray, String> {

	private static final String ENDPOINT_SUFIX = "/approve";
	private static final String POSTS_NODE = "/posts";
	private static final String ENDPOINT = "/subscribers";
	private static PendingSubscriptionsModel instance;
	
	private PendingSubscriptionsModel() {}
	
	public static PendingSubscriptionsModel getInstance() {
		if (instance == null) {
			instance = new PendingSubscriptionsModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONArray> callback, String... p) {
		String channelJid = p[0];
		BuddycloudHTTPHelper.getArray(url(channelJid, context), context, 
				new ModelCallback<JSONArray>() {
					@Override
					public void success(JSONArray response) {
						List<String> jids = new ArrayList<String>();
						for (int i = 0; i < response.length(); i++) {
							JSONObject subscription = response.optJSONObject(i);
							if(subscription.optString("subscription").equals(
									SubscribedChannelsModel.SUBSCRIPTION_PENDING)) {
								jids.add(subscription.optString("jid"));
							}
						}
						callback.success(new JSONArray(jids));
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}

	private static String url(String channelJid, Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channelJid + ENDPOINT + POSTS_NODE + ENDPOINT_SUFIX;
	}


	@Override
	public void save(Context context, JSONArray object,
			final ModelCallback<JSONArray> callback, String... p) {
		try {
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url(p[0], context), true, false, requestEntity, context, new ModelCallback<JSONObject>() {
				@Override
				public void success(final JSONObject subscription) {
					callback.success(null);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
	}
}
