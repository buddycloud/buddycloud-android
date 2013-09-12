package com.buddycloud.http;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.buddycloud.R;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;

public class SSLUtils {

	public static void checkSSL(final Context context, final String apiAddress, 
			final ModelCallback<Void> callback) {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	boolean sSLDisabled = preferences.getBoolean("pref_key_disable_ssl_check", false);
    	if (sSLDisabled) {
    		callback.success(null);
    		return;
    	}

    	String skipSSLForDomain = Preferences.getPreference(context, Preferences.TRUST_SSL_PREFIX + apiAddress);
    	if (skipSSLForDomain != null && Boolean.parseBoolean(skipSSLForDomain)) {
    		callback.success(null);
    		return;
    	}
    	
		BuddycloudHTTPHelper.checkSSL(apiAddress, context, new ModelCallback<Integer>() {
			@Override
			public void success(Integer response) {
				callback.success(null);
			}

			@Override
			public void error(Throwable throwable) {
				confirmTrustSSL(context, apiAddress, callback);
			}
		});
	}
	
	private static void confirmTrustSSL(final Context context, final String apiAddress, 
			final ModelCallback<Void> callback) {
		String sslErrorTitle = context.getString(R.string.ssl_error_title);
		String sslErrorMessage = context.getString(R.string.ssl_error_message, apiAddress);
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(sslErrorTitle)
				.setMessage(sslErrorMessage)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								callback.success(null);
							}
						})
				.setNeutralButton(R.string.yes_and_remember, 
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Preferences.setPreference(context, 
										Preferences.TRUST_SSL_PREFIX + apiAddress, 
										Boolean.TRUE.toString());
								callback.success(null);
							}
						})
				.setNegativeButton(R.string.no, 
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								callback.error(null);
							}
						})
				.show();
    }
	
}
