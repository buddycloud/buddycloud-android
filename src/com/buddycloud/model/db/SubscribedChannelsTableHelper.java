package com.buddycloud.model.db;


public class SubscribedChannelsTableHelper {
	
	private SubscribedChannelsTableHelper() {}

	public static final String TABLE_NAME = "subscribedChannels";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_SUBSCRIBED = "subscribed";

	public static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_USER + " TEXT NOT NULL, " 
			+ COLUMN_SUBSCRIBED + " TEXT NOT NULL);";

	public static final String COLUMN_USER_IDX = "subscribed_user_idx";
	
	public static final String CREATE_USER_INDEX = "CREATE INDEX "
			+ COLUMN_USER_IDX + " ON " + TABLE_NAME + "("
			+ COLUMN_USER + ");";
	
	public static final String PURGE_TABLE = "DELETE FROM " + TABLE_NAME;
}
