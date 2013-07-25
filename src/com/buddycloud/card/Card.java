package com.buddycloud.card;

import org.json.JSONObject;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public interface Card {

	JSONObject getPost(); 
	
	void setPost(JSONObject post); 
	
	View getView(int position, View convertView, ViewGroup viewGroup);

	void setOnClickListener(OnClickListener onClickListener);
	
}
