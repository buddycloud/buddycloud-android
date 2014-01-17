package com.buddycloud.fragments.contacts;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class ContactMatcherUtils {

	public static String hash(String provider, String id) {
		return new String(Hex.encodeHex(DigestUtils.sha256(provider + ":" + id)));
	}
	
}
