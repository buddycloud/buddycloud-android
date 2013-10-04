package com.buddycloud.utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import android.net.Uri;

import com.squareup.picasso.OkHttpDownloader;

public class PicassoDownloader extends OkHttpDownloader {

	static final String RESPONSE_SOURCE_ANDROID = "X-Android-Response-Source";
	static final String RESPONSE_SOURCE_OKHTTP = "OkHttp-Response-Source";

	final boolean skipCache;

	public PicassoDownloader(File arg0, long arg1, boolean skipCache) {
		super(arg0, arg1);
		getClient().setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		this.skipCache = skipCache;
	}

	@Override
	public Response load(Uri uri, boolean localCacheOnly) throws IOException {
		HttpURLConnection connection = openConnection(uri);
		if (!skipCache) {
			connection.setUseCaches(true);
		}

		if (skipCache) {
			connection.setRequestProperty("Cache-Control", "no-cache");
		} else if (localCacheOnly) {
			connection.setRequestProperty("Cache-Control",
					"only-if-cached;max-age=" + Integer.MAX_VALUE);
		}

		int responseCode = connection.getResponseCode();
		if (responseCode >= 300) {
			return null;
		}

		String responseSource = connection
				.getHeaderField(RESPONSE_SOURCE_OKHTTP);
		if (responseSource == null) {
			responseSource = connection.getHeaderField(RESPONSE_SOURCE_ANDROID);
		}
		boolean fromCache = parseResponseSourceHeader(responseSource);

		return new Response(connection.getInputStream(), fromCache);
	}

	static boolean parseResponseSourceHeader(String header) {
		if (header == null) {
			return false;
		}
		String[] parts = header.split(" ", 2);
		if ("CACHE".equals(parts[0])) {
			return true;
		}
		if (parts.length == 1) {
			return false;
		}
		try {
			return "CONDITIONAL_CACHE".equals(parts[0])
					&& Integer.parseInt(parts[1]) == 304;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
