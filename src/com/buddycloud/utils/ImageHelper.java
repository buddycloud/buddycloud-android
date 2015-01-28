package com.buddycloud.utils;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.log.Logger;
import com.nostra13.universalimageloader.cache.disc.impl.TotalSizeLimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.HttpClientImageDownloader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public class ImageHelper {
    
	private static final String TAG = ImageHelper.class.getName();
	private static final String UIL_CACHE = "uil";
	
	public static void configUIL(Context context) {
		File cacheDir = new File(context.getExternalCacheDir(), UIL_CACHE);
		
		DisplayImageOptions defaultOptions = defaultImageOptions();
		
		HttpClientImageDownloader downloader = new HttpClientImageDownloader(context, 
				BuddycloudHTTPHelper.createHttpClient(context));
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.discCache(new TotalSizeLimitedDiscCache(cacheDir, 50 * 1024 * 1024))
				.imageDownloader(downloader)
				.defaultDisplayImageOptions(defaultOptions)
				.writeDebugLogs()
				.build();
		
		ImageLoader.getInstance().init(config);
	}

	public static DisplayImageOptions defaultImageOptions() {
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        		.cacheInMemory(true)
        		.cacheOnDisc(true)
        		.build();
		return defaultOptions;
	}
	
	public static BitmapProcessor createRoundProcessor(final int roundPixels, 
			final boolean squareEnd, final int targetW) {
		return new BitmapProcessor() {
			
			@Override
			public Bitmap process(Bitmap arg0) {
				Bitmap newBitmap = getRoundedCornerBitmap(arg0, roundPixels, squareEnd, targetW);
				return newBitmap;
			}
		};
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels, boolean squareEnd, int targetW) {
        
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		if (targetW != -1) {
			double ratio = (double) h / (double) w;
			w = targetW;
			h = (int) (targetW * ratio);
		}
		
		Logger.debug(TAG, "Scale w: " + w + ", h: " + h);
		
		bitmap = Bitmap.createScaledBitmap(bitmap, w, h, false);
		
		Bitmap output = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        if (squareEnd) {
        	canvas.drawRect(0, h/2, w/2, h, paint);
        	canvas.drawRect(w/2, h/2, w, h, paint);
        }
        
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}