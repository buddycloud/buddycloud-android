package com.buddycloud.model.dao;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

public class DAOHelper {

	static void insert(final SQLiteDatabase db, final String table, final String nullColumnHack, 
			final ContentValues values, final DAOCallback<Boolean> callback) {
		new AsyncTask<Void, Void, Long>() {
			@Override
			protected Long doInBackground(Void... params) {
				if (values == null) {
					return -1L;
				}
				return db.insert(table, nullColumnHack, values);
			}
			
			@Override
			protected void onPostExecute(Long result) {
				callback.onResponse(result != -1);
			}
		}.execute();
	}
	
	static void update(final SQLiteDatabase db, final String table, final ContentValues values, 
			final String whereClause, final String[] whereArgs, final DAOCallback<Boolean> callback) {
		new AsyncTask<Void, Void, Integer>() {
			@Override
			protected Integer doInBackground(Void... params) {
				if (values == null) {
					return -1;
				}
				return db.update(table, values, whereClause, whereArgs);
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				callback.onResponse(result > 0);
			}
		}.execute();
	}
	
	static void query(final SQLiteDatabase db, final boolean distinct, final String table, 
			final String[] columns, final String selection, final String[] selectionArgs, 
			final String groupBy, final String having, final String orderBy, final String limit, 
			final DAOCursorParser cursorParser, final DAOCallback<JSONArray> callback) {
		new AsyncTask<Void, Void, JSONArray>() {
			@Override
			protected JSONArray doInBackground(Void... params) {
				Cursor cursor = db.query(distinct, table, columns, selection, selectionArgs, 
						groupBy, having, orderBy, limit);
				cursor.moveToFirst();
				JSONArray responseArray = new JSONArray();
			    while (!cursor.isAfterLast()) {
			    	JSONObject json = cursorParser.parse(cursor);
			    	if (json != null) {
			    		responseArray.put(json);
			    	}
			    	cursor.moveToNext();
			    }
				cursor.close();
				return responseArray;
			}
			
			@Override
			protected void onPostExecute(JSONArray result) {
				callback.onResponse(result);
			}
		}.execute();
	}
	
	static void queryUnique(final SQLiteDatabase db, final boolean distinct, final String table, 
			final String[] columns, final String selection, final String[] selectionArgs, 
			final String groupBy, final String having, final String orderBy, final String limit, 
			final DAOCursorParser cursorParser, final DAOCallback<JSONObject> callback) {
		query(db, distinct, table, columns, selection, selectionArgs, groupBy, having, 
				orderBy, limit, cursorParser, 
				new DAOCallback<JSONArray>() {
					public void onResponse(JSONArray t) {
						callback.onResponse(t.optJSONObject(0));
					}
				});
	}
}
