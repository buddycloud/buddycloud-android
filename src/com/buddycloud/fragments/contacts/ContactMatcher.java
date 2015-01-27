package com.buddycloud.fragments.contacts;

import org.json.JSONArray;

import android.app.Activity;

import com.buddycloud.model.ModelCallback;

public interface ContactMatcher {

	void match(Activity activity, ModelCallback<JSONArray> callback);
	
	String getName();
	
}
