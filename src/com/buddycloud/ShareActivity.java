package com.buddycloud;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.buddycloud.http.PostToBuddycloudTask;
import com.buddycloud.http.UploadMediaTask;

public class ShareActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		Intent intent = getIntent();
		if (!intent.getAction().equals(Intent.ACTION_SEND)) {
			return;
		}

		final Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);

		String mediaType = getContentResolver().getType(uri);
		if (mediaType.contains("image/")) {
			ImageView imageView = (ImageView) findViewById(R.id.shareImagePreview);
			imageView.setVisibility(ImageView.VISIBLE);
			imageView.setImageURI(uri);
		} else if (mediaType.contains("video/")) {
			VideoView videoView = (VideoView) findViewById(R.id.shareVideoPreview);
			videoView.setVisibility(VideoView.VISIBLE);
			videoView.setVideoURI(uri);
		}
		
		Button shareMediaBtn = (Button) findViewById(R.id.shareMediaBtn);
		shareMediaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareMedia(uri);
			}
		});
	}

	protected void shareMedia(Uri uri) {
		UploadMediaTask uploadMediaTask = new UploadMediaTask(
				ShareActivity.this);
		uploadMediaTask.execute(uri);
		try {
			String resultURL = uploadMediaTask.get();
			Toast.makeText(getApplicationContext(),
					"Media uploaded! Now posting it...", Toast.LENGTH_LONG)
					.show();
			postToBuddycloud(resultURL);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	protected void postToBuddycloud(String picURL) throws InterruptedException,
			ExecutionException {
		PostToBuddycloudTask postToBuddycloudTask = new PostToBuddycloudTask(
				ShareActivity.this);
		EditText captionTxt = (EditText) findViewById(R.id.captionText);
		postToBuddycloudTask.execute(picURL, captionTxt.getText().toString());
		postToBuddycloudTask.get();

		Toast.makeText(getApplicationContext(), "Media posted!", Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_share, menu);
		return true;
	}
}
