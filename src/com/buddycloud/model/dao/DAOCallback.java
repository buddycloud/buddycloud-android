package com.buddycloud.model.dao;

public interface DAOCallback<T> {

	void onResponse(T t);
	
}
