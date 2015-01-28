package com.buddycloud;

import android.app.Application;
import android.content.Context;

/**
 * This class used for managing the application global context
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class ApplicationManager extends Application {
	
	protected static final String TAG = ApplicationManager.class.getSimpleName();
	
	private static Context context;

	private static ApplicationManager mInstance;
	
	public void onCreate() {
		super.onCreate();
		
		mInstance = this;
	
		// Initialize the global app context
		context = getApplicationContext();
	}

	public static synchronized ApplicationManager getInstance() {
		return mInstance;
	}
	
	public static Context getAppContext() {
		return context;
	}
}
