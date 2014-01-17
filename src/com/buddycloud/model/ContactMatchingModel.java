package com.buddycloud.model;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class ContactMatchingModel extends AbstractModel<JSONObject, JSONObject, String> {

	private static final String ENDPOINT = "/match_contacts"; 
	private static ContactMatchingModel instance;
	
	private ContactMatchingModel() {}
	
	public static ContactMatchingModel getInstance() {
		if (instance == null) {
			instance = new ContactMatchingModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<JSONObject> callback, String... p) {
	}

	private static String url(Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + ENDPOINT;
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		try {
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url(context), requestEntity, context, callback);
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
		}
	}

	@Override
	public JSONObject getFromCache(Context context, String... p) {
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		
	}

	@Override
	public void delete(Context context, ModelCallback<Void> callback, String... p) {
		
	}
}
