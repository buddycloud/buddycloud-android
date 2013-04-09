package com.buddycloud.model.dao;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.UnreadCountersTableHelper;
import com.buddycloud.preferences.Preferences;

public class UnreadCountersDAO {
	
	private static UnreadCountersDAO instance;
	
	private SQLiteDatabase db;
	private UnreadCountersTableHelper helper;
	private String myJid;
	
	private final String[] COLUMNS = new String[]{
			UnreadCountersTableHelper.COLUMN_CHANNEL, 
			UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, 
			UnreadCountersTableHelper.COLUMN_TOTAL_COUNT};
	
	
	private UnreadCountersDAO(Context context) {
		this.helper = new UnreadCountersTableHelper(context);
		this.db = helper.getWritableDatabase();
		this.myJid = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
	}
	
	
	public static UnreadCountersDAO getInstance(Context context) {
		if (instance == null) {
			instance = new UnreadCountersDAO(context);
		}
		
		return instance;
	}

	
	private ContentValues buildValues(String channel, int mentionsCount, int totalCount) {
		ContentValues values = new ContentValues();
		values.put(UnreadCountersTableHelper.COLUMN_USER, myJid);
		values.put(UnreadCountersTableHelper.COLUMN_CHANNEL, channel);
		values.put(UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, mentionsCount);
		values.put(UnreadCountersTableHelper.COLUMN_TOTAL_COUNT, totalCount);
		
		return values;
	}
	
	private ContentValues buildValues(String channel, JSONObject counter) {
		int mentionsCount = counter.optInt("mentionsCount");
		int totalCount = counter.optInt("totalCount");
		return buildValues(channel, mentionsCount, totalCount);
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
					UnreadCountersTableHelper.COLUMN_CHANNEL + "=" + channel;
			int rowsAffected = db.update(UnreadCountersTableHelper.TABLE_NAME, 
					values, filter, null);
			return rowsAffected == 1;
		}
		
		return false;
	}
	
	public JSONObject get(String channel) {
		String filter = UnreadCountersTableHelper.COLUMN_USER + "=\"" + myJid + "\" AND " + 
				UnreadCountersTableHelper.COLUMN_CHANNEL + "=" + channel;
		Cursor cursor = db.query(UnreadCountersTableHelper.TABLE_NAME, COLUMNS, filter,
				null, null, null, null);
		cursor.moveToFirst();
		JSONObject json = cursorToJSONObject(cursor);
		cursor.close();
		
		return json;
	}
	
	public Map<String, JSONObject> getAll() {
		Map<String, JSONObject> map = new HashMap<String, JSONObject>();
		String filter = UnreadCountersTableHelper.COLUMN_USER + "=\"" + myJid + "\"";
		
		Cursor cursor = db.query(UnreadCountersTableHelper.TABLE_NAME,
				COLUMNS, filter, null, null, null, null);
		
		cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	JSONObject json = cursorToJSONObject(cursor);
	    	if (json != null) {
	    		map.put(cursor.getString(0), json);
	    	}
	    	cursor.moveToNext();
	    }
	    cursor.close();
	    
	    return map;
	}
	
	private JSONObject cursorToJSONObject(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put(UnreadCountersTableHelper.COLUMN_MENTIONS_COUNT, cursor.getInt(1));
			json.put(UnreadCountersTableHelper.COLUMN_TOTAL_COUNT, cursor.getInt(2));
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	/*private UnreadCounter cursorToUnreadCounter(Cursor cursor) {
	    return new UnreadCounter(cursor.getString(0), cursor.getInt(1), cursor.getInt(2));
	}*/
}
