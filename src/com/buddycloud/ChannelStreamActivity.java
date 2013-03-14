package com.buddycloud;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.model.Channel;
import com.buddycloud.preferences.Preferences;

public class ChannelStreamActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);
		
		final Channel channel = (Channel) getIntent().getExtras().get("CHANNEL");
		TextView channelTitleView = (TextView) findViewById(R.id.channelTitle);
		channelTitleView.setText(channel.getJid());
		
		ListView contentView = (ListView) findViewById(R.id.contentListView);
		contentView.setEmptyView(findViewById(R.id.subscribedProgress));
		final ChannelStreamAdapter streamAdapter = new ChannelStreamAdapter(this, channel);
		contentView.setAdapter(streamAdapter);
		
		Button postButton = (Button) findViewById(R.id.postButton);
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
		});
	}
}
