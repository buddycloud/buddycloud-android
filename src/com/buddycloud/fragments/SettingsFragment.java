package com.buddycloud.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.buddycloud.R;

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
}
