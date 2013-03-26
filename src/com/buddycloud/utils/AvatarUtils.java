package com.buddycloud.utils;

import android.app.Activity;

import com.buddycloud.preferences.Preferences;

public class AvatarUtils {
	
	private static final double AVATAR_DIP = 75.;
	
	private AvatarUtils() {}
	
	public static String avatarURL(Activity context, String channel) {
		int avatarSize = (int) (AVATAR_DIP * context.getResources().getDisplayMetrics().density + 0.5);
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + "/media/avatar?maxheight=" + avatarSize;
	}
}
