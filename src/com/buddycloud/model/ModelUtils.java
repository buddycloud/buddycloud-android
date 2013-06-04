package com.buddycloud.model;

import java.util.concurrent.Semaphore;

import org.json.JSONArray;

import android.content.Context;

public class ModelUtils {

	public static void fillAll(final Context context, final ModelCallback<Void> callback) {
		SubscribedChannelsModel.getInstance().fill(context, new ModelCallback<Void>() {
			@Override
			public void success(Void voidd) {
				JSONArray response = SubscribedChannelsModel.getInstance().getFromCache(context);
				final Semaphore semaphore = new Semaphore(response.length() - 1);
				for (int i = 0; i < response.length(); i++) {
					String channelJid = response.optString(i);
					ChannelMetadataModel.getInstance().fill(context, new ModelCallback<Void>() {
						@Override
						public void success(Void response) {
							if (semaphore.tryAcquire()) {
								return;
							}
							SyncModel.getInstance().fill(context, new ModelCallback<Void>() {
								@Override
								public void success(Void response) {
									callback.success(null);
								}

								@Override
								public void error(Throwable throwable) {
								}
							});
						}

						@Override
						public void error(Throwable throwable) {
							
						}
						
					}, channelJid);
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				
			}
		});
	}
	
}
