package com.buddycloud.fragments;

import android.app.Activity;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class ContentFragment extends SherlockFragment {

	public abstract void attached(Activity activity);

	public abstract void createOptions(Menu menu);

	public abstract boolean menuItemSelected(int featureId, MenuItem item);	
	
}
