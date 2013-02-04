package com.buddycloud.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;

import com.buddycloud.preferences.Constants;
import com.buddycloud.utils.TempUtils;

public class UploadPictureTask extends AsyncTask<Uri, Void, String> {

	private final Activity parent;

	public UploadPictureTask(Activity parent) {
		this.parent = parent;
	}

	@Override
	protected String doInBackground(Uri... params) {

		try {
			SharedPreferences preferences = parent.getSharedPreferences(Constants.PREFS_NAME, 0);
			String myChannel = preferences.getString(Constants.MY_CHANNEL, null);
			
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			ContentResolver cr = parent.getContentResolver();
			InputStream inputStream = cr.openInputStream(params[0]);
			
			File temporaryFile = TempUtils.createTemporaryFile("compressed-", ".jpg");
			FileOutputStream fos = new FileOutputStream(temporaryFile);
			IOUtils.copy(inputStream, fos);
			fos.close();
			
			reqEntity.addPart("data", new FileBody(temporaryFile));
			reqEntity.addPart("filename", new StringBody(temporaryFile.getName()));
			reqEntity.addPart("content-type", new StringBody("image/jpeg"));
			reqEntity.addPart("title", new StringBody("Android upload"));
			
			JSONObject jsonObject = BuddycloudHTTPHelper.post(
					Constants.MY_API + "/" + myChannel + "/media", true,
					reqEntity, preferences);
			return Constants.MY_API + "/"
					+ jsonObject.optString("entityId") + "/media/"
					+ jsonObject.optString("id");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
