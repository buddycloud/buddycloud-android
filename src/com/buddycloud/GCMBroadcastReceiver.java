package com.buddycloud;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.buddycloud.log.Logger;
import com.buddycloud.notifications.GCMUtils;

/**
 * This {@code WakefulBroadcastReceiver} takes care of creating and managing a
 * partial wake lock for your app. It passes off the work of processing the GCM
 * message to an {@code IntentService}, while ensuring that the device does not
 * go back to sleep in the transition. The {@code IntentService} calls
 * {@code GcmBroadcastReceiver.completeWakefulIntent()} when it is ready to
 * release the wake lock.
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

	protected static final String TAG = GCMBroadcastReceiver.class.getSimpleName();
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	Logger.info(TAG, "GcmBroadcastReceiver - onReceive !!!");
		
		// Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMIntentService.class.getName());
        
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        
        GCMUtils.clearGCMAuthors(context);
    }
}
