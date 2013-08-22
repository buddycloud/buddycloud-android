package com.buddycloud.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.FileUtils;

public class MediaModel extends AbstractModel<JSONObject, JSONObject, String> {

	private static MediaModel instance;
	public static final String ENDPOINT = "/media"; 
	public static final String AVATAR = "/avatar";
	
	private MediaModel() {}
	
	public static MediaModel getInstance() {
		if (instance == null) {
			instance = new MediaModel();
		}
		return instance;
	}
	
	@Override
	public void getFromServer(Context context, final ModelCallback<JSONObject> callback, String... p) {
	}

	private static String url(Context context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT;
	}
	
	private static String avatarUrl(Context context, String channel) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		return apiAddress + "/" + channel + ENDPOINT + AVATAR;
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

	public void saveAvatar(Context context, JSONObject object,
			ModelCallback<JSONObject> callback, String... p) {
		try {
			postAvatar(context, callback, p);
		} catch (Exception e) {
			callback.error(e);
		}
	}
	
	private void postAvatar(Context context, ModelCallback<JSONObject> callback,
			String... p) throws FileNotFoundException, Exception, IOException,
			UnsupportedEncodingException {
		postMediaGeneral(context, callback, p[0], avatarUrl(context, p[1]), false);
	}
	
	private void postMedia(Context context, ModelCallback<JSONObject> callback,
			String... p) throws FileNotFoundException, Exception, IOException,
			UnsupportedEncodingException {
		postMediaGeneral(context, callback, p[0], url(context, p[1]), true);
	}

	private void postMediaGeneral(Context context,
			ModelCallback<JSONObject> callback, String imageUri, String url, boolean post)
			throws UnsupportedEncodingException {
		
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		ContentResolver cr = context.getContentResolver();
		
		final Uri streamUri = Uri.parse(imageUri);
		String streamType = cr.getType(streamUri);
		
		File mediaFile = new File(FileUtils.getRealPathFromURI(context, streamUri));
		if (streamType == null) {
			streamType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					FilenameUtils.getExtension(mediaFile.getName()));
		}
		
		reqEntity.addPart("data", new FileBody(mediaFile));
		reqEntity.addPart("filename", new StringBody(mediaFile.getName()));
		reqEntity.addPart("content-type", new StringBody(streamType));
		reqEntity.addPart("title", new StringBody("Android upload"));
		
		if (post) {
			BuddycloudHTTPHelper.post(url, reqEntity, context, callback);
		} else {
			BuddycloudHTTPHelper.put(url, reqEntity, context, callback);
		}
	}

	@Override
	public JSONObject getFromCache(Context context, String... p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fill(Context context, ModelCallback<Void> callback, String... p) {
		// TODO Auto-generated method stub
		
	}

}
