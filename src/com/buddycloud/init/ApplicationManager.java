package com.buddycloud.init;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.buddycloud.log.Logger;
import com.buddycloud.utils.ImageHelper;

/**
 * This class used for managing the application global context
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class ApplicationManager extends Application {
	
	protected static final String TAG = ApplicationManager.class.getSimpleName();
	
	private static final boolean DEVELOPER_MODE = false;
	
	private static Context context;
	
	private static ApplicationManager mInstance;
	
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	
		// Initialize the app context
		context = getApplicationContext();
		
		// Do initial setup
		init();
	}

	public static synchronized ApplicationManager getInstance() {
		return mInstance;
	}
	
	public static Context getAppContext() {
		return context;
	}

	private void init() {
		
		applicationDebuggingMode();
		ImageHelper.configUIL(context);
	}
	
	@SuppressLint("NewApi")
	private void applicationDebuggingMode() {
		if (DEVELOPER_MODE) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()   // or .detectAll() for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }
	}

	/**
	 * ========================
	 * Database Cleanup Methods
	 * ========================
	 */
	private void clearApplicationData() 
	{
	    File cache = getCacheDir();
	    File appDir = new File(cache.getParent());
	    if (appDir.exists()) {
	        String[] children = appDir.list();
	        for (String s : children) {
	            if (!s.equals("lib")) {
	                deleteDir(new File(appDir, s));
	                Logger.info("TAG", "**************** File /data/data/APP_PACKAGE/" + s + " DELETED *******************");
	            }
	        }
	    }
	}
	
	private boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}
}
