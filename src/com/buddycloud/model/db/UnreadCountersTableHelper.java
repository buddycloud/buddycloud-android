package com.buddycloud.model.db;


public class UnreadCountersTableHelper {
	
	private UnreadCountersTableHelper() {}

	public static final String TABLE_NAME = "unreadCounters";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_CHANNEL = "channel";
	public static final String COLUMN_MENTIONS_COUNT = "mentionsCount";
	public static final String COLUMN_TOTAL_COUNT = "totalCount";
	public static final String COLUMN_REPLY_COUNT = "replyCount";
	public static final String COLUMN_LAST_WEEK_ACTIVITY = "lastWeekActivity";
	public static final String COLUMN_VISIT_COUNT = "visitCount";
	
	public static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + "("
			+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ COLUMN_USER + " TEXT NOT NULL, " 
			+ COLUMN_CHANNEL + " TEXT NOT NULL, " 
			+ COLUMN_MENTIONS_COUNT + " INTEGER DEFAULT 0, " 
			+ COLUMN_TOTAL_COUNT + " INTEGER DEFAULT 0, "
			+ COLUMN_REPLY_COUNT + " INTEGER DEFAULT 0, "
			+ COLUMN_LAST_WEEK_ACTIVITY + " INTEGER DEFAULT 0, "
			+ COLUMN_VISIT_COUNT + " INTEGER DEFAULT 0);";

	
	public static final String COLUMN_USER_IDX = "counters_user_idx";
	
	public static final String CREATE_USER_INDEX = "CREATE INDEX "
			+ COLUMN_USER_IDX + " ON " + TABLE_NAME + "("
			+ COLUMN_USER + ");";
	
	public static final String PURGE_TABLE = "DELETE FROM " + TABLE_NAME;
}
