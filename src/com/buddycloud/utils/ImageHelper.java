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
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class ImageHelper {
    
	private static final String TAG = ImageHelper.class.getName();
	private static final String PICASSO_CACHE = "picasso";
	private static final Long PICASSO_CACHE_SIZE = 20L * 1024L * 1024L; // 20MB
	private static Picasso PICASSO = null;
	
	public static Picasso picasso(Context context) {
		if (PICASSO == null) {
			PICASSO = createPicasso(context, false);
		}
		return PICASSO;
	}

	private static Picasso createPicasso(Context context, boolean skipCache) {
		File cacheDir = new File(context.getExternalCacheDir(), PICASSO_CACHE);
		cacheDir.mkdirs();
		return new Picasso.Builder(context)
			.downloader(new PicassoDownloader(cacheDir, PICASSO_CACHE_SIZE, skipCache))
			.build();
	}

	public static Picasso picassoSkipCache(Context context) {
		return createPicasso(context, true);
	}
	
	public static Transformation createRoundTransformation(final Context context, 
			final int roundPixels, final boolean squareEnd, final int targetW) {
		return new Transformation() {
			
			@Override
			public Bitmap transform(Bitmap arg0) {
				Bitmap newBitmap = getRoundedCornerBitmap(arg0, roundPixels, squareEnd, targetW);
				arg0.recycle();
				return newBitmap;
			}
			
			@Override
			public String key() {
				return "ROUND";
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
		
		Log.d(TAG, "Scale w: " + w + ", h: " + h);
		
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