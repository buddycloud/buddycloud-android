package com.buddycloud.preferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

	public static final String PREFS_NAME = "BuddycloudPrefsFile";
	
	public static final String MY_CHANNEL_JID = "com.bebendo.MYCHANNELJID";
	public static final String API_ADDRESS = "com.bebendo.APIADDRESS";
	public static final String PASSWORD = "com.bebendo.PASSWORD";
	public static final String LAST_UPDATE = "com.bebendo.LASTUPDATE";
	
	public static final String DEFAULT_API_ADDRESS = "https://api.buddycloud.org";
	public static final String DEFAUL_LAST_UPDATE = "2013-01-01T00:00:00Z";
	
	public static final String FALLBACK_PERSONAL_AVATAR = "https://demo.buddycloud.org/img/personal-75px.jpg";
	public static final String FALLBACK_TOPIC_AVATAR = "https://demo.buddycloud.org/img/topic-75px.jpg";	
	
	public static final DateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static {
		ISO_8601.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
	
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
