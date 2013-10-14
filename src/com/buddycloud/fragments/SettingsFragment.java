package com.buddycloud.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.db.BuddycloudSQLiteOpenHelper;
import com.buddycloud.preferences.Preferences;

@SuppressLint("NewApi")
public class SettingsFragment extends PreferenceFragment {

	private static final String BUG_REPORT_RECIPIENTS = "simon@buddycloud.com";
	private static final String BUG_REPORT_SUBJECT = "buddycloud - Android client bug report";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return onPreferenceClick(getActivity(), preference);
	}

	public static boolean onPreferenceClick(final Context context, Preference preference) {
		if (preference.getKey().equals("pref_key_delete_account")) {
			confirmDeleteAccount(context);	
			return true;
		}
		if (preference.getKey().equals("pref_key_rate_buddycloud")) {
			rateBuddycloud(context);
			return true;
		}
		if (preference.getKey().equals("pref_key_debug_report")) {
			sendBugReport(context);
			return true;
		}
		return false;
	}

	protected static void sendBugReport(final Context context) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", 
				BUG_REPORT_RECIPIENTS, null));
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, BUG_REPORT_SUBJECT);
		context.startActivity(Intent.createChooser(emailIntent, 
				context.getString(R.string.message_send_bug_report)));
	}

	protected static void rateBuddycloud(final Context context) {
		final String appName = context.getPackageName();
		try {
		    context.startActivity(new Intent(Intent.ACTION_VIEW, 
		    		Uri.parse("market://details?id=" + appName)));
		} catch (android.content.ActivityNotFoundException anfe) {
			context.startActivity(new Intent(Intent.ACTION_VIEW, 
					Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
		}
	}

	protected static void confirmDeleteAccount(final Context context) {
		new AlertDialog.Builder(context)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(context.getString(R.string.title_confirm_delete_account))
			.setMessage(context.getString(R.string.message_confirm_delete_account))
			.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							deleteAccount(context);
						}
					}).setNegativeButton(R.string.no, null).show();
	}

	protected static void deleteAccount(final Context context) {
		AccountModel.getInstance().delete(context, new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				Toast.makeText(context, 
						context.getString(R.string.message_delete_account_success), 
						Toast.LENGTH_LONG).show();
				deleteDatabase(context);
				deletePreferences(context);
				restart(context);
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, 
						context.getString(R.string.message_delete_account_failed), 
						Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private static void deletePreferences(final Context context) {
		Preferences.deletePreferences(context);
	}

	private static void deleteDatabase(final Context context) {
		new BuddycloudSQLiteOpenHelper(context).purgeDatabase();
	}
	
	private static void restart(final Context context) {
		Intent i = context.getPackageManager().getLaunchIntentForPackage(
				context.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}
}
