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
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TempUtils;

public class UploadMediaTask extends AsyncTask<Uri, Void, String> {
	private static final String TAG = "UploadMediaTask";

	private final Activity parent;

	public UploadMediaTask(Activity parent) {
		this.parent = parent;
	}

	@Override
	protected String doInBackground(Uri... params) {

		try {
			
			String apiAddress = Preferences.getPreference(parent, Preferences.API_ADDRESS);
			String myChannel = Preferences.getPreference(parent, Preferences.MY_CHANNEL_JID);

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			ContentResolver cr = parent.getContentResolver();
			
			final Uri streamUri = params[0];
			final String streamType = cr.getType(streamUri);
			InputStream inputStream = cr.openInputStream(streamUri);
			
			File temporaryFile = TempUtils.createTemporaryFile("compressed-", null);
			FileOutputStream fos = new FileOutputStream(temporaryFile);
			IOUtils.copy(inputStream, fos);
			fos.close();
			
			Log.i(TAG, "streamUri="+streamUri.toString());
			Log.i(TAG, "streamType="+streamType);
			
			reqEntity.addPart("data", new FileBody(temporaryFile));
			reqEntity.addPart("filename", new StringBody(temporaryFile.getName()));
			reqEntity.addPart("content-type", new StringBody(streamType));
			reqEntity.addPart("title", new StringBody("Android upload"));
			
			JSONObject jsonObject = BuddycloudHTTPHelper.post(
					apiAddress + "/" + myChannel + "/media", true,
					reqEntity, parent);
			return apiAddress + "/"
					+ jsonObject.optString("entityId") + "/media/"
					+ jsonObject.optString("id");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
