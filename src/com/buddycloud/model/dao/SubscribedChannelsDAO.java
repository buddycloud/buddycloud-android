package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.model.db.SubscribedChannelsTableHelper;
import com.buddycloud.preferences.Preferences;

public class SubscribedChannelsDAO implements DAO<JSONObject, JSONObject> {
	
	private static SubscribedChannelsDAO instance;
	
	private SQLiteDatabase db;
	private BuddycloudSQLiteOpenHelper helper;
	private String myJid;
	
	private final String[] COLUMNS = new String[]{
			SubscribedChannelsTableHelper.COLUMN_SUBSCRIBED};
	
	private SubscribedChannelsDAO(Context context) {
		this.helper = new BuddycloudSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
		this.myJid = Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
	}
	
	public static SubscribedChannelsDAO getInstance(Context context) {
		if (instance == null) {
			instance = new SubscribedChannelsDAO(context);
		}
		
		return instance;
	}

	private ContentValues buildValues(JSONObject json) {
		ContentValues values = new ContentValues();
		values.put(SubscribedChannelsTableHelper.COLUMN_USER, myJid);
		values.put(SubscribedChannelsTableHelper.COLUMN_SUBSCRIBED, json.toString());
		return values;
	}
	
	public boolean insert(String key, JSONObject json) {
		ContentValues values = buildValues(json);
		if (values != null) {
			long rowId = db.insert(SubscribedChannelsTableHelper.TABLE_NAME, null, values);
			return rowId != -1;
		}
		
		return false;
	}
	
	public boolean update(String key, JSONObject json) {
		ContentValues values = buildValues(json);
		if (values != null) {
			String filter = SubscribedChannelsTableHelper.COLUMN_USER + "=\"" + myJid + "\"";
			int rowsAffected = db.update(SubscribedChannelsTableHelper.TABLE_NAME, 
					values, filter, null);
			return rowsAffected == 1;
		}
		return false;
	}
	
	public JSONObject get(String key) {
		String filter = SubscribedChannelsTableHelper.COLUMN_USER + "=\"" + myJid + "\"";
		JSONObject response = DAOHelper.queryUniqueOnSameThread(db, false, SubscribedChannelsTableHelper.TABLE_NAME, COLUMNS, filter,
				null, null, null, null, null, cursorParser());
		if (response == null) {
			return null;
		}
		try {
			return new JSONObject(response.optString(SubscribedChannelsTableHelper.COLUMN_SUBSCRIBED));
		} catch (JSONException e) {
			return null;
		}
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
		return null;
	}
	
	private JSONObject cursorToJSON(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put(SubscribedChannelsTableHelper.COLUMN_SUBSCRIBED, 
					getString(cursor, SubscribedChannelsTableHelper.COLUMN_SUBSCRIBED));
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}

}
