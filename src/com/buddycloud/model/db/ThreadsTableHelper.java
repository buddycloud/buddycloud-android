package com.buddycloud.model.db;


public class ThreadsTableHelper {
	
	private ThreadsTableHelper() {}

	public static final String TABLE_NAME = "threads";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_UPDATED = "updated";
	public static final String COLUMN_CHANNEL = "channel";
	
	public static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + " ("
			+ COLUMN_ID + " TEXT PRIMARY KEY, "
			+ COLUMN_CHANNEL + " TEXT NOT NULL,"
			+ COLUMN_UPDATED + " TEXT NOT NULL);";

	public static final String PURGE_TABLE = "DELETE FROM " + TABLE_NAME;
}
