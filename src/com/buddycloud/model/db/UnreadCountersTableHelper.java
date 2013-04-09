package com.buddycloud.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class UnreadCountersTableHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "unreadCounters";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_USER_IDX = "user_idx";
	public static final String COLUMN_CHANNEL = "channel";
	public static final String COLUMN_MENTIONS_COUNT = "mentionsCount";
	public static final String COLUMN_TOTAL_COUNT = "totalCount";

	private static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_USER + " TEXT NOT NULL, " 
			+ COLUMN_CHANNEL + " TEXT NOT NULL, " 
			+ COLUMN_MENTIONS_COUNT + " INTEGER NOT NULL," 
			+ COLUMN_TOTAL_COUNT + " INTEGER NOT NULL);";
	
	private static final String CREATE_USER_INDEX = "CREATE INDEX "
			+ COLUMN_USER_IDX + " ON " + TABLE_NAME + "("
			+ COLUMN_USER + ");";
	
	public UnreadCountersTableHelper(Context context) {
		super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create table
		db.execSQL(CREATE_TABLE);
		// User index
		db.execSQL(CREATE_USER_INDEX);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(UnreadCountersTableHelper.class.getName(),
			  "Upgrading " + TABLE_NAME + " from version " + oldVersion + " to "
			  + newVersion + ", which will destroy all old data");
		
		// Drop old data
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

}
