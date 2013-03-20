package com.buddycloud.utils;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import android.os.AsyncTask;

import com.buddycloud.model.ModelCallback;

public class DNSUtils {

	private static final String HTTP = "http://";
	private static final String HTTPS = "https://";
	private static final String SRV_PREFIX = "_buddycloud-api._tcp.";
	
	private static final int HTTPS_DEF_PORT = 443;
	
	/**
	 * Adapted from https://code.google.com/p/asmack/source/browse/src/custom/org/jivesoftware/smack/util/DNSUtil.java
	 * 
	 * @param domain
	 * @return
	 * @throws TextParseException
	 */
	private static String resolveAPISRV(String domain) throws TextParseException {
		String bestHost = null;
		int bestPort = -1;
		int bestPriority = Integer.MAX_VALUE;
		int bestWeight = 0;
		Lookup lookup = new Lookup(SRV_PREFIX + domain, Type.SRV);
		Record recs[] = lookup.run();
		if (recs == null) {
			throw new RuntimeException("Could not lookup domain.");
		}

		// TODO Should comply with http://tools.ietf.org/html/rfc2782
		for (Record rec : recs) {
			SRVRecord record = (SRVRecord) rec;
			if (record != null && record.getTarget() != null) {
				int weight = (int) (record.getWeight() * record.getWeight() * Math
						.random());
				if (record.getPriority() < bestPriority) {
					bestPriority = record.getPriority();
					bestWeight = weight;
					bestHost = record.getTarget().toString();
					bestPort = record.getPort();
				} else if (record.getPriority() == bestPriority) {
					if (weight > bestWeight) {
						bestPriority = record.getPriority();
						bestWeight = weight;
						bestHost = record.getTarget().toString();
						bestPort = record.getPort();
					}
				}
			}
		}
		
		if (bestHost == null) {
			throw new RuntimeException("Domain has no SRV records.");
		}
		// Host entries in DNS should end with a ".".
		if (bestHost.endsWith(".")) {
			bestHost = bestHost.substring(0, bestHost.length() - 1);
		}
		
		String prefix = HTTP;
		if (bestPort == HTTPS_DEF_PORT) {
			prefix = HTTPS;
		}
		
		return prefix + bestHost;
	}
	
	public static void resolveAPISRV(final ModelCallback<String> apiAddressCallback, 
			final String domain) {
		new AsyncTask<Void, Void, String>() {

			private Throwable e;

			@Override
			protected String doInBackground(Void... params) {
				try {
					return resolveAPISRV(domain);
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
