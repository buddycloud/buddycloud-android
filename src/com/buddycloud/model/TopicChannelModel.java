package com.buddycloud.model;

import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class TopicChannelModel extends AbstractModel<JSONObject, Void, String> {

	private static TopicChannelModel instance;
	
	private TopicChannelModel() {}
	
	public static TopicChannelModel getInstance() {
		if (instance == null) {
			instance = new TopicChannelModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONObject> callback, String... p) {
	}

	private static String url(Context context, String channelJid) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channelJid;
	}


	@Override
	public void save(Context context, Void object,
			final ModelCallback<Void> callback, String... p) {
		if (p == null || p.length < 1) {
			return;
		}
		
		BuddycloudHTTPHelper.post(url(context, p[0]), true, false, null, context, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				notifyChanged();
				callback.success(null);
			}
			
			@Override
			public void error(Throwable throwable) {
				callback.error(throwable);
			}
		});
	}

	@Override
	public JSONObject getFromCache(Context context, String... p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}
}
