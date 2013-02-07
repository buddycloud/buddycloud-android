package com.buddycloud;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.buddycloud.model.Channel;

public class ChannelStreamActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);
		
		Channel channel = (Channel) getIntent().getExtras().get("CHANNEL");
		TextView channelTitleView = (TextView) findViewById(R.id.channelTitle);
		channelTitleView.setText(channel.getJid());
		
		ListView contentView = (ListView) findViewById(R.id.contentListView);
		contentView.setEmptyView(findViewById(R.id.subscribedProgress));
		contentView.setAdapter(new ChannelStreamAdapter(this, channel));
	}
}
