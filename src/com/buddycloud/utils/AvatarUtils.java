package com.buddycloud.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.buddycloud.preferences.Preferences;

public class AvatarUtils {
	
	private static final double AVATAR_DIP = 75.;
	
	private static final int MIN_SIZE = 50;
	private static final int MAX_SIZE = 200;
	private static final int THRESHOLD = 125;
	
	private AvatarUtils() {}
	
	public static String avatarURL(Context context, String channel) {
		int avatarSize = (int) (AVATAR_DIP * context.getResources().getDisplayMetrics().density + 0.5);
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		avatarSize = avatarSize > THRESHOLD ? MAX_SIZE : MIN_SIZE; 
		return apiAddress + "/" + channel + "/media/avatar?maxheight=" + avatarSize;
	}
	
	public static File downSample(Context context, Uri uri) throws Exception{
	    Bitmap b = null;

	        //Decode image size
	    BitmapFactory.Options o = new BitmapFactory.Options();
	    o.inJustDecodeBounds = true;

	    int scale = 1;
	    if (o.outHeight > MAX_SIZE || o.outWidth > MAX_SIZE) {
	        scale = (int)Math.pow(2, (int) Math.round(Math.log(MAX_SIZE / 
	           (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	    }

	    //Decode with inSampleSize
	    BitmapFactory.Options o2 = new BitmapFactory.Options();
	    o2.inSampleSize = scale;
	    InputStream is = context.getContentResolver().openInputStream(uri);
		b = BitmapFactory.decodeStream(is, null, o2);
	    is.close();

	    File outputDir = context.getCacheDir();
	    File outputFile = File.createTempFile("avatar", ".jpg", outputDir);
	    FileOutputStream fos = new FileOutputStream(outputFile);
        b.compress(Bitmap.CompressFormat.JPEG, 80, fos);
        fos.close();
	    
	    return outputFile;
	}
}
