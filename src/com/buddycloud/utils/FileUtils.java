package com.buddycloud.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import com.buddycloud.log.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileUtils {

    private static final String TAG = FileUtils.class.getName();

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

    public static void removeAllFiles(String foldername) {
        if (isDirectoryExist(foldername)) {
            try {
                File directory = null;
                directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + File.separator + foldername);

                org.apache.commons.io.FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                Logger.error(TAG, "I/O Exception: ", e);
            }
        }
    }

    public static boolean isDirectoryExist(String foldername) {
        if (TextUtils.isEmpty(foldername))
            return false;

        File directory = null;
        directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + foldername);
        return (directory != null) ? directory.exists() : false;
    }
}
