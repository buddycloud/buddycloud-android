package com.buddycloud.card;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CardListAdapter extends BaseAdapter {

	private List<Card> cards = new ArrayList<Card>();
	
	@Override
	public int getCount() {
		return cards.size();
	}

	@Override
	public Object getItem(int arg0) {
		return cards.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return cards.get(arg0).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		return cards.get(position).getView(position, convertView, viewGroup);
	}

	public void addCard(Card card) {
		cards.add(card);
	}

	public void clear() {
		cards.clear();
	}
	
}
