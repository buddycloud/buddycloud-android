package com.buddycloud.fragments;

import android.app.Activity;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.model.ModelCallback;

public abstract class ContentFragment extends SherlockFragment {

	public abstract void attached(Activity activity);

	public abstract void createOptions(Menu menu);

	public abstract boolean menuItemSelected(int featureId, MenuItem item);
	
	protected <T> SmartCallback<T> smartify(ModelCallback<T> callback) {
		return new SmartCallback<T>(callback);
	}
	
	protected class SmartCallback<T> implements ModelCallback<T> {

		private ModelCallback<T> innerCallback;

		public SmartCallback(ModelCallback<T> innerCallback) {
			this.innerCallback = innerCallback;
		}
		
		@Override
		public void success(T response) {
			if (!isAttachedToActivity()) {
				return;
			}
			innerCallback.success(response);
		}

		@Override
		public void error(Throwable throwable) {
			if (!isAttachedToActivity()) {
				return;
			}
			innerCallback.error(throwable);
		}
		
	}
	
	protected boolean isAttachedToActivity() {
		return !isDetached() && getActivity() != null;
	}
}
