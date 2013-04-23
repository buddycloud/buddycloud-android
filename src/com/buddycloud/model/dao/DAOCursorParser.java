package com.buddycloud.model.dao;

import org.json.JSONObject;

import android.database.Cursor;

public interface DAOCursorParser {

	JSONObject parse(Cursor c);
	
}
