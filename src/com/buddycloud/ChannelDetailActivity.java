package com.buddycloud;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;

import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.utils.AvatarUtils;
import com.squareup.picasso.Picasso;

public class ChannelDetailActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_details);
		
		String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
		EditText jidTxt = (EditText) findViewById(R.id.jidTxt);
		jidTxt.setText(channelJid);
		
		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(this, channelJid);
		EditText titleTxt = (EditText) findViewById(R.id.titleTxt);
		titleTxt.setText(metadata.optString("title"));
		
		EditText descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);
		descriptionTxt.setText(metadata.optString("description"));
		
		EditText accessModelTxt = (EditText) findViewById(R.id.accessModelTxt);
		accessModelTxt.setText(metadata.optString("access_model"));
		
		EditText creationDateTxt = (EditText) findViewById(R.id.creationDateTxt);
		creationDateTxt.setText(metadata.optString("creation_date"));
		
		EditText channelTypeTxt = (EditText) findViewById(R.id.channelTypeTxt);
		channelTypeTxt.setText(metadata.optString("channel_type"));
		
		EditText defaultAffiliationTxt = (EditText) findViewById(R.id.defaultAffiliationTxt);
		defaultAffiliationTxt.setText(metadata.optString("default_affiliation"));
		
		ImageView avatarView = (ImageView) findViewById(R.id.avatarView);
		String avatarURL = AvatarUtils.avatarURL(this, channelJid);
		Picasso.with(this).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
