package com.buddycloud.utils;

import com.buddycloud.init.ApplicationManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This class used to check the network is connected or not.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class NetworkUtil {

	public static boolean isNetworkAvailable() {

		Context ctx = ApplicationManager.getAppContext();

		ConnectivityManager connectivityManager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
