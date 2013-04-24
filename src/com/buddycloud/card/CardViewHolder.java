package com.buddycloud.card;

import android.util.SparseArray;
import android.view.View;

public class CardViewHolder {

	private SparseArray<View> holder = new SparseArray<View>();
	
	@SuppressWarnings("unchecked")
	public <T> T getView(Integer resource) {
		return (T) holder.get(resource);
	}
	
	public static CardViewHolder create(View view, int... resources) {
		CardViewHolder h = new CardViewHolder();
		for (int resource : resources) {
			h.holder.put(resource, view.findViewById(resource));
		}
		return h;
	}
}
