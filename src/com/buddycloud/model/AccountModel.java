package com.buddycloud.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;

public class AccountModel extends AbstractModel<Void, JSONObject, String> {

	private static AccountModel instance;
	private static final String ENDPOINT = "/account"; 
	private static final String PW_RESET_ENDPOINT = "/pw/reset";
	private static final String PW_CHANGE_ENDPOINT = "/pw/change"; 
	
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

	private static String pwResetUrl(String apiAddress) {
		return apiAddress + ENDPOINT + PW_RESET_ENDPOINT;
	}

	private static String pwChangeUrl(Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + ENDPOINT + PW_CHANGE_ENDPOINT;
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
	public void delete(Context context, final ModelCallback<Void> callback, String... p) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		BuddycloudHTTPHelper.delete(url(apiAddress), true, false, context, new ModelCallback<JSONObject>() {
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

	public void resetPassword(final Context context, final String userJid, 
			final ModelCallback<JSONObject> callback) {
		String[] userJidSplitted = userJid.split("@");
		String domain = userJidSplitted[1];
		DNSUtils.resolveAPISRV(new ModelCallback<String>() {
			@Override
			public void success(String apiAddress) {
				Map<String, String> passwordObj = new HashMap<String, String>();
				passwordObj.put("username", userJid);
				StringEntity requestEntity = null;
				try {
					requestEntity = new StringEntity(new JSONObject(passwordObj).toString(), "UTF-8");
					requestEntity.setContentType("application/json");
				} catch (Exception e) {
					callback.error(e);
					return;
				}
				BuddycloudHTTPHelper.post(pwResetUrl(apiAddress), false, false, 
						requestEntity, context, callback);
			}

			@Override
			public void error(Throwable throwable) {
				callback.error(throwable);
			}
		}, domain);
	}

	public void changePassword(Context context, String currentPassword,
			String newPassword, final ModelCallback<Void> callback) {
		String authHeader = BuddycloudHTTPHelper.getAuthHeader(context, currentPassword);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", authHeader);
		
		Map<String, String> passwordObj = new HashMap<String, String>();
		passwordObj.put("username", Preferences.getPreference(context, Preferences.MY_CHANNEL_JID));
		passwordObj.put("password", newPassword);
		StringEntity requestEntity = null;
		try {
			requestEntity = new StringEntity(new JSONObject(passwordObj).toString(), "UTF-8");
			requestEntity.setContentType("application/json");
		} catch (Exception e) {
			callback.error(e);
			return;
		}
		
		BuddycloudHTTPHelper.post(pwChangeUrl(context), headers, requestEntity, context, 
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
}
