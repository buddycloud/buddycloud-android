package com.buddycloud.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Base64;

import com.buddycloud.preferences.Preferences;

public class BuddycloudHTTPHelper {

	public static Executor THREAD_POOL = Executors.newCachedThreadPool();
	
	public static JSONObject get(String url, boolean auth, Activity parent) {
		return req("get", url, auth, null, parent);
	}

	public static JSONObject post(String url, boolean auth, HttpEntity entity, Activity parent) {
		return req("post", url, auth, entity, parent);
	}
	
	private static JSONObject req(final String methodType, final String url, 
			final boolean auth, final HttpEntity entity, final Activity parent) {
		
		final BlockingQueue<JSONObject> blockingBarrier = new ArrayBlockingQueue<JSONObject>(1);
		THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient(createConnectionManager(), null);
					HttpRequestBase method = null;
					if (methodType.equals("get")) {
						method = new HttpGet(url);
					} else if (methodType.equals("post")) {
						method = new HttpPost(url);
						if (entity != null) {
							((HttpPost)method).setEntity(entity);
						}
					}
					if (auth) {
						addAuthHeader(method, parent);
					}
					HttpResponse responseGet = client.execute(method);
					HttpEntity resEntityGet = responseGet.getEntity();
					if (resEntityGet != null) {
						String response = EntityUtils.toString(resEntityGet);
						JSONObject jsonObject = new JSONObject(response);
						blockingBarrier.offer(jsonObject);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		try {
			return blockingBarrier.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	protected static void addAuthHeader(HttpRequestBase method, Activity parent) {
		String loginPref = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);
        String passPref = Preferences.getPreference(parent, Preferences.PASSWORD);
        String auth = loginPref.split("@")[0] + ":" + passPref;
		method.setHeader("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP));
	}
	
	public static SingleClientConnManager createConnectionManager() {
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		return new SingleClientConnManager(new DefaultHttpClient().getParams(), registry);
	}
}
