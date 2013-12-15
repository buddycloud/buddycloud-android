package com.buddycloud.model;

public class ModelCallbackImpl<T> implements ModelCallback<T>{
	
	@Override
	public void success(T response) {}

	@Override
	public void error(Throwable throwable) {}
	
}
