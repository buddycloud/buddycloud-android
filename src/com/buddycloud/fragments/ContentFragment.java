package com.buddycloud.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class ContentFragment extends SherlockFragment {

	public abstract void attached();

	public abstract void createOptions(Menu menu);

	public abstract boolean menuItemSelected(int featureId, MenuItem item);	
	
}
