package com.buddycloud.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class FollowersModel extends AbstractModel<JSONArray, JSONArray, String> {

	private static final String ENDPOINT = "/subscribers"; 
	private static FollowersModel instance;
	
	private FollowersModel() {}
	
	public static FollowersModel getInstance() {
		if (instance == null) {
			instance = new FollowersModel();
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void getFromServer(final Context context, final ModelCallback<JSONArray> callback, String... p) {
		String channelJid = p[0];
		BuddycloudHTTPHelper.getObject(url(channelJid, context), context, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						List<String> channels = new ArrayList<String>();
						Iterator<String> keyIterator = response.keys();
						while (keyIterator.hasNext()) {
							String key = keyIterator.next();
							channels.add(key);
						}
						callback.success(new JSONArray(channels));
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}

	private static String url(String channelJid, Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channelJid + ENDPOINT + "/posts";
	}


	@Override
	public void save(Context context, JSONArray object,
			ModelCallback<JSONArray> callback, String... p) {
		// TODO Auto-generated method stub
		
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
