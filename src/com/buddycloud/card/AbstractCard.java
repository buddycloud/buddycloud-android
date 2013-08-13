package com.buddycloud.card;

import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public abstract class AbstractCard implements Card {

	private OnClickListener onClickListener;
	private CardListAdapter parent;

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		View view = getContentView(position, convertView, viewGroup);
		if (onClickListener != null) {
			view.setOnClickListener(onClickListener);
		}
		return view;
	}
	
	public abstract View getContentView(int position, View convertView, ViewGroup viewGroup);
	
	public void setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	
	@Override
	public void setParentAdapter(CardListAdapter parent) {
		this.parent = parent;
	}
	
	@Override
	public CardListAdapter getParentAdapter() {
		return parent;
	}
	
}
