package com.buddycloud.http;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.util.Base64;

import com.buddycloud.preferences.Constants;

public class BuddycloudHTTPHelper {

	public static Executor THREAD_POOL = Executors.newCachedThreadPool();
	
	public static JSONObject get(String url, boolean auth, SharedPreferences preferences) {
		return req("get", url, auth, null, preferences);
	}

	public static JSONObject post(String url, boolean auth, HttpEntity entity, SharedPreferences preferences) {
		return req("post", url, auth, entity, preferences);
	}
	
	private static JSONObject req(final String methodType, final String url, 
			final boolean auth, final HttpEntity entity, final SharedPreferences preferences) {
		
		final BlockingQueue<JSONObject> blockingBarrier = new ArrayBlockingQueue<JSONObject>(1);
		THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();
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
						addAuthHeader(method, preferences);
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

	protected static void addAuthHeader(HttpRequestBase method, SharedPreferences preferences) {
		String loginPref = preferences.getString(Constants.MY_CHANNEL, null);
        String passPref = preferences.getString(Constants.PASSWORD, null);
        String auth = loginPref.split("@")[0] + ":" + passPref;
		method.setHeader("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP));
	}
	
}
