package com.buddycloud.http;

import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.os.AsyncTask;

import com.buddycloud.preferences.Preferences;

public class PostToBuddycloudTask extends AsyncTask<String, Void, Void> {

	private final Activity parent;

	public PostToBuddycloudTask(Activity parent) {
		this.parent = parent;
	}

	@Override
	protected Void doInBackground(String... params) {

		try {
			String myChannel = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);
			String apiAddress = Preferences.getPreference(parent, Preferences.API_ADDRESS);
			
			String imageURL = params[0];
			String caption = params[1];
			
			String postURL = apiAddress + "/" + myChannel + "/content/posts";
			StringEntity requestEntity = new StringEntity(
					"{\"content\": \"" + caption + " - via buddycloud android app. " + imageURL + "\"}",
				    "UTF-8");
			
			requestEntity.setContentType("application/json");
//			BuddycloudHTTPHelper.post(postURL, true, requestEntity, parent);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return null;
	}
	
}
