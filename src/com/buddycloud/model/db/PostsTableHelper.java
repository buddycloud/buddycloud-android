package com.buddycloud.model.db;


public class PostsTableHelper {
	
	private PostsTableHelper() {}
	
	public static final String TABLE_NAME = "channelPosts";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_AUTHOR = "author";
	public static final String COLUMN_PUBLISHED = "published";
	public static final String COLUMN_UPDATED = "updated";
	public static final String COLUMN_CHANNEL = "channel";
	public static final String COLUMN_CONTENT = "content";
	public static final String COLUMN_REPLY_TO = "replyTo";
	public static final String COLUMN_THREAD_ID = "threadId";
	public static final String COLUMN_THREAD_UPDATED = "threadUpdated";
	public static final String COLUMN_MEDIA = "media";
	
	public static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + " ("
			+ COLUMN_ID + " TEXT PRIMARY KEY, "
			+ COLUMN_AUTHOR + " TEXT NOT NULL, " 
			+ COLUMN_PUBLISHED + " TEXT NOT NULL, " 
			+ COLUMN_UPDATED + " TEXT NOT NULL," 
			+ COLUMN_CONTENT + " TEXT,"
			+ COLUMN_CHANNEL + " TEXT NOT NULL,"
			+ COLUMN_REPLY_TO + " TEXT,"
			+ COLUMN_THREAD_ID + " TEXT NOT NULL,"
			+ COLUMN_THREAD_UPDATED + " TEXT NOT NULL,"
			+ COLUMN_MEDIA + " TEXT);";
	
	public static final String COLUMN_CHANNEL_IDX = "posts_channel_idx";
	
	public static final String CREATE_CHANNEL_INDEX = "CREATE INDEX "
			+ COLUMN_CHANNEL_IDX + " ON " + TABLE_NAME + " ("
			+ COLUMN_CHANNEL + ");";
	
	public static final String PURGE_TABLE = "DELETE FROM " + TABLE_NAME;

}
