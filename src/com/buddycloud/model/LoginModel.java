package com.buddycloud.model;

import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class LoginModel extends AbstractModel<Void, Void, Void> {

	private static LoginModel instance;
	private static final String ENDPOINT = "/"; 
	
	private LoginModel() {}
	
	public static LoginModel getInstance() {
		if (instance == null) {
			instance = new LoginModel();
		}
		return instance;
	}
	
	public void getFromServer(final Context context, final ModelCallback<Void> callback, Void... p) {
		BuddycloudHTTPHelper.getObject(url(context), context, 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						callback.success(null);
					}
					
					@Override
					public void error(Throwable throwable) {
						callback.error(throwable);
					}
				});
	}

	
	private static String url(Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT;
	}


	@Override
	public void save(Context context, Void object,
			ModelCallback<Void> callback, Void... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Void getFromCache(Context context, Void... p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, Void... p) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Context context, ModelCallback<Void> callback, Void... p) {
		// TODO Auto-generated method stub
		
	}
}
