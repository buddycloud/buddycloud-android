package com.buddycloud.utils;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.os.Build;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.buddycloud.R;

/**
 * This class provides utility methods to set the
 * action bar. However, it also try to consistent
 * the look and feel w.r.t android Lollipop OS5.
 * 
 * @author Adnan Urooj (Deminem)
 *
 */
public class ActionbarUtil {

	public static void setTitle(final SherlockPreferenceActivity activity, final String title) {
		if (activity == null || title == null) return;
		
		activity.getSupportActionBar().setTitle(title);
		activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
	}
	
	public static void setTitle(final SherlockActivity activity, final String title) {
		if (activity == null || title == null) return;
		
		activity.getSupportActionBar().setTitle(title);
		activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
	}
	
	public static void setTitle(final SherlockFragmentActivity activity, final String title) {
		if (activity == null || title == null) return;
		
		activity.getSupportActionBar().setTitle(title);
		activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
	}
	
	public static void setIcon(final SherlockActivity activity, final int iconResc) {
		if (activity == null) return;
		
		activity.getSupportActionBar().setIcon(iconResc);
		activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
		activity.getSupportActionBar().setDisplayUseLogoEnabled(true);
		activity.getSupportActionBar().setHomeButtonEnabled(true);
	}
	
	public static void setIcon(final SherlockFragmentActivity activity, final int iconResc) {
		if (activity == null) return;
		
		activity.getSupportActionBar().setIcon(iconResc);
		activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
		activity.getSupportActionBar().setDisplayUseLogoEnabled(true);
		activity.getSupportActionBar().setHomeButtonEnabled(true);
	}
	
	public static void setLogo(final SherlockActivity activity, final int logoResc) {
		if (activity == null) return;
		
		activity.getSupportActionBar().setLogo(logoResc);
		activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
		activity.getSupportActionBar().setDisplayUseLogoEnabled(true);
		activity.getSupportActionBar().setHomeButtonEnabled(true);
	}
	
	public static void setLogo(final SherlockFragmentActivity activity, final int logoResc) {
		if (activity == null) return;
		
		activity.getSupportActionBar().setLogo(logoResc);
		activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
		activity.getSupportActionBar().setDisplayUseLogoEnabled(true);
		activity.getSupportActionBar().setHomeButtonEnabled(true);
	}
	
	/**
	 * Show the actionbar with given attributes
	 * 
	 * @param activity
	 * @param title
	 * @param isShowDrawerMenu
	 */
	public static void showActionBar(final SherlockActivity activity, 
			final String title, final boolean isShowDrawerMenu) {
		if (activity == null) return;
		
		setTitle(activity, title);
		if (isShowDrawerMenu) {
			setActionBar(activity.getActionBar(), activity.getSupportActionBar(), R.drawable.ic_drawer);
		}
		makeOverflowMenuShow(activity.getApplicationContext());
	}
	
	/**
	 * Show the actionbar with given attributes
	 * 
	 * @param activity
	 * @param title
	 * @param isShowDrawerMenu
	 */
	public static void showActionBar(final SherlockFragmentActivity activity, 
			final String title, final boolean isShowDrawerMenu) {
		if (activity == null) return;
		
		setTitle(activity, title);
		if (isShowDrawerMenu) {
			setActionBar(activity.getActionBar(), activity.getSupportActionBar(), R.drawable.ic_drawer);
		}
		makeOverflowMenuShow(activity.getApplicationContext());
	}

	/**
	 * Show the actionbar with backstack icon
	 * 
	 * @param activity
	 * @param title
	 */
	public static void showActionBarwithBack(final SherlockPreferenceActivity activity, 
			final String title) {
		if (activity == null) return;
		
		setTitle(activity, title);
		makeOverflowMenuShow(activity.getApplicationContext());
		setActionBar(activity.getActionBar(), activity.getSupportActionBar(), R.drawable.ic_ab_up_compat);
	}
	
	/**
	 * Show the actionbar with backstack icon
	 * 
	 * @param activity
	 * @param title
	 */
	public static void showActionBarwithBack(final SherlockActivity activity, 
			final String title) {
		if (activity == null) return;
		
		setTitle(activity, title);
		makeOverflowMenuShow(activity.getApplicationContext());
		setActionBar(activity.getActionBar(), activity.getSupportActionBar(), R.drawable.ic_ab_up_compat);
	}
	
	/**
	 * Show the actionbar with backstack icon
	 * 
	 * @param activity
	 * @param title
	 */
	public static void showActionBarwithBack(final SherlockFragmentActivity activity, 
			final String title) {
		if (activity == null) return;
		
		setTitle(activity, title);
		makeOverflowMenuShow(activity.getApplicationContext());
		setActionBar(activity.getActionBar(), activity.getSupportActionBar(), R.drawable.ic_ab_up_compat);
	}
	
	/**
	 * To enforce the overflow menu icon show for every android device.
	 * 
	 * @param context
	 */
	private static void makeOverflowMenuShow(final Context context) {
		try {
			ViewConfiguration config = ViewConfiguration.get(context);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressLint("NewApi")
	private static void setActionBar(final ActionBar actionBar, 
			final com.actionbarsherlock.app.ActionBar sherlockActionBar, 
			final int iconResc) {
		if (actionBar == null || sherlockActionBar == null) return;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			actionBar.setHomeAsUpIndicator(iconResc);
			sherlockActionBar.setDisplayHomeAsUpEnabled(true);
			sherlockActionBar.setHomeButtonEnabled(true);
		} else {
			sherlockActionBar.setLogo(iconResc);
			sherlockActionBar.setDisplayUseLogoEnabled(true);
			sherlockActionBar.setDisplayHomeAsUpEnabled(true);
			sherlockActionBar.setHomeButtonEnabled(true);
		}
	}
}
