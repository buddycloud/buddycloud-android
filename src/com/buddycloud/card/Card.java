package com.buddycloud.card;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public interface Card {

	View getView(int position, View convertView, ViewGroup viewGroup);

	void setOnClickListener(OnClickListener onClickListener);
	
}
