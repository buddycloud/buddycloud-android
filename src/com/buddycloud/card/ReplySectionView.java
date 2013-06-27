package com.buddycloud.card;

import android.database.DataSetObserver;
import android.widget.LinearLayout;

public class ReplySectionView {

	public static void configure(final LinearLayout layout, final CardListAdapter adapter) {
		reloadLayout(layout, adapter);
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				reloadLayout(layout, adapter);
			}
		});
	}

	private static void reloadLayout(LinearLayout layout,
			CardListAdapter adapter) {
		layout.removeAllViews();
		for (int i = 0; i < adapter.getCount(); i++) {
			layout.addView(adapter.getView(i, null, layout));
		}
	}
	
}
