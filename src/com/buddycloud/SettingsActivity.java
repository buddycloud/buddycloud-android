package com.buddycloud;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.fragments.SettingsFragment;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.InputUtils;

public class SettingsActivity extends SherlockPreferenceActivity {

	public static final int REQUEST_CODE = 111;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		ActionbarUtil.showActionBarwithBack(this, getString(R.string.menu_settings));
		showPreferences();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(SettingsActivity.this);
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return SettingsFragment.onPreferenceClick(this, preference);
	}

	@SuppressWarnings("deprecation")
	protected void showPreferences() {
		ListView lv = getListView();
		if (lv != null) {
			lv.setBackgroundColor(this.getResources().getColor(R.color.bc_bg_color));
			lv.setCacheColorHint(this.getResources().getColor(R.color.bc_bg_color));
			lv.setSelector(R.drawable.setting_pref_item_background_selector);
			
			ColorDrawable dividerDrawable = new ColorDrawable(this.getResources().getColor(R.color.bc_green_blue_color));
			lv.setDivider(dividerDrawable);
			lv.setDividerHeight(1);
		}
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.preferences);
		} else {
			loadSettingsFragment();
		}
	}

	@SuppressLint("NewApi")
	private void loadSettingsFragment() {
		getFragmentManager().beginTransaction().replace(
				android.R.id.content, new SettingsFragment()).commit();
	}
}
