package com.buddycloud.http;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;

public class BuddycloudHTTPHelper {
	
	private static final String TAG = "BuddycloudHTTPHelper";
	
	public static void getObject(String url, Context parent, 
			final ModelCallback<JSONObject> callback) {
		getObject(url, true, true, parent, callback);
	}

	public static void getArray(String url, Context parent, 
			final ModelCallback<JSONArray> callback) {
		getArray(url, true, true, parent, callback);
	}
	
	public static void post(String url, HttpEntity entity, Context parent, 
			final ModelCallback<JSONObject> callback) {
		post(url, true, true, entity, parent, callback);
	}

	public static void getObject(String url, boolean auth, boolean acceptsJSON, Context parent, 
			final ModelCallback<JSONObject> callback) {
		reqObject("get", url, auth, acceptsJSON, null, parent, callback);
	}

	public static void getArray(String url, boolean auth, boolean acceptsJSON, Context parent, 
			final ModelCallback<JSONArray> callback) {
		reqArray("get", url, auth, acceptsJSON, null, parent, callback);
	}
	
	public static void post(String url, boolean auth, boolean acceptsJSON, HttpEntity entity, Context parent, 
			final ModelCallback<JSONObject> callback) {
		reqObject("post", url, auth, acceptsJSON, entity, parent, callback);
	}
	
	private static void reqObject(String method, String url, boolean auth, boolean acceptsJSON, 
			HttpEntity entity, Context parent, ModelCallback<JSONObject> callback) {
		new RequestAsyncTask<JSONObject>(method, url, entity, auth, acceptsJSON, parent, callback) {
			@Override
			protected JSONObject toJSON(String responseStr) throws JSONException {
				return new JSONObject(responseStr);
			}
		}.execute();
	}	

	private static void reqArray(String method, String url, boolean auth, boolean acceptsJSON, 
			HttpEntity entity, Context parent, ModelCallback<JSONArray> callback) {
		new RequestAsyncTask<JSONArray>(method, url, entity, auth, acceptsJSON, parent, callback) {
			@Override
			protected JSONArray toJSON(String responseStr) throws JSONException {
				return new JSONArray(responseStr);
			}
		}.execute();
	}
	
	protected static void addAcceptJSONHeader(HttpRequestBase method) {
		method.setHeader("Accept", "application/json");
	}
	
	protected static void addAuthHeader(HttpRequestBase method, Context parent) {
		String loginPref = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);
        String passPref = Preferences.getPreference(parent, Preferences.PASSWORD);
        String auth = loginPref.split("@")[0] + ":" + passPref;
		method.setHeader("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP));
	}
	
	public static HttpClient createHttpClient() {
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		SchemeRegistry registry = new SchemeRegistry();
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		registry.register(new Scheme("https", socketFactory, 443));
		SingleClientConnManager connManager = new SingleClientConnManager(new DefaultHttpClient().getParams(), registry);
		return new DefaultHttpClient(connManager, null);
	}
	
	private static abstract class RequestAsyncTask<T extends Object> extends AsyncTask<Void, Void, Object> {

		private String methodType;
		private String url;
		private HttpEntity entity;
		private boolean auth;
		private boolean acceptsJSON;
		private Context parent;
		private ModelCallback<T> callback;
		
		public RequestAsyncTask(String methodType, String url, HttpEntity entity,
				boolean auth, boolean acceptsJSON, Context parent,
				ModelCallback<T> callback) {
			this.methodType = methodType;
			this.url = url;
			this.entity = entity;
			this.auth = auth;
			this.acceptsJSON = acceptsJSON;
			this.parent = parent;
			this.callback = callback;
		}

		@Override
		protected Object doInBackground(Void... params) {
			try {
				HttpClient client = createHttpClient();
				HttpRequestBase method = null;
				if (methodType.equals("get")) {
					method = new HttpGet(url);
					method.setHeader("Accept", "application/json");
				} else if (methodType.equals("post")) {
					method = new HttpPost(url);
					if (entity != null) {
						((HttpPost)method).setEntity(entity);
					}
				}
				if (acceptsJSON) {
					addAcceptJSONHeader(method);
				}
				if (auth) {
					addAuthHeader(method, parent);
				}
				HttpResponse response = client.execute(method);
				HttpEntity resEntityGet = ((HttpResponse)response).getEntity();
				String responseStr = EntityUtils.toString(resEntityGet);
				
				return responseStr;
			} catch (Throwable e) {
				Log.e(TAG, e.getLocalizedMessage(), e);
				return e;
			}
		}
		
		@Override
		protected void onPostExecute(Object response) {
			if (response instanceof Throwable) {
				callback.error((Throwable) response);
			} else {
				try {
					T jsonResponse = (T) toJSON(response.toString());
					callback.success(jsonResponse);
				} catch (Throwable e) {
					callback.error(e);
				}
			}
		}
		
		protected abstract T toJSON(String responseStr) throws JSONException;
		
	}
}
