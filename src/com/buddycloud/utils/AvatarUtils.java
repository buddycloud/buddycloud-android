package com.buddycloud.utils;

import android.app.Activity;

import com.buddycloud.preferences.Preferences;

public class AvatarUtils {
	
	private static final double AVATAR_DIP = 75.;
	
	private static final int MIN_SIZE = 50;
	private static final int MAX_SIZE = 200;
	private static final int THRESHOLD = 125;
	
	private AvatarUtils() {}
	
	public static String avatarURL(Activity context, String channel) {
		int avatarSize = (int) (AVATAR_DIP * context.getResources().getDisplayMetrics().density + 0.5);
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		avatarSize = avatarSize > THRESHOLD ? MAX_SIZE : MIN_SIZE; 
		return apiAddress + "/" + channel + "/media/avatar?maxheight=" + avatarSize;
	}
}
