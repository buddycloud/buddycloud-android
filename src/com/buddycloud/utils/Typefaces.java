package com.buddycloud.utils;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

import com.buddycloud.log.Logger;

/**
 * Typefaces cache, this allows us to get custom typefaces
 * without causing the android memory leak issue https://code.google.com/p/android/issues/detail?id=9904
 * bug marked as RELEASED Jun 2013 by JBQ (guess its going into 4.3 maybe ?)
 *
 * Wherever you want a font just do:
 *    view.setTypeface(Typefaces.get(context, "assets/fontname.ttf"));
 */
public class Typefaces {
	private static final String TAG = "Typefaces";

	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static Typeface get(Context c, String assetPath) {
		synchronized (cache) {
			if (!cache.containsKey(assetPath)) {
				try {
					Typeface t = Typeface.createFromAsset(c.getAssets(),
							assetPath);
					cache.put(assetPath, t);
				} catch (Exception e) {
					Logger.error(TAG, "Could not get typeface '" + assetPath
							+ "' because " + e.getMessage());
					return null;
				}
			}
			return cache.get(assetPath);
		}
	}
}