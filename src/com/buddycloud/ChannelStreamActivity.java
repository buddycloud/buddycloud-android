package com.buddycloud;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class ChannelStreamActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);
		
		// Title
		final String channelJid = (String) getIntent().getExtras().get(MainActivity.CHANNEL);
		loadTitle(channelJid);

		// Stream
		ListView contentView = (ListView) findViewById(R.id.contentListView);
		contentView.setEmptyView(findViewById(R.id.subscribedProgress));
		
		// Set adapter
		final ChannelStreamAdapter streamAdapter = new ChannelStreamAdapter(this, channelJid);
		contentView.setAdapter(streamAdapter);
		
		/*Button postButton = (Button) findViewById(R.id.postButton);
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText postContent = (EditText) findViewById(R.id.postContentTxt);
				
				String apiAddress = Preferences.getPreference(ChannelStreamActivity.this, 
						Preferences.API_ADDRESS);
				
				String url = apiAddress + "/" + channel.getJid() + "/content/posts";
				StringEntity requestEntity;
				try {
					requestEntity = new StringEntity(
							"{\"content\": \"" + postContent.getText().toString() + "\"}",
						    "UTF-8");
					requestEntity.setContentType("application/json");
//					BuddycloudHTTPHelper.post(url, true, requestEntity, ChannelStreamActivity.this);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				Toast.makeText(getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
				postContent.setText("");
				streamAdapter.refetchPosts();
			}
		});*/
	}
	
	private void loadTitle(String channelJid) {
		TextView channelTitleView = (TextView) findViewById(R.id.channelTitle);
		channelTitleView.setText(channelJid);
	}
}
