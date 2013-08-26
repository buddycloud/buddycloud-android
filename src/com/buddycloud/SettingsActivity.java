package com.buddycloud;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.buddycloud.fragments.SettingsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;

public class SettingsActivity extends SherlockPreferenceActivity {

	public static final int REQUEST_CODE = 111;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		NotificationSettingsModel.getInstance().getFromServer(this, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				showPreferences(response);
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getApplicationContext(), "Could not load notification settings.", 
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@SuppressWarnings("deprecation")
	protected void showPreferences(JSONObject response) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = sharedPrefs.edit();
		NotificationSettingsModel.getInstance().fillEditor(editor, response);
		editor.commit();
		
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
