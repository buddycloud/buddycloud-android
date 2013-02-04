package com.buddycloud.http;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ProfilePicCache {

	private Map<String, Bitmap> cache = new HashMap<String, Bitmap>();
	
	public Bitmap getBitmap(final String url) {
		Bitmap bitmap = cache.get(url);
		if (bitmap != null) {
			return bitmap;
		}

		final BlockingQueue<Object> blockingBarrier = new ArrayBlockingQueue<Object>(1);
		BuddycloudHTTPHelper.THREAD_POOL.execute(new Runnable() {
			@Override
			public void run() {
				URL bitmapURL;
				try {
					bitmapURL = new URL(url);
					Bitmap iconBitmap = BitmapFactory.decodeStream(bitmapURL.openConnection().getInputStream());
					cache.put(url, iconBitmap);
					blockingBarrier.offer(iconBitmap);
				} catch (Exception e) {
					blockingBarrier.offer(e);
				}
			}
		});
		try {
			Object takenObject = blockingBarrier.take();
			if (takenObject instanceof Bitmap) {
				return (Bitmap) takenObject;
			}
			return null;
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
