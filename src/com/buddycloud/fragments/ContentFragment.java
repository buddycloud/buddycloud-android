package com.buddycloud.fragments;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;

public abstract class ContentFragment extends SherlockFragment {

	public abstract void syncd();
	
	public abstract void attached();

	public abstract void createOptions(Menu menu);	
	
}
