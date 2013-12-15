package com.buddycloud.utils;

public class JIDUtils {

	public static String nodeToChannel(String node) {
		return node.split("/")[2];
	}

}
