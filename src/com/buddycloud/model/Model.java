package com.buddycloud.model;

import android.app.Activity;

public interface Model<F, S> {

	void refresh(Activity context, ModelCallback<F> callback);
	
	void save(Activity context, S object, ModelCallback<S> callback);
	
	F get(Activity context);
	
}
