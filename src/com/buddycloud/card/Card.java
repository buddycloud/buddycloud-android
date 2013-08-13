package com.buddycloud.card;

import org.json.JSONObject;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public interface Card extends Comparable<Card> {

	JSONObject getPost(); 
	
	void setPost(JSONObject post); 

	void setParentAdapter(CardListAdapter parent);
	
	CardListAdapter getParentAdapter();
	
	View getView(int position, View convertView, ViewGroup viewGroup);

	void setOnClickListener(OnClickListener onClickListener);
	
}
