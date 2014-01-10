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

import com.buddycloud.AboutBuddycloudActivity;
import com.buddycloud.ChangePasswordActivity;
import com.buddycloud.R;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SyncModel;
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
		if (preference.getKey().equals("pref_key_change_password")) {
			changePassword(context);
			return true;
		}
		if (preference.getKey().equals("pref_key_mark_all_as_read")) {
			confirmMarkAllAsRead(context);
			return true;
		}
		if (preference.getKey().equals("pref_key_about_bc")) {
			openAbout(context);
			return true;
		}
		return false;
	}

	private static void confirmMarkAllAsRead(final Context context) {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(context.getString(R.string.title_confirm_mark_all_as_read))
				.setMessage(context.getString(R.string.message_confirm_mark_all_as_read))
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								markAllAsRead(context);
							}
						}).setNegativeButton(R.string.no, null).show();
	}

	private static void markAllAsRead(Context context) {
		SyncModel.getInstance().resetUnreadCounters(context);
		Toast.makeText(context, 
				context.getString(R.string.message_mark_all_as_read_success), 
				Toast.LENGTH_LONG).show();
	}

	private static void openAbout(Context context) {
		Intent aboutBCIntent = new Intent();
		aboutBCIntent.setClass(context, AboutBuddycloudActivity.class);
		context.startActivity(aboutBCIntent);
	}

	private static void changePassword(Context context) {
		Intent changePasswordIntent = new Intent();
		changePasswordIntent.setClass(context, ChangePasswordActivity.class);
		context.startActivity(changePasswordIntent);
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
		BuddycloudSQLiteOpenHelper.getInstance(context).purgeDatabase();
	}
	
	private static void restart(final Context context) {
		Intent i = context.getPackageManager().getLaunchIntentForPackage(
				context.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}
}
