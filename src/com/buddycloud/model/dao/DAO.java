package com.buddycloud.model.dao;

import java.util.Map;

public interface DAO<Set, Get> {
	
	public boolean insert(String key, Set json);
	
	public boolean update(String key, Set json);
	
	public Get get(String key);
	
	public Map<String, Get> getAll();
}
