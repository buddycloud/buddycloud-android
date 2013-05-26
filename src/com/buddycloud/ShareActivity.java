package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;

public class ShareActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		Intent intent = getIntent();
		if (!intent.getAction().equals(Intent.ACTION_SEND)) {
			return;
		}

		String myJid = Preferences.getPreference(getApplicationContext(), Preferences.MY_CHANNEL_JID);
		EditText targetChannelView = (EditText) findViewById(R.id.channelText);
		targetChannelView.setText(myJid);
		
		targetChannelView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent searchActivityIntent = new Intent();
				searchActivityIntent.setClass(ShareActivity.this, SearchActivity.class);
				startActivityForResult(searchActivityIntent, SearchActivity.REQUEST_CODE);
			}
		});
		
		final Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);

		String mediaType = getContentResolver().getType(uri);
		ImageView imageView = (ImageView) findViewById(R.id.shareImagePreview);
		if (mediaType.contains("image/")) {
			imageView.setImageURI(uri);
		} else if (mediaType.contains("video/")) {
			Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(getRealPathFromURI(uri),
			        MediaStore.Images.Thumbnails.MINI_KIND);
			imageView.setImageBitmap(thumbnail);
		}
		
		RelativeLayout shareMediaBtn = (RelativeLayout) findViewById(R.id.shareMediaBtn);
		shareMediaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareMedia(uri);
			}
		});
	}

	private String getRealPathFromURI(Uri contentUri) {
	    String[] proj = { MediaStore.Images.Media.DATA };
	    CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
	    Cursor cursor = loader.loadInBackground();
	    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(columnIndex);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SearchActivity.REQUEST_CODE) {
			if (data != null) {
				final String channelJid = data.getStringExtra(GenericChannelsFragment.CHANNEL);
				EditText targetChannelView = (EditText) findViewById(R.id.channelText);
				targetChannelView.setText(channelJid);
			}
		}
	}
	
	protected void shareMedia(Uri uri) {
		Toast.makeText(getApplicationContext(),
				"Uploading media...", Toast.LENGTH_LONG).show();
		EditText targetChannelView = (EditText) findViewById(R.id.channelText);
		MediaModel.getInstance().save(getApplicationContext(), null, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				String mediaURL = MediaModel.url(getApplicationContext(), 
						response.optString("entityId")) + "/" + response.optString("id");
				postToBuddycloud(mediaURL);
			}
			
			@Override
			public void error(Throwable throwable) {
				
			}
		}, uri.toString(), targetChannelView.getText().toString());
	}

	protected void postToBuddycloud(String picURL) {
		EditText targetChannelView = (EditText) findViewById(R.id.channelText);
		PostsModel.getInstance().save(this, createPost(picURL), new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(getApplicationContext(),
						"Media uploaded", Toast.LENGTH_LONG).show();
				finish();
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getApplicationContext(),
						"Error during file upload", Toast.LENGTH_LONG).show();
				finish();
			}
		}, targetChannelView.getText().toString());
		
	}

	private JSONObject createPost(String picURL) {
		EditText caption = (EditText) findViewById(R.id.captionText);
		JSONObject post = new JSONObject();
		try {
			post.putOpt("content", caption.getText().toString() + " " + picURL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return post;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.activity_share, menu);
		return true;
	}
}
