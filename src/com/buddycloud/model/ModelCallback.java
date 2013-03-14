package com.buddycloud.model;

public interface ModelCallback<T> {

	void success(T response);
	
	void error(Throwable throwable);
	
}
