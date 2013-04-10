package com.buddycloud.model;

import android.content.Context;

public interface Model<RefreshType, SaveType, Params> {

	void refresh(Context context, ModelCallback<RefreshType> callback, Params... p);
	
	void save(Context context, SaveType object, ModelCallback<SaveType> callback, Params... p);
	
	RefreshType get(Context context, Params... p);
	
}
