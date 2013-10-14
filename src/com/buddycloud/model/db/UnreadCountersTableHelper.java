package com.buddycloud.model.db;


public class UnreadCountersTableHelper {
	
	private UnreadCountersTableHelper() {}

	public static final String TABLE_NAME = "unreadCounters";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_CHANNEL = "channel";
	public static final String COLUMN_MENTIONS_COUNT = "mentionsCount";
	public static final String COLUMN_TOTAL_COUNT = "totalCount";

	public static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_USER + " TEXT NOT NULL, " 
			+ COLUMN_CHANNEL + " TEXT NOT NULL, " 
			+ COLUMN_MENTIONS_COUNT + " INTEGER NOT NULL," 
			+ COLUMN_TOTAL_COUNT + " INTEGER NOT NULL);";

	
	public static final String COLUMN_USER_IDX = "counters_user_idx";
	
	public static final String CREATE_USER_INDEX = "CREATE INDEX "
			+ COLUMN_USER_IDX + " ON " + TABLE_NAME + "("
			+ COLUMN_USER + ");";
	
	public static final String PURGE_TABLE = "DELETE FROM " + TABLE_NAME;
}
