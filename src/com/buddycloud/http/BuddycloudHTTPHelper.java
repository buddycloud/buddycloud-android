package com.buddycloud.http;

import java.security.KeyStore;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;

public class BuddycloudHTTPHelper {
	
	private static final Executor EXECUTOR = Executors.newFixedThreadPool(20);
	private static final String TAG = "BuddycloudHTTPHelper";
	private static HttpClient client = null;
	
	private static HttpClient getClient(Context context) {
		if (client == null) {
			client = createHttpClient(context);
		}
		return client;
	}
	
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

	public static void put(String url, HttpEntity entity, Context parent, 
			final ModelCallback<JSONObject> callback) {
		reqObject("put", url, true, true, entity, parent, callback);
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
	
	public static void post(String url, Map<String, String> headers, HttpEntity entity, Context parent, 
			final ModelCallback<JSONObject> callback) {
		reqObject("post", url, headers, entity, parent, callback);
	}
	
	public static void delete(String url, boolean auth, boolean acceptsJSON, Context parent, 
			final ModelCallback<JSONObject> callback) {
		reqObject("delete", url, auth, acceptsJSON, null, parent, callback);
	}
	
	public static void reqStatus(String url, boolean auth, 
			Context parent, ModelCallback<Integer> callback) {
		RequestAsyncTask<Integer> task = new RequestAsyncTask<Integer>("get", url, null, auth, 
				false, parent, callback) {
					@Override
					protected Integer toJSON(String responseStr) throws JSONException {
						return Integer.valueOf(responseStr);
					}
				};
		task.returnCodeOnly = true;
		task.executeOnExecutor(EXECUTOR);
	}
	
	public static void checkSSL(String url, Context parent, ModelCallback<Integer> callback) {
		RequestAsyncTask<Integer> task = new RequestAsyncTask<Integer>("get", url, null, false, 
				false, parent, callback) {
					@Override
					protected Integer toJSON(String responseStr) throws JSONException {
						return Integer.valueOf(responseStr);
					}
				};
		task.client = createSecureHttpClient();
		task.returnCodeOnly = true;
		task.executeOnExecutor(EXECUTOR);
	}
	
	private static void reqObject(String method, String url, boolean auth, boolean acceptsJSON, 
			HttpEntity entity, Context parent, ModelCallback<JSONObject> callback) {
		new RequestAsyncTask<JSONObject>(method, url, entity, auth, acceptsJSON, parent, callback) {
			@Override
			protected JSONObject toJSON(String responseStr) throws JSONException {
				if (responseStr == null || responseStr.length() == 0 || responseStr.equals("OK")) {
					return new JSONObject();
				}
				return new JSONObject(responseStr);
			}
		}.executeOnExecutor(EXECUTOR);
	}

	private static void reqObject(String method, String url, Map<String, String> headers, 
			HttpEntity entity, Context parent, ModelCallback<JSONObject> callback) {
		RequestAsyncTask<JSONObject> task = new RequestAsyncTask<JSONObject>(method, url, entity, 
				false, false, parent, callback) {
			@Override
			protected JSONObject toJSON(String responseStr) throws JSONException {
				if (responseStr == null || responseStr.length() == 0 || responseStr.equals("OK")) {
					return new JSONObject();
				}
				return new JSONObject(responseStr);
			}
		};
		task.headers = headers;
		task.executeOnExecutor(EXECUTOR);
	}
	
	private static void reqArray(String method, String url, boolean auth, boolean acceptsJSON, 
			HttpEntity entity, Context parent, ModelCallback<JSONArray> callback) {
		new RequestAsyncTask<JSONArray>(method, url, entity, auth, acceptsJSON, parent, callback) {
			@Override
			protected JSONArray toJSON(String responseStr) throws JSONException {
				return new JSONArray(responseStr);
			}
		}.executeOnExecutor(EXECUTOR);
	}
	
	protected static void addAcceptJSONHeader(HttpRequestBase method) {
		method.setHeader("Accept", "application/json");
	}
	
	protected static void addAuthHeader(HttpRequestBase method, Context parent) {
		String loginPref = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);
        String passPref = Preferences.getPreference(parent, Preferences.PASSWORD);
        String auth = loginPref.split("@")[0] + ":" + passPref;
		String authToken = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
		method.setHeader("Authorization", "Basic " + authToken);
	}
	
	public static String getAuthHeader(Context parent, String password) {
		String loginPref = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);
        String auth = loginPref.split("@")[0] + ":" + password;
		String authToken = Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP);
		return "Basic " + authToken;
	}
	
	protected static void addUserAgentHeader(HttpRequestBase method, Context parent) {
		try {
			PackageInfo pInfo = parent.getPackageManager().getPackageInfo(
					parent.getPackageName(), 0);
			method.setHeader("User-Agent", "buddycloud for Android v" + pInfo.versionCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static HttpClient createSecureHttpClient() {
		try {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			ClientConnectionManager ccm = new SingleClientConnManager(
					new DefaultHttpClient().getParams(), registry);
			return new DefaultHttpClient(ccm, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static HttpClient createHttpClient(Context context) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SchemeRegistry registry = new SchemeRegistry();
			
			SSLSocketFactory socketFactory = new AndroidInsecureSSLSocketFactory(trustStore, context);
			socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
			
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", socketFactory, 443));
			
			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					new DefaultHttpClient().getParams(), registry);
			
			DefaultHttpClient client = new DefaultHttpClient(ccm, null);
			client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
			
			return client;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isError(int statusCode) {
		return statusCode >= 400;
	}
	
	private static abstract class RequestAsyncTask<T extends Object> extends AsyncTask<Void, Void, Object> {

		private String methodType;
		private String url;
		private HttpEntity entity;
		private boolean auth;
		private boolean acceptsJSON;
		private Context parent;
		private ModelCallback<T> callback;
		private boolean returnCodeOnly;
		private HttpClient client;
		private Map<String, String> headers;
		
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
			this.client = getClient(parent);
		}

		@Override
		protected Object doInBackground(Void... params) {
			try {
				long t = System.currentTimeMillis();
				HttpRequestBase method = null;
				if (methodType.equals("get")) {
					method = new HttpGet(url);
					method.setHeader("Accept", "application/json");
				} else if (methodType.equals("post") || methodType.equals("put")) {
					if (methodType.equals("post")) {
						method = new HttpPost(url);
					} else {
						method = new HttpPut(url);
					}
					if (entity != null) {
						((HttpEntityEnclosingRequestBase)method).setEntity(entity);
					}
				} else if (methodType.equals("delete")) {
					method = new HttpDelete(url);
				}
				if (acceptsJSON) {
					addAcceptJSONHeader(method);
				}
				if (auth) {
					addAuthHeader(method, parent);
				}
				
				addUserAgentHeader(method, parent);
				
				if (headers != null) {
					for (Entry<String, String> header : headers.entrySet()) {
						method.setHeader(header.getKey(), header.getValue());
					}
				}
				
				HttpResponse response = client.execute(method);
				Log.d(TAG, "HTTP: {M: " + methodType + ", U: " + url + ", T: " + (System.currentTimeMillis() - t) + "}");
				
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (isError(statusCode)) {
					// Make sure entity is consumed (released) so connection can be re-used
					// this avoids the SingleClientConnManager warning about invalid status connection not released
					response.getEntity().consumeContent();
					return new Exception(response.getStatusLine().toString());
				}
				
				if (returnCodeOnly) {
					return statusCode;
				}
				
				HttpEntity resEntityGet = ((HttpResponse)response).getEntity();
				if (resEntityGet == null) {
					return "";
				}
				
				String responseStr = EntityUtils.toString(resEntityGet, "utf-8");
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
