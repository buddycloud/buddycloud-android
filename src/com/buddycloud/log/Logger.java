package com.buddycloud.log;

import android.util.Log;

/**
 * This class used for logging the output on the console with configuration
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class Logger {

	private static final boolean _DEBUG = false;
	
	public static void debug(String TAG, String msg) {
		if (_DEBUG) {
			Log.d(TAG, msg);
		}
	}
	
	public static void info(String TAG, String msg) {
		if (_DEBUG) {
			Log.i(TAG, msg);
		}
	}
	
	public static void warn(String TAG, String msg) {
		warn(TAG, msg, null);
	}
	
	public static void warn(String TAG, String msg, Throwable t) {
		if (_DEBUG) {
			Log.w(TAG, msg, t);
		}
	}

	public static void error(String TAG, String msg) {
		error(TAG, msg, null);
	}
	
	public static void error(String TAG, String msg, Throwable t) {
		if (_DEBUG) {
			Log.e(TAG, msg, t);
		}
	}
}
