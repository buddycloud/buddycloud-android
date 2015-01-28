package com.buddycloud;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.buddycloud.log.Logger;
import com.buddycloud.model.PostsModel;

public class ConnectivityChangeIntentService extends IntentService {

	private static final String TAG = ConnectivityChangeIntentService.class.getName();

	public ConnectivityChangeIntentService() {
		super(ConnectivityChangeIntentService.class.toString());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getApplicationContext();
		ConnectivityManager cm =
		        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		Logger.debug(TAG, "Connected: " + isConnected + "; Intent: " + intent);
		
		if (isConnected) {
			PostsModel.getInstance().savePendingPosts(context);
		}
	}

}
