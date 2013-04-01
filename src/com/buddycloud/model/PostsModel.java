package com.buddycloud.model;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;

public class PostsModel implements Model<JSONArray, JSONObject, String> {

	private static final String TAG = "PostsModel";
	private static PostsModel instance;
	private static final String ENDPOINT = "/content/posts?max=31"; 
	
	private Map<String, JSONArray> channelsPosts = new HashMap<String, JSONArray>();
	private Map<String, JSONArray> postsComments = new HashMap<String, JSONArray>();

	private PostsModel() {}

	public static PostsModel getInstance() {
		if (instance == null) {
			instance = new PostsModel();
		}
		return instance;
	}
	
	
	private boolean isPost(JSONObject item) {
		return item.opt("replyTo") == null;
	}

	@Override
	public void refresh(Activity context, final ModelCallback<JSONArray> callback, String... p) {
		if (p != null && p.length == 1) {
			channelsPosts.clear();
			postsComments.clear();
			final String channel = p[0];
			BuddycloudHTTPHelper.getArray(url(context, channel), context,
					new ModelCallback<JSONArray>() {

				private void parsePostsAndComments(JSONArray response) {
					JSONArray posts = new JSONArray();
					for (int i = 0; i < response.length(); i++) {
						JSONObject item = response.optJSONObject(i);
						
						if (isPost(item)) {
							posts.put(item);
						} else {
							String postId = item.optString("replyTo");
							JSONArray comments = postsComments.get(postId);
							if (comments == null) {
								comments = new JSONArray();
							}
							
							comments.put(item);
							postsComments.put(postId, comments);
						}
					}
					
					channelsPosts.put(channel, posts);
				}

				@Override
				public void success(JSONArray response) {
					parsePostsAndComments(response);
					if (callback != null) {
						callback.success(response);
					}
				}

				@Override
				public void error(Throwable throwable) {
					if (callback != null) {
						callback.error(throwable);
					}
				}
			});
		}
	}

	private static String url(Activity context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT;
	}

	@Override
	public void save(Activity context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		if (p == null || p.length < 1) {
			return;
		}
		
		try {
			Log.d(TAG, object.toString());
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(url(context, p[0]), true, false, requestEntity, context, callback);
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
		}
	}

	@Override
	public JSONArray get(Activity context, String... p) {
		if (p != null && p.length == 1) {
			String channelJid = p[0];
			if (channelsPosts.containsKey(channelJid)) {
				return channelsPosts.get(channelJid);
			}
		}
		
		return new JSONArray();
	}
	
	public JSONArray postsFromChannel(Activity context, String channel) {
		return get(context, channel);
	}
	
	public JSONArray commentsFromPost(String postId) {
		if (postId != null) {
			if (postsComments.containsKey(postId)) {
				return postsComments.get(postId);
			}
		}
		
		return new JSONArray();
	}
	
	public JSONObject getById(Activity context, String postId, String channel) {
		JSONArray jsonArray = get(context, channel);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject post = jsonArray.optJSONObject(i);
			if (post.optString("id").equals(postId)) {
				return post;
			}
		}
		return null;
	}
}
