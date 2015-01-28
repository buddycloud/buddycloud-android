package com.buddycloud.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Base64;

import com.buddycloud.log.Logger;

/**
 * This class for managing the app version and hashKey.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class VersionUtils {

	protected static final String TAG = VersionUtils.class.getSimpleName();

	/**
	 * Get the app version code defined in the android manifest.
	 * 
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static int getVersionCode(final Context context) throws NameNotFoundException {
		PackageInfo manager= context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
	    return manager.versionCode;
	}
	
	/**
	 * Get the app version name defined in the android manifest.
	 * 
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getVersionName(final Context context) {
		
		String verion = null;
		try {
			PackageInfo manager= context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			
			verion = manager.versionName;
			
		} catch (Exception e) {
			Logger.info(TAG, e.getLocalizedMessage());
		}

	    return verion;
	}
	
	/**
	 * Get the unique app hashKey
	 * 
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public static String getAppHashKey( Context context ) {

		String hashKey = null;
		try {
			PackageInfo manager = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_SIGNATURES);
			
			for (Signature signature : manager.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());

				hashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
			}

		} catch (NameNotFoundException e) {
			Logger.info(TAG, e.getLocalizedMessage());

		} catch (NoSuchAlgorithmException e) {
			Logger.info(TAG, e.getLocalizedMessage());
		}

		return hashKey;
	}
}
