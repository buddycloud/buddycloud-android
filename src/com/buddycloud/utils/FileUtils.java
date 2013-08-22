package com.buddycloud.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

public class FileUtils {

	public static String getRealPathFromURI(Context context, Uri contentUri) {
	    if (contentUri.getScheme().equals("file")) {
	    	return contentUri.getSchemeSpecificPart();
	    }
		String[] proj = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(columnIndex);
	}

}
