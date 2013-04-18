package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.model.db.PostsTableHelper;

public class PostsDAO implements DAO<JSONObject, JSONArray> {
	
	private static PostsDAO instance;
	
	private SQLiteDatabase db;
	private BuddycloudSQLiteOpenHelper helper;
	
	
	private PostsDAO(Context context) {
		this.helper = new BuddycloudSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	
	public static PostsDAO getInstance(Context context) {
		if (instance == null) {
			instance = new PostsDAO(context);
		}
		
		return instance;
	}

	
	private ContentValues buildValues(String id, String author, String published, String updated, 
			String content, String channel, String replyTo) {
		
		ContentValues values = new ContentValues();
		values.put(PostsTableHelper.COLUMN_ID, id);
		values.put(PostsTableHelper.COLUMN_AUTHOR, author);
		values.put(PostsTableHelper.COLUMN_PUBLISHED, published);
		values.put(PostsTableHelper.COLUMN_UPDATED, updated);
		values.put(PostsTableHelper.COLUMN_CONTENT, content);
		values.put(PostsTableHelper.COLUMN_CHANNEL, channel);

		if (replyTo != null && !replyTo.equals("")) {
			values.put(PostsTableHelper.COLUMN_REPLY_TO, replyTo);
		}
		
		return values;
	}
	
	private ContentValues buildValues(String channel, JSONObject json) {
		String id = json.optString("id");
		String author = json.optString("description");
		String published = json.optString("published");
		String updated = json.optString("updated");
		String content = json.optString("content");
		String replyTo = json.optString("replyTo");
		
		return buildValues(id, author, published, updated, content, channel, replyTo);
	}
	
	public boolean insert(String channel, JSONObject json) {
		ContentValues values = buildValues(channel, json);
		if (values != null) {
			long rowId = db.insert(PostsTableHelper.TABLE_NAME, null, values);
			return rowId != -1;
		}
		
		return false;
	}
	
	public boolean update(String channel, JSONObject json) {
		ContentValues values = buildValues(channel, json);
		
		if (values != null) {
			String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"" + " AND " +
							PostsTableHelper.COLUMN_ID + "=" + values.getAsString(PostsTableHelper.COLUMN_ID);
			int rowsAffected = db.update(PostsTableHelper.TABLE_NAME, 
					values, filter, null);
			return rowsAffected == 1;
		}
		
		return false;
	}
	
	public JSONArray get(String channel) {
		JSONArray channelPosts = new JSONArray();
		
		String filter = PostsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"";
		String orderBy = "date(" + PostsTableHelper.COLUMN_UPDATED + ") DESC";

		Cursor cursor = db.query(PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, orderBy);
		cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	JSONObject json = cursorToJSON(cursor);
	    	if (json != null) {
	    		channelPosts.put(json);
	    	}
	    	cursor.moveToNext();
	    }
		cursor.close();
		
		return channelPosts;
	}
	
	public JSONArray get(String channel, int limit) {
		JSONArray channelPosts = new JSONArray();
		
		String filter = PostsTableHelper.COLUMN_ID + "=\"" + channel + "\"";
		String orderBy = "date(" + PostsTableHelper.COLUMN_UPDATED + ") DESC";

		Cursor cursor = db.query(PostsTableHelper.TABLE_NAME, null, filter,
				null, null, null, orderBy, limit + "");
		
		cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	JSONObject json = cursorToJSON(cursor);
	    	if (json != null) {
	    		channelPosts.put(json);
	    	}
	    	cursor.moveToNext();
	    }
		cursor.close();
		
		return channelPosts;
	}
	
	public Map<String, JSONArray> getAll() {
		// TODO: select by channel, get() and then return
		return null;
	}
	
	private JSONObject cursorToJSON(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put(PostsTableHelper.COLUMN_ID, getString(cursor, PostsTableHelper.COLUMN_ID));
			json.put(PostsTableHelper.COLUMN_AUTHOR, getString(cursor, PostsTableHelper.COLUMN_AUTHOR));
			json.put(PostsTableHelper.COLUMN_PUBLISHED, getString(cursor, PostsTableHelper.COLUMN_PUBLISHED));
			json.put(PostsTableHelper.COLUMN_UPDATED, getString(cursor, PostsTableHelper.COLUMN_UPDATED));
			json.put(PostsTableHelper.COLUMN_CHANNEL, getString(cursor, PostsTableHelper.COLUMN_CHANNEL));
			json.put(PostsTableHelper.COLUMN_CONTENT, getString(cursor, PostsTableHelper.COLUMN_CONTENT));
			json.put(PostsTableHelper.COLUMN_REPLY_TO, getString(cursor, PostsTableHelper.COLUMN_REPLY_TO));
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}
}
