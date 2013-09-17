package com.buddycloud.model;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class FollowersModel extends AbstractModel<JSONObject, JSONObject, String> {

	private static final String ENDPOINT = "/subscribers"; 
	private static FollowersModel instance;
	
	private FollowersModel() {}
	
	public static FollowersModel getInstance() {
		if (instance == null) {
			instance = new FollowersModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONObject> callback, String... p) {
		String channelJid = p[0];
		BuddycloudHTTPHelper.getObject(url(channelJid, context), context, 
				callback);
	}

	private static String url(String channelJid, Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channelJid + ENDPOINT + "/posts";
	}


	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		String channelJid = p[0];
		try {
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url(channelJid, context), requestEntity, context, 
					callback);
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
		}
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

	@Override
	public void delete(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}
}
