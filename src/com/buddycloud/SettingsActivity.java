package com.buddycloud;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.buddycloud.fragments.SettingsFragment;

public class SettingsActivity extends SherlockPreferenceActivity {

	public static final int REQUEST_CODE = 111;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		showPreferences();
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return SettingsFragment.onPreferenceClick(this, preference);
	}

	@SuppressWarnings("deprecation")
	protected void showPreferences() {
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
