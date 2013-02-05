package com.buddycloud.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

	public static final String PREFS_NAME = "BuddycloudPrefsFile";
	public static final String MY_CHANNEL_JID = "login";
	public static final String API_ADDRESS = "apiaddress";
	public static final String PASSWORD = "password";
	
	public static final String DEFAULT_API_ADDRESS = "https://api.buddycloud.org";

	public static String getPreference(Activity parent, String key) {
		SharedPreferences preferences = parent.getSharedPreferences(PREFS_NAME, 0);
		return preferences.getString(key, null);
	}
	
	public static void setPreference(Activity parent, String key, String value) {
		SharedPreferences preferences = parent.getSharedPreferences(PREFS_NAME, 0);
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
}
