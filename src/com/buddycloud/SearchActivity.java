package com.buddycloud;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.fragments.SearchChannelsFragment;
import com.buddycloud.log.Logger;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.InputUtils;

public class SearchActivity extends SherlockFragmentActivity {

	protected static final String TAG = SearchActivity.class.getSimpleName();
	
	public static final int REQUEST_CODE = 102;
	private static final long SEARCH_DELAY = 2000;

	private ImageButton searchCancelBtn;
	private TextView searchView;
	private boolean searchScheduled = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		ActionbarUtil.showActionBarwithBack(this, getString(R.string.search_title));

		String q = getIntent().getStringExtra(SearchChannelsFragment.FILTER);
		searchView = (TextView) findViewById(R.id.searchTxt);
		searchView.setText(q);

		final SearchChannelsFragment searchChannelsFragment = new SearchChannelsFragment();

		String[] affiliationsToDisplay = getIntent().getStringArrayExtra(
				SearchChannelsFragment.AFFILIATIONS);
		if (affiliationsToDisplay != null) {
			searchChannelsFragment
					.setAffiliationsToDisplay(affiliationsToDisplay);
		}

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		searchChannelsFragment.setInitialFilter(q);
		searchChannelsFragment.setWindowToken(searchView.getWindowToken());
		transaction.replace(R.id.contentFrame, searchChannelsFragment);
		transaction.commitAllowingStateLoss();

		searchView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(final CharSequence searchString,
					int arg1, int arg2, int arg3) {

				if (searchString.toString().length() > 0) {
					searchCancelBtn.setImageDrawable(getResources()
							.getDrawable(R.drawable.ic_cancel_dark));
				} else {
					searchCancelBtn.setImageDrawable(getResources()
							.getDrawable(R.drawable.ic_cancel_light));
				}

				if (!searchScheduled) {
					searchScheduled = true;
					searchView.postDelayed(new Runnable() {
						@Override
						public void run() {
							searchChannelsFragment.filter(SearchActivity.this,
									searchString.toString());
							searchScheduled = false;
						}
					}, SEARCH_DELAY);
				}
			}
		});

		searchCancelBtn = (ImageButton) findViewById(R.id.search_cancel_btn);
		searchCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchCancel();
			}
		});
	}

	@Override
	protected void onPause() {
		TextView et = ((TextView) findViewById(R.id.searchTxt));
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		super.onPause();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(SearchActivity.this);
        		finish();
        		return true;
 
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}

	/**
	 * Cancel the search and clear filter.
	 * 
	 */
	private void searchCancel() {

		if (searchView != null && searchView.getText().length() != 0) {
			searchView.setText("");
		}
	}
}
