package com.buddycloud.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BuddycloudSQLiteOpenHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 6;
	public static final String DATABASE_NAME = "buddycloud.db";
	
	public BuddycloudSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
		onCreate(db);
	}

}
