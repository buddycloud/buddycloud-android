package com.buddycloud.model;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.buddycloud.http.BuddycloudHTTPHelper;

import android.content.Context;

public class AccountModel extends AbstractModel<Void, JSONObject, String> {

	private static AccountModel instance;
	private static final String ENDPOINT = "/account"; 
	
	private AccountModel() {}
	
	public static AccountModel getInstance() {
		if (instance == null) {
			instance = new AccountModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<Void> callback, String... p) {
	}

	private static String url(String apiAddress) {
		return apiAddress + ENDPOINT;
	}


	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		
		if (p == null || p.length < 1) {
			return;
		}
		try {
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url(p[0]), false, false, requestEntity, context, callback);
		} catch (Exception e) {
			callback.error(e);
		}
	}

	@Override
	public Void getFromCache(Context context, String... p) {
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
