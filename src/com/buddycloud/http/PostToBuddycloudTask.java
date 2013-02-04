package com.buddycloud.http;

import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.buddycloud.preferences.Constants;

public class PostToBuddycloudTask extends AsyncTask<String, Void, Void> {

	private final Activity parent;

	public PostToBuddycloudTask(Activity parent) {
		this.parent = parent;
	}

	@Override
	protected Void doInBackground(String... params) {

		try {
			SharedPreferences preferences = parent.getSharedPreferences(Constants.PREFS_NAME, 0);
			String myChannel = preferences.getString(Constants.MY_CHANNEL, null);
			
			String url = Constants.MY_API + "/" + myChannel + "/content/posts";
			StringEntity requestEntity = new StringEntity(
					"{\"content\": \"Sent from buddycloud android app. " + params[0] + "\"}",
				    "UTF-8");
			
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url, true, requestEntity, preferences);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}
	
}
