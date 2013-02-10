package com.buddycloud;

import java.util.concurrent.ExecutionException;

import com.buddycloud.http.PostToBuddycloudTask;
import com.buddycloud.http.UploadMediaTask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

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
		postToBuddycloudTask.execute(picURL);
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
