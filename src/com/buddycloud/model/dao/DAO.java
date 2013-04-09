package com.buddycloud.model.dao;

import java.util.Map;

import org.json.JSONObject;

public interface DAO<T> {
	
	public boolean insert(String channel, T json);
	
	public boolean update(String channel, JSONObject json);
	
	public T get(String channel);
	
	public Map<String, T> getAll();
}
