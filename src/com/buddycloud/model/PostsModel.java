package com.buddycloud.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.DAOCallback;
import com.buddycloud.model.dao.PostsDAO;
import com.buddycloud.preferences.Preferences;

public class PostsModel implements Model<List<String>, JSONObject, String> {

	private static final String TAG = PostsModel.class.getName();
	private static PostsModel instance;
	private static final int PAGE_SIZE = 31;
	private static final String POSTS_ENDPOINT = "/content/posts";
	
	private Map<String, List<String>> channelStreams = new HashMap<String, List<String>>();
	private Map<String, JSONObject> posts = new HashMap<String, JSONObject>();
	private Map<String, List<JSONObject>> postsComments = new HashMap<String, List<JSONObject>>();
	
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
	
	private void parse(PostsDAO postsDAO, String channelJid, JSONArray response, boolean updateDatabase) {
		parseChannelPosts(postsDAO, channelJid, response, updateDatabase);
	}
	
	public void parseChannelPosts(PostsDAO postsDAO, String channel, JSONArray jsonPosts, boolean updateDatabase) {
		List<String> stream = channelStreams.get(channel);
		if (stream == null) {
			stream = new ArrayList<String>();
		}
		channelStreams.put(channel, stream);
		
		for (int i = 0; i < jsonPosts.length(); i++) {
			JSONObject item = jsonPosts.optJSONObject(i);
			String postId = item.optString("id");
			
			normalize(item);
			if (updateDatabase) {
				postsDAO.insert(channel, item);
			}
			
			if (isPost(item)) {
				stream.add(postId);
			} else {
				addReply(item);
			}
			this.posts.put(postId, item);
		}
		
	}

	private void addReply(JSONObject item) {
		String origPostId = item.optString("replyTo");
		List<JSONObject> comments = postsComments.get(origPostId);
		if (comments == null) {
			comments = new ArrayList<JSONObject>();
		}
		comments.add(0, item);
		postsComments.put(origPostId, comments);
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
	
	private void lookupPostsFromDatabase(final Context context, 
			final String channelJid, 
			final ModelCallback<List<String>> callback) {
		final PostsDAO postsDAO = PostsDAO.getInstance(context);
		
		DAOCallback<JSONArray> postCallback = new DAOCallback<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
				if (response != null && response.length() > 0) {
					parseChannelPosts(postsDAO, channelJid, response, false);
				}
				fetchPosts(context, channelJid, callback);
			}
		};
		postsDAO.get(channelJid, PAGE_SIZE, postCallback);
	}
	
	@Override
	public void refresh(Context context, final ModelCallback<List<String>> callback, String... p) {
		String channelJid = p[0];
		expire(channelJid);
		lookupPostsFromDatabase(context, channelJid, callback);
	}

	public void refreshPost(Context context, final ModelCallback<List<String>> callback, String... p) {
		String channelJid = p[0];
		String itemId = p[1];
		fetchPost(context, channelJid, itemId, callback);
	}
	
	public void fetchPost(final Context context, final String channelJid, 
			final String itemId, final ModelCallback<List<String>> callback) {
		BuddycloudHTTPHelper.getObject(postUrl(context, channelJid, itemId), context,
				new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				normalize(response);
				posts.put(response.optString("id"), response);
				fetchComments(context, channelJid, itemId, callback);
			}

			@Override
			public void error(Throwable throwable) {
				if (callback != null) {
					callback.error(throwable);
				}
			}
		});
	}
	
	private void fetchComments(final Context context, final String channelJid, 
			final String itemId, final ModelCallback<List<String>> callback) {
		BuddycloudHTTPHelper.getArray(repliesUrl(context, channelJid, itemId), context,
				new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				for (int i = 0; i < response.length(); i++) {
					JSONObject reply = response.optJSONObject(i);
					normalize(reply);
					addReply(reply);
				}
				
				if (callback != null) {
					callback.success(null);
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
			final ModelCallback<List<String>> callback) {
		BuddycloudHTTPHelper.getArray(postsUrl(context, channelJid), context,
				new ModelCallback<JSONArray>() {

			@Override
			public void success(JSONArray response) {
				final PostsDAO postsDAO = PostsDAO.getInstance(context);
				parse(postsDAO, channelJid, response, true);
				if (callback != null) {
					callback.success(channelStreams.get(channelJid));
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
	
	private String postsUrl(Context context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + POSTS_ENDPOINT + "?max=" + PAGE_SIZE;
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
	public List<String> get(Context context, String... p) {
		if (p != null && p.length == 1) {
			String channelJid = p[0];
			if (channelStreams.containsKey(channelJid)) {
				return channelStreams.get(channelJid);
			}
		}
		return new ArrayList<String>();
	}
	
	public List<String> cachedPostsFromChannel(String channel) {
		if (channel != null) {
			if (channelStreams.containsKey(channel)) {
				return channelStreams.get(channel);
			}
		}
		return new ArrayList<String>();
	}
	
	public Set<String> cachedChannels() {
		return channelStreams.keySet();
	}
	
	public void expire() {
		channelStreams.clear();
		postsComments.clear();
	}
	
	public void expire(String channelJid) {
		Iterator<Entry<String, List<JSONObject>>> iterator = postsComments.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, List<JSONObject>> entry = iterator.next();
			String postId = entry.getKey();
			JSONObject postWithId = postWithId(postId, channelJid);
			if (postWithId != null) {
				iterator.remove();
			}
		}
		channelStreams.remove(channelJid);
	}
	
	public void expire(String channelJid, String itemId) {
		channelStreams.remove(channelJid);
	}
	
	public List<JSONObject> cachedCommentsFromPost(String postId) {
		if (postId != null) {
			if (postsComments.containsKey(postId)) {
				return postsComments.get(postId);
			}
		}
		
		return new ArrayList<JSONObject>();
	}
	
	public JSONObject postWithId(String postId, String channel) {
		return posts.get(postId);
	}
	
	public void selectChannel(Context context, String channel) {
		
	}
}