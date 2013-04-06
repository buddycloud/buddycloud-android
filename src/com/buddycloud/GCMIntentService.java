package com.buddycloud;

import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onRegistered(Context arg0, String regId) {
		// TODO Send regId to the pusher
		
	}

	@Override
	protected void onUnregistered(Context arg0, String regId) {
		// TODO Auto-generated method stub
		
	}

}
