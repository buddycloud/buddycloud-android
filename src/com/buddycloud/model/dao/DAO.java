package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONObject;

public interface DAO<Set, Get> {
	
	public boolean insert(String channel, Set json);
	
	public boolean update(String channel, JSONObject json);
	
	public void get(String channel, DAOCallback<Get> callback);
	
	public void getAll(DAOCallback<Map<String, Get>> callback);
}
