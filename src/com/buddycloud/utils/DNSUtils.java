package com.buddycloud.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import android.os.AsyncTask;

import com.buddycloud.model.ModelCallback;

public class DNSUtils {

	private static final String TXT_PREFIX = "_buddycloud-api._tcp.";
	
	/**
	 * Adapted from https://code.google.com/p/asmack/source/browse/src/custom/org/jivesoftware/smack/util/DNSUtil.java
	 * 
	 * @param domain
	 * @return
	 * @throws TextParseException
	 */
	@SuppressWarnings("unchecked")
	private static String resolveAPITXT(String domain) throws TextParseException {
		Lookup lookup = new Lookup(TXT_PREFIX + domain, Type.TXT);
		Record recs[] = lookup.run();
		if (recs == null) {
			throw new RuntimeException("Could not lookup domain.");
		}

		Map<String, String> stringMap = null;
		for (Record rec : recs) {
			String rData = rec.rdataToString().replaceAll("\"", "");
			List<String> rDataTokens = Arrays.asList(rData.split("\\s+"));
			TXTRecord record = new TXTRecord(rec.getName(), rec.getDClass(), 
					rec.getTTL(), rDataTokens);
			List<String> strings = record.getStrings();
			if (strings != null && strings.size() > 0) {
				stringMap = parseStrings(strings);
				break;
			}
		}

		if (stringMap == null) {
			throw new RuntimeException("Domain has no TXT records for buddycloud.");
		}

		String host = stringMap.get("host");
		String protocol = stringMap.get("protocol");
		String path = stringMap.get("path");
		String port = stringMap.get("port");

		path = path == null || path.equals("/") ? "" : path;
		port = port == null ? "" : port;

		return protocol + "://" + host + ":" + port + path;
	}
	
	private static Map<String, String> parseStrings(List<String> rDataTokens) {
		Map<String, String> stringsMap = new HashMap<String, String>();
		for (String rToken : rDataTokens) {
			String[] splitToken = rToken.trim().split("=");
			stringsMap.put(splitToken[0], splitToken[1]);
		}
		return stringsMap;
	}

	public static void resolveAPISRV(final ModelCallback<String> apiAddressCallback, 
			final String domain) {
		new AsyncTask<Void, Void, String>() {

			private Throwable e;

			@Override
			protected String doInBackground(Void... params) {
				try {
					return resolveAPITXT(domain);
				} catch (Throwable e) {
					this.e = e;
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(String result) {
				if (e != null) {
					apiAddressCallback.error(e);
				} else {
					apiAddressCallback.success(result);
				}
			}
			
		}.execute();
	}
}
