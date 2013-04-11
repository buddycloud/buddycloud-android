package com.buddycloud.model.db;


public class ChannelMetadataTableHelper {
	
	private ChannelMetadataTableHelper() {}
	
	public static final String TABLE_NAME = "channelMetadata";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_CHANNEL_TYPE = "channelType";
	public static final String COLUMN_ACCESS_MODEL = "accessModel";
	public static final String COLUMN_CREATION_DATE = "creationDate";
	public static final String COLUMN_DEFAULT_AFFILIATION = "defaultAffiliation";
	
	public static final String CREATE_TABLE = "CREATE TABLE "
			+ TABLE_NAME + " ("
			+ COLUMN_ID + " TEXT PRIMARY KEY, "
			+ COLUMN_TITLE + " TEXT NOT NULL, " 
			+ COLUMN_DESCRIPTION + " TEXT NOT NULL, " 
			+ COLUMN_CHANNEL_TYPE + " TEXT NOT NULL," 
			+ COLUMN_ACCESS_MODEL + " TEXT NOT NULL,"
			+ COLUMN_DEFAULT_AFFILIATION + " TEXT NOT NULL,"
			+ COLUMN_CREATION_DATE + " TEXT NOT NULL);";
	
}
