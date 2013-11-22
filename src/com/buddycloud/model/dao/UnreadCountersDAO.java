package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.model.db.UnreadCountersTableHelper;
import com.buddycloud.preferences.Preferences;

public class UnreadCountersDAO implements DAO<JSONObject, JSONObject> {
	
	private static UnreadCountersDAO instance;
	
	private SQLiteDatabase db;
	private BuddycloudSQLiteOpenHelper helper;
	private String myJid;
	
	private final String[] COLUMNS = new String[] {
			UnreadCountersTableHelper.COLUMN_CHANNEL, 
			UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, 
			UnreadCountersTableHelper.COLUMN_TOTAL_COUNT,
			UnreadCountersTableHelper.COLUMN_REPLY_COUNT,
			UnreadCountersTableHelper.COLUMN_VISIT_COUNT,
			UnreadCountersTableHelper.COLUMN_LAST_WEEK_ACTIVITY};
	
	private UnreadCountersDAO(Context context) {
		this.helper = new BuddycloudSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
		this.myJid = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
	}
	
	public static UnreadCountersDAO getInstance(Context context) {
		if (instance == null) {
			instance = new UnreadCountersDAO(context);
		}
		
		return instance;
	}

	private ContentValues buildValues(String channel, int mentionsCount, int totalCount, 
			int replyCount, int visitCount, int lastWeekActivity) {
		ContentValues values = new ContentValues();
		values.put(UnreadCountersTableHelper.COLUMN_USER, myJid);
		values.put(UnreadCountersTableHelper.COLUMN_CHANNEL, channel);
		values.put(UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, mentionsCount);
		values.put(UnreadCountersTableHelper.COLUMN_TOTAL_COUNT, totalCount);
		values.put(UnreadCountersTableHelper.COLUMN_REPLY_COUNT, replyCount);
		values.put(UnreadCountersTableHelper.COLUMN_VISIT_COUNT, visitCount);
		values.put(UnreadCountersTableHelper.COLUMN_LAST_WEEK_ACTIVITY, lastWeekActivity);
		
		return values;
	}
	
	private ContentValues buildValues(String channel, JSONObject counter) {
		int mentionsCount = counter.optInt("mentionsCount");
		int totalCount = counter.optInt("totalCount");
		int replyCount = counter.optInt("replyCount");
		int visitCount = counter.optInt("visitCount");
		int lastWeekActivity = counter.optInt("lastWeekActivity");
		return buildValues(channel, mentionsCount, totalCount, replyCount, 
				visitCount, lastWeekActivity);
	}
	
	public void delete(String channelJid) {
		String filter = UnreadCountersTableHelper.COLUMN_USER + "=\"" + myJid + "\" AND " + 
				UnreadCountersTableHelper.COLUMN_CHANNEL + "=\"" + channelJid + "\"";
		db.delete(UnreadCountersTableHelper.TABLE_NAME, filter, null);
	}
	
	public boolean insert(String channel, JSONObject counter) {
		ContentValues values = buildValues(channel, counter);
		if (values != null) {
			long rowId = db.insert(UnreadCountersTableHelper.TABLE_NAME, null, values);
			return rowId != -1;
		}
		
		return false;
	}
	
	public boolean update(String channel, JSONObject counter) {
		ContentValues values = buildValues(channel, counter);
		if (values != null) {
			String filter = UnreadCountersTableHelper.COLUMN_USER + "=\"" + myJid + "\" AND " + 
					UnreadCountersTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"";
			int rowsAffected = db.update(UnreadCountersTableHelper.TABLE_NAME, 
					values, filter, null);
			return rowsAffected == 1;
		}
		
		return false;
	}
	
	public JSONObject get(String channel) {
		String filter = UnreadCountersTableHelper.COLUMN_USER + "=\"" + myJid + "\" AND " + 
				UnreadCountersTableHelper.COLUMN_CHANNEL + "=\"" + channel + "\"";
		return DAOHelper.queryUniqueOnSameThread(db, false, UnreadCountersTableHelper.TABLE_NAME, COLUMNS, filter,
				null, null, null, null, null, cursorParser());
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
	
	public Map<String, JSONObject> getAll() {
		String filter = UnreadCountersTableHelper.COLUMN_USER + "=\"" + myJid + "\"";
		return DAOHelper.queryMapOnSameThread(db, false, UnreadCountersTableHelper.TABLE_NAME,
				COLUMNS, filter, null, null, null, null, null, cursorParser(), 
				UnreadCountersTableHelper.COLUMN_CHANNEL);
	}
	
	private JSONObject cursorToJSON(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put(UnreadCountersTableHelper.COLUMN_CHANNEL, 
					getString(cursor, UnreadCountersTableHelper.COLUMN_CHANNEL));
			json.put(UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, 
					getInt(cursor, UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT));
			json.put(UnreadCountersTableHelper.COLUMN_TOTAL_COUNT, 
					getInt(cursor, UnreadCountersTableHelper.COLUMN_TOTAL_COUNT));
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	private int getInt(Cursor cursor, String columnName) {
		return cursor.getInt(cursor.getColumnIndex(columnName));
	}
	
	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}

}
