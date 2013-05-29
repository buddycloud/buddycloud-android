package com.buddycloud.model;

import android.content.Context;

public interface Model<RefreshType, SaveType, Params> {

	RefreshType get(Context context, Params... p);
	
	void getAsync(Context context, ModelCallback<RefreshType> callback, Params... p);
	
	void fill(Context context, ModelCallback<Void> callback, Params... p);
	
	void save(Context context, SaveType object, ModelCallback<SaveType> callback, Params... p);
	
}
