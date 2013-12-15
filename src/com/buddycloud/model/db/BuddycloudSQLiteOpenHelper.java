package com.buddycloud.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BuddycloudSQLiteOpenHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 15;
	public static final String DATABASE_NAME = "buddycloud.db";
	
	private static BuddycloudSQLiteOpenHelper instance = null;
	
	private BuddycloudSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static BuddycloudSQLiteOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new BuddycloudSQLiteOpenHelper(context);
		}
		return instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Channel metadata table
		db.execSQL(ChannelMetadataTableHelper.CREATE_TABLE);
		
		// Subscribed channels table
		db.execSQL(SubscribedChannelsTableHelper.CREATE_TABLE);
		db.execSQL(SubscribedChannelsTableHelper.CREATE_USER_INDEX);
		
		// Unread counters table
		db.execSQL(UnreadCountersTableHelper.CREATE_TABLE);
		db.execSQL(UnreadCountersTableHelper.CREATE_USER_INDEX);
		
		// Posts table
		db.execSQL(PostsTableHelper.CREATE_TABLE);
		db.execSQL(PostsTableHelper.CREATE_CHANNEL_INDEX);
		
		// Threads table
		db.execSQL(ThreadsTableHelper.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(BuddycloudSQLiteOpenHelper.class.getName(),
			  "Upgrading " + DATABASE_NAME + " from version " + oldVersion + " to "
			  + newVersion + ", which will destroy all old data");

		// Drop old data
		db.execSQL("DROP TABLE IF EXISTS " + ChannelMetadataTableHelper.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SubscribedChannelsTableHelper.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + UnreadCountersTableHelper.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PostsTableHelper.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ThreadsTableHelper.TABLE_NAME);
		onCreate(db);
	}

	public void purgeDatabase() {
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL(PostsTableHelper.PURGE_TABLE);
		db.execSQL(UnreadCountersTableHelper.PURGE_TABLE);
		db.execSQL(SubscribedChannelsTableHelper.PURGE_TABLE);
		db.execSQL(ChannelMetadataTableHelper.PURGE_TABLE);
		db.execSQL(ThreadsTableHelper.PURGE_TABLE);
	}

}
