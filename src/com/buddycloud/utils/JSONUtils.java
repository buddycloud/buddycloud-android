package com.buddycloud.utils;

import org.json.JSONArray;

public class JSONUtils {

	public static boolean contains(JSONArray array, String content) {
		for (int i = 0; i < array.length(); i++) {
			if (array.optString(i).equals(content)) {
				return true;
			}
		}
		return false;
	}
	
}
