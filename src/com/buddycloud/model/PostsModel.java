package com.buddycloud.model;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.PostsDAO;
import com.buddycloud.preferences.Preferences;

public class PostsModel extends AbstractModel<JSONArray, JSONObject, String> {

	private static final String TAG = PostsModel.class.getName();
	private static PostsModel instance;
	private static final int REMOTE_PAGE_SIZE = 30;
	private static final int LOCAL_PAGE_SIZE = 30;
	private static final String POSTS_ENDPOINT = "/content/posts";
	
	private PostsModel() {}

	public static PostsModel getInstance() {
		if (instance == null) {
			instance = new PostsModel();
		}
		return instance;
	}
	
	private boolean persist(PostsDAO postsDAO, String channel, JSONArray jsonPosts) throws JSONException {
		boolean containsTopic = false;
		for (int i = 0; i < jsonPosts.length(); i++) {
			JSONObject item = jsonPosts.optJSONObject(i);
			normalize(item);
			if (postsDAO.get(channel, item.optString("id")) == null) {
				updateTopicTimestamp(postsDAO, channel, item);
				postsDAO.insert(channel, item);
			} else {
				postsDAO.update(channel, item);
			}
			if (!isComment(item)) {
				containsTopic = true;
			}
		}
		return containsTopic;
	}

	private void updateTopicTimestamp(PostsDAO postsDAO, String channel,
			JSONObject item) throws JSONException {
		String itemId = item.optString("id");
		if (isComment(item)) {
			JSONObject parent = postsDAO.get(channel, item.optString("replyTo"));
			if (parent != null) {
				parent.putOpt("updated", item.optString("updated"));
				postsDAO.update(channel, parent);
			}
		} else {
			JSONArray replies = postsDAO.getReplies(channel, itemId);
			if (replies != null && replies.length() > 0) {
				JSONObject lastReply = replies.optJSONObject(replies.length() - 1);
				item.putOpt("updated", lastReply.optString("updated"));
			}
		}
	}
	
	private void normalize(JSONObject item) {
		String author = item.optString("author");
		if (author.contains("acct:")) {
			String[] split = author.split(":");
			author = split[1];
			try {
				item.put("author", author);
			} catch (JSONException e) {}
		}
	}
	
	private JSONArray lookupPostsFromDatabase(final Context context, final String channelJid, String after) {
		final PostsDAO postsDAO = PostsDAO.getInstance(context);
		JSONArray postStream = postsDAO.get(channelJid, after, LOCAL_PAGE_SIZE);
		for (int i = 0; i < postStream.length(); i++) {
			JSONObject eachPost = postStream.optJSONObject(i);
			JSONArray replies = postsDAO.getReplies(channelJid,
					eachPost.optString("id"));
			try {
				eachPost.putOpt("replies", replies);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}
		return postStream;
	}
	
	private boolean isComment(JSONObject item) {
		return item.has("replyTo");
	}
	
	@Override
	public JSONArray getFromCache(Context context, String... p) {
		String channelJid = p[0];
		String after = null;
		if (p.length > 1) {
			after = p[1];
		}
		return lookupPostsFromDatabase(context, channelJid, after);
	}

	public void fetchSinglePost(Context context, final ModelCallback<JSONObject> callback, String... p) {
		String channelJid = p[0];
		String itemId = p[1];
		fetchPost(context, channelJid, itemId, callback);
	}
	
	private void fetchPost(final Context context, final String channelJid, 
			final String itemId, final ModelCallback<JSONObject> callback) {
		BuddycloudHTTPHelper.getObject(postUrl(context, channelJid, itemId), context,
				new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				normalize(response);
				fetchReplies(context, channelJid, response, callback);
			}

			@Override
			public void error(Throwable throwable) {
				if (callback != null) {
					callback.error(throwable);
				}
			}
		});
	}
	
	private void fetchReplies(final Context context, final String channelJid, 
			final JSONObject item, final ModelCallback<JSONObject> callback) {
		String itemId = item.optString("id");
		BuddycloudHTTPHelper.getArray(repliesUrl(context, channelJid, itemId), context,
				new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					JSONObject reply = response.optJSONObject(i);
					normalize(reply);
				}
				try {
					item.putOpt("replies", response);
					persist(PostsDAO.getInstance(context), channelJid, response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if (callback != null) {
					callback.success(item);
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

	private void fetchPosts(final Context context, final String channelJid, 
			final ModelCallback<Void> callback, String after) {
		BuddycloudHTTPHelper.getArray(postsUrl(context, channelJid, after), context,
				new ModelCallback<JSONArray>() {

			@Override
			public void success(JSONArray response) {
				try {
					final PostsDAO postsDAO = PostsDAO.getInstance(context);
					if (!persist(postsDAO, channelJid, response) && response.length() > 0) {
						JSONObject lastReply = response.optJSONObject(response.length() - 1);
						fetchPosts(context, channelJid, callback, lastReply.optString("id"));
					} else {
						callback.success(null);
					}
				} catch (Exception e) {
					error(e);
				}
			}

			@Override
			public void error(Throwable throwable) {
				callback.error(throwable);
			}
		});
	}
	
	private String postsUrl(Context context, String channel, String after) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		String postsURL = apiAddress + "/" + channel + POSTS_ENDPOINT + "?max=" + REMOTE_PAGE_SIZE;
		if (after != null) {
			postsURL += "&after=" + after;
		}
		return postsURL;
	}
	
	private String postsUrl(Context context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + POSTS_ENDPOINT + "?max=" + REMOTE_PAGE_SIZE;
	}
	
	private String postUrl(Context context, String channel, String postId) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + POSTS_ENDPOINT + "/" + postId;
	}
	
	private String repliesUrl(Context context, String channel, String postId) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + POSTS_ENDPOINT + "/" + postId + "/replyto";
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		if (p == null || p.length < 1) {
			return;
		}
		
		try {
			Log.d(TAG, object.toString());
			StringEntity requestEntity = new StringEntity(object.toString(), "UTF-8");
			requestEntity.setContentType("application/json");
			BuddycloudHTTPHelper.post(postsUrl(context, p[0]), true, false, requestEntity, context, callback);
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
		}
	}

	@Override
	public void getFromServer(Context context, ModelCallback<JSONArray> callback,
			String... p) {
		// TODO Auto-generated method stub
		
	}

	public void fillMore(Context context, ModelCallback<Void> callback, String... p) {
		String channelJid = p[0];
		String oldestPostId = p[1];
		fetchPosts(context, channelJid, callback, oldestPostId);
	}
	
	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		String channelJid = p[0];
		fetchPosts(context, channelJid, callback, null);
	}
}