package com.buddycloud.model.dao;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.model.db.ChannelMetadataTableHelper;

public class ChannelMetadataDAO implements DAO<JSONObject, JSONObject> {
	
	private static ChannelMetadataDAO instance;
	
	private SQLiteDatabase db;
	private BuddycloudSQLiteOpenHelper helper;
	
	private final String[] COLUMNS = new String[]{
			ChannelMetadataTableHelper.COLUMN_ID, 
			ChannelMetadataTableHelper.COLUMN_TITLE, 
			ChannelMetadataTableHelper.COLUMN_DESCRIPTION,
			ChannelMetadataTableHelper.COLUMN_CHANNEL_TYPE,
			ChannelMetadataTableHelper.COLUMN_ACCESS_MODEL,
			ChannelMetadataTableHelper.COLUMN_CREATION_DATE,
			ChannelMetadataTableHelper.COLUMN_DEFAULT_AFFILIATION};
	
	
	private ChannelMetadataDAO(Context context) {
		this.helper = new BuddycloudSQLiteOpenHelper(context);
		this.db = helper.getWritableDatabase();
	}
	
	
	public static ChannelMetadataDAO getInstance(Context context) {
		if (instance == null) {
			instance = new ChannelMetadataDAO(context);
		}
		
		return instance;
	}

	
	private ContentValues buildValues(String channel, String title, String description, String channelType,
			String accessModel, String creationDate, String defaultAffiliation) {
		ContentValues values = new ContentValues();
		values.put(ChannelMetadataTableHelper.COLUMN_ID, channel);
		values.put(ChannelMetadataTableHelper.COLUMN_TITLE, title);
		values.put(ChannelMetadataTableHelper.COLUMN_DESCRIPTION, description);
		values.put(ChannelMetadataTableHelper.COLUMN_CHANNEL_TYPE, channelType);
		values.put(ChannelMetadataTableHelper.COLUMN_ACCESS_MODEL, accessModel);
		values.put(ChannelMetadataTableHelper.COLUMN_CREATION_DATE, creationDate);
		values.put(ChannelMetadataTableHelper.COLUMN_DEFAULT_AFFILIATION, defaultAffiliation);
		
		return values;
	}
	
	private ContentValues buildValues(String channel, JSONObject json) {
		String title = json.optString("title");
		String description = json.optString("description");
		String channelType = json.optString("channel_type");
		String accessModel = json.optString("access_model");
		String creationDate = json.optString("creation_date");
		String defaultAffiliation = json.optString("default_affiliation");
		
		return buildValues(channel, title, description, channelType, accessModel, creationDate, defaultAffiliation);
	}
	
	public boolean insert(String channel, JSONObject json) {
		ContentValues values = buildValues(channel, json);
		if (values != null) {
			long rowId = db.insert(ChannelMetadataTableHelper.TABLE_NAME, null, values);
			return rowId != -1;
		}
		
		return false;
	}
	
	public boolean update(String channel, JSONObject json) {
		ContentValues values = buildValues(channel, json);
		if (values != null) {
			String filter = ChannelMetadataTableHelper.COLUMN_ID + "=\"" + channel + "\"";
			int rowsAffected = db.update(ChannelMetadataTableHelper.TABLE_NAME, 
					values, filter, null);
			return rowsAffected == 1;
		}
		
		return false;
	}
	
	public JSONObject get(String channel) {
		String filter = ChannelMetadataTableHelper.COLUMN_ID + "=\"" + channel + "\"";
		Cursor cursor = db.query(ChannelMetadataTableHelper.TABLE_NAME, COLUMNS, filter,
				null, null, null, null);
		cursor.moveToFirst();
		JSONObject json = cursorToJSON(cursor);
		cursor.close();
		
		return json;
	}
	
	public Map<String, JSONObject> getAll() {
		Map<String, JSONObject> map = new HashMap<String, JSONObject>();
		
		Cursor cursor = db.query(ChannelMetadataTableHelper.TABLE_NAME,
				COLUMNS, null, null, null, null, null);
		
		cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	JSONObject json = cursorToJSON(cursor);
	    	if (json != null) {
	    		map.put(cursor.getString(0), json);
	    	}
	    	cursor.moveToNext();
	    }
	    cursor.close();
	    
	    return map;
	}
	
	private JSONObject cursorToJSON(Cursor cursor) {
		JSONObject json = new JSONObject();
		try {
			json.put(ChannelMetadataTableHelper.COLUMN_ID, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_ID));
			json.put(ChannelMetadataTableHelper.COLUMN_TITLE, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_TITLE));
			json.put(ChannelMetadataTableHelper.COLUMN_DESCRIPTION, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_DESCRIPTION));
			json.put(ChannelMetadataTableHelper.COLUMN_CHANNEL_TYPE, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_CHANNEL_TYPE));
			json.put(ChannelMetadataTableHelper.COLUMN_ACCESS_MODEL, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_ACCESS_MODEL));
			json.put(ChannelMetadataTableHelper.COLUMN_CREATION_DATE, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_CREATION_DATE));
			json.put(ChannelMetadataTableHelper.COLUMN_DEFAULT_AFFILIATION, 
					getString(cursor, ChannelMetadataTableHelper.COLUMN_DEFAULT_AFFILIATION));
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	private String getString(Cursor cursor, String columnName) {
		return cursor.getString(cursor.getColumnIndex(columnName));
	}
}
