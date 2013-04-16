package com.buddycloud.model;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.dao.PostsDAO;
import com.buddycloud.model.db.PostsTableHelper;
import com.buddycloud.preferences.Preferences;

public class PostsModel implements Model<JSONArray, JSONObject, String> {

	private static final String TAG = "PostsModel";
	private static PostsModel instance;
	private static final int PAGE_SIZE = 31;
	private static final String ENDPOINT = "/content/posts"; 
	
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
	
	private void parsePosts(PostsDAO dao, String channel, JSONArray response, boolean updateDatabase) {
		JSONArray posts = channelsPosts.get(channel);
		if (posts == null) {
			posts = new JSONArray();
		}
		
		for (int i = 0; i < response.length(); i++) {
			JSONObject item = response.optJSONObject(i);
			
			if (updateDatabase) {
				dao.insert(channel, item);
			}
			
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
	
	private void lookupDatabase(PostsDAO dao, String channel, ModelCallback<JSONArray> callback) {
		JSONArray response = dao.get(channel, PAGE_SIZE);
		if (response != null && response.length() > 0) {
			parsePosts(dao, channel, response, false);
			if (callback != null) {
				callback.success(response);
			}
		}
	}

	@Override
	public void refresh(Context context, final ModelCallback<JSONArray> callback, String... p) {
		if (p != null && p.length == 1) {
			channelsPosts.clear();
			postsComments.clear();
			final String channel = p[0];
			
			// Lookup for posts at database
			final PostsDAO dao = PostsDAO.getInstance(context);
			lookupDatabase(dao, channel, callback);
			
			BuddycloudHTTPHelper.getArray(url(context, channel), context,
					new ModelCallback<JSONArray>() {

				private void parsePostsAndComments(JSONArray response) {
					parsePosts(dao, channel, response, true);
					
					if (callback != null) {
						callback.success(response);
					}
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
	
	/*private int loadedItemsSize(String channel) {
		// Total posts and comments already loaded from a channel
		int totalItems = 0;
		
		JSONArray posts = channelsPosts.get(channel);
		if (posts != null) {
			totalItems += posts.length();

			for (int i = 0; i < posts.length(); i++) {
				String postId = posts.optJSONObject(i).optString(PostsTableHelper.COLUMN_ID);
				JSONArray comments = postsComments.get(postId);
				
				if (comments != null) {
					totalItems += comments.length();
				}
			}
		}
		
		return totalItems;
	}*/
	
	public static Date fromISODateString(String isoDateString) throws Exception {
		String isoFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	    DateFormat format = new SimpleDateFormat(isoFormat, Locale.getDefault());

	    return format.parse(isoDateString);
	}
	
	private JSONObject mostRecentItem(String channel) {
		JSONArray posts = channelsPosts.get(channel);
		
		if (posts != null) {
			JSONObject mostRecentPost = posts.optJSONObject(0);
			JSONArray comments = postsComments.get(mostRecentPost.optString(PostsTableHelper.COLUMN_ID));
			
			if (comments != null) {
				JSONObject mostRecentComment = comments.optJSONObject(0);
				try {
					Date postDate = fromISODateString(mostRecentPost.optString(PostsTableHelper.COLUMN_UPDATED));
					Date commentDate = fromISODateString(mostRecentComment.optString(PostsTableHelper.COLUMN_UPDATED));

					return postDate.compareTo(commentDate) > 0 ? mostRecentPost : mostRecentComment;
				} catch (Exception e) {}
			}
		}
		
		return null;
	}

	private String url(Context context, String channel) {
		JSONObject mostRecentItem = mostRecentItem(channel);
		String params = mostRecentItem != null ? "?after=" + mostRecentItem.optString(PostsTableHelper.COLUMN_UPDATED) : "?max=" + PAGE_SIZE;
		
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT + params;
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
			BuddycloudHTTPHelper.post(url(context, p[0]), true, false, requestEntity, context, callback);
		} catch (UnsupportedEncodingException e) {
			callback.error(e);
		}
	}

	@Override
	public JSONArray get(Context context, String... p) {
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
