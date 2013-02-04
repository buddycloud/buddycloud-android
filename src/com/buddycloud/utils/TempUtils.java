package com.buddycloud.utils;

import java.io.File;

import android.os.Environment;

public class TempUtils {

	public static File createTemporaryFile(String part, String ext) throws Exception {
	    File tempDir= Environment.getExternalStorageDirectory();
	    tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
	    if(!tempDir.exists()) {
	        tempDir.mkdir();
	    }
	    return File.createTempFile(part, ext, tempDir);
	}

}
