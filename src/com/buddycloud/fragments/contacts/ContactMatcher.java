package com.buddycloud.fragments.contacts;

import org.json.JSONArray;

import com.buddycloud.model.ModelCallback;

import android.app.Activity;

public interface ContactMatcher {

	void match(Activity activity, ModelCallback<JSONArray> callback);
	
	String getName();
	
}
