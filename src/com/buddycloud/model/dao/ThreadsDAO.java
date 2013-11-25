package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.model.db.ThreadsTableHelper;
import com.buddycloud.model.db.UnreadCountersTableHelper;

public class ThreadsDAO implements DAO<JSONObject, JSONObject> {

	private static ThreadsDAO instance;
	
	private SQLiteDatabase db;
	private BuddycloudSQLiteOpenHelper helper;
	
	private final String[] COLUMNS = new String[] {
			ThreadsTableHelper.COLUMN_ID, 
			ThreadsTableHelper.COLUMN_UPDATED, 
			ThreadsTableHelper.COLUMN_CHANNEL};
	
	private ContentValues buildValues(JSONObject json) {
		String threadId = json.optString("id");
		String threadUpdated = json.optString("updated");
		String channel = json.optString("channel");
		
		ContentValues values = new ContentValues();
		values.put(ThreadsTableHelper.COLUMN_ID, threadId);
		values.put(ThreadsTableHelper.COLUMN_UPDATED, threadUpdated);
		values.put(ThreadsTableHelper.COLUMN_CHANNEL, channel);
		return values;
	}
	
	private ThreadsDAO(Context context) {
		this.helper = new BuddycloudSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	public static ThreadsDAO getInstance(Context context) {
		if (instance == null) {
			instance = new ThreadsDAO(context);
		}
		return instance;
	}

	@Override
	public boolean insert(String threadId, JSONObject json) {
		ContentValues values = buildValues(json);
		long rowId = db.insert(ThreadsTableHelper.TABLE_NAME, null, values);
		return rowId != -1;
	}
	
	public JSONObject getNewest(String channel) {
		String filter = ThreadsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"";
		String orderBy = "datetime(" + ThreadsTableHelper.COLUMN_UPDATED + ") DESC";
		return DAOHelper.queryUniqueOnSameThread(db, false, ThreadsTableHelper.TABLE_NAME, null, filter,
				null, null, null, orderBy, String.valueOf(1), 
				cursorParser());
	}

	@Override
	public boolean update(String threadId, JSONObject json) {
		ContentValues values = buildValues(json);
		String channel = json.optString("channel");
		String filter = ThreadsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"" + " AND " +
				ThreadsTableHelper.COLUMN_ID + "=\"" + threadId + "\"";
		int rowsAffected = db.update(ThreadsTableHelper.TABLE_NAME, values, filter, null);
		return rowsAffected > 0;
	}
	
	private DAOCursorParser cursorParser() {
		DAOCursorParser cursorParser = new DAOCursorParser() {
			@Override
			public JSONObject parse(Cursor c) {
				return cursorToJSON(c);
			}
		};
		return cursorParser;
	}
	
	private JSONObject cursorToJSON(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put("id", getString(cursor, UnreadCountersTableHelper.COLUMN_ID));
			json.put("channel", getString(cursor, ThreadsTableHelper.COLUMN_CHANNEL));
			json.put("updated", getString(cursor, ThreadsTableHelper.COLUMN_UPDATED));
		} catch (JSONException e) {
			return null;
		}
		return json;
	}

	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}
	
	public JSONObject get(String threadId, String channel) {
		String filter = ThreadsTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"" + " AND " +
				ThreadsTableHelper.COLUMN_ID + "=\"" + threadId + "\"";
		return DAOHelper.queryUniqueOnSameThread(db, false, ThreadsTableHelper.TABLE_NAME, COLUMNS, filter,
				null, null, null, null, null, cursorParser());
	}
	
	@Override
	public JSONObject get(String threadId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, JSONObject> getAll() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
