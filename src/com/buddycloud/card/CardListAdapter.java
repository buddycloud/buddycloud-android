package com.buddycloud.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CardListAdapter extends BaseAdapter {

	private List<Card> cards = new ArrayList<Card>();
	private Map<String, Card> cardsRef = new HashMap<String, Card>();
	private Fragment fragment;
	
	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}
	
	public Fragment getFragment() {
		return fragment;
	}
	
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
		String postId = card.getPost().optString("id");
		Card oldCard = cardsRef.get(postId);
		if (oldCard == null) {
			cardsRef.put(postId, card);
			cards.add(card);
		} else {
			oldCard.setPost(card.getPost());
		}
		card.setParentAdapter(this);
	}
	
	public Card getCard(String itemId) {
		return cardsRef.get(itemId);
	}

	public void sort() {
		Collections.sort(cards);
		notifyDataSetChanged();
	}

	public void clear() {
		cards.clear();
		cardsRef.clear();
	}

	public void remove(String postId) {
		Card removed = cardsRef.remove(postId);
		if (removed != null) {
			cards.remove(removed);
		}
		notifyDataSetChanged();
	}
}
