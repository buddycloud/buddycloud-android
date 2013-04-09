package com.buddycloud.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ChannelMetadataTableHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_NAME = "metadata";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_CHANNEL_TYPE = "channelType";
	public static final String COLUMN_ACCESS_MODEL = "accessModel";
	public static final String COLUMN_CREATION_DATE = "creationDate";
	public static final String COLUMN_DEFAULT_AFFILIATION = "defaultAffiliation";
	
	private static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + "("
			+ COLUMN_ID + " TEXT PRIMARY KEY, "
			+ COLUMN_TITLE + " TEXT NOT NULL, " 
			+ COLUMN_DESCRIPTION + " TEXT NOT NULL, " 
			+ COLUMN_CHANNEL_TYPE + " TEXT NOT NULL," 
			+ COLUMN_ACCESS_MODEL + " TEXT NOT NULL,"
			+ COLUMN_DEFAULT_AFFILIATION + " TEXT NOT NULL,"
			+ COLUMN_CREATION_DATE + " TEXT NOT NULL);";
	
	
	public ChannelMetadataTableHelper(Context context) {
		super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create table
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ChannelMetadataTableHelper.class.getName(),
			  "Upgrading " + TABLE_NAME + " from version " + oldVersion + " to "
			  + newVersion + ", which will destroy all old data");
		
		// Drop old data
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

}
