package com.buddycloud.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TempUtils;

public class MediaModel implements Model<JSONObject, JSONObject, String> {

	private static MediaModel instance;
	private static final String ENDPOINT = "/media"; 
	
	private MediaModel() {}
	
	public static MediaModel getInstance() {
		if (instance == null) {
			instance = new MediaModel();
		}
		return instance;
	}
	
	@Override
	public void refresh(Context context, final ModelCallback<JSONObject> callback, String... p) {
	}

	public static String url(Context context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT;
	}

	@Override
	public void save(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		try {
			postMedia(context, callback, p);
		} catch (Exception e) {
			callback.error(e);
		}
	}

	private void postMedia(Context context, ModelCallback<JSONObject> callback,
			String... p) throws FileNotFoundException, Exception, IOException,
			UnsupportedEncodingException {
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		ContentResolver cr = context.getContentResolver();
		
		final Uri streamUri = Uri.parse(p[0]);
		final String streamType = cr.getType(streamUri);
		InputStream inputStream = cr.openInputStream(streamUri);
		
		File temporaryFile = TempUtils.createTemporaryFile("compressed-", null);
		FileOutputStream fos = new FileOutputStream(temporaryFile);
		IOUtils.copy(inputStream, fos);
		fos.close();
		
		reqEntity.addPart("data", new FileBody(temporaryFile));
		reqEntity.addPart("filename", new StringBody(temporaryFile.getName()));
		reqEntity.addPart("content-type", new StringBody(streamType));
		reqEntity.addPart("title", new StringBody("Android upload"));
		
		BuddycloudHTTPHelper.post(url(context, p[1]), reqEntity, context, callback);
	}

	@Override
	public JSONObject get(Context context, String... p) {
		return null;
	}
	
}
