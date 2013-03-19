package com.buddycloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
	
	public final static String CHANNEL = "com.buddycloud.CHANNEL"; 
	
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
		contentView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position,
					long arg3) {
				String channelJid = (String) adapterView.getItemAtPosition(position);
				Intent channelStreamIntent = new Intent();
				channelStreamIntent.setClass(getApplicationContext(), ChannelStreamActivity.class);
				channelStreamIntent.putExtra(CHANNEL, channelJid);
				startActivity(channelStreamIntent);
			}
		});
	}
	
}
