package com.buddycloud.model;

import android.app.Activity;

public interface Model<RefreshType, SaveType, Params> {

	void refresh(Activity context, ModelCallback<RefreshType> callback, Params... p);
	
	void save(Activity context, SaveType object, ModelCallback<SaveType> callback, Params... p);
	
	RefreshType get(Activity context, Params... p);
	
}
