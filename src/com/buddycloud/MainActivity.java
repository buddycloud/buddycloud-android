package com.buddycloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent loginActivity = new Intent();
		loginActivity.setClass(getApplicationContext(), LoginActivity.class);
		startActivityForResult(loginActivity, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ListView contentView = (ListView) findViewById(R.id.contentListView);
		contentView.setEmptyView(findViewById(R.id.subscribedProgress));
		contentView.setAdapter(new SubscribedChannelsAdapter(this));
	}

}
