package com.buddycloud;

import org.json.JSONObject;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;

public class ChannelDetailActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_details);
		
		String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
		setTitle(channelJid);
		
		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(this, channelJid);
		EditText titleTxt = (EditText) findViewById(R.id.titleTxt);
		titleTxt.setText(metadata.optString("title"));
		
		EditText descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);
		descriptionTxt.setText(metadata.optString("description"));
		
		EditText accessModelTxt = (EditText) findViewById(R.id.accessModelTxt);
		accessModelTxt.setText(metadata.optString("accessModel"));
		
		EditText creationDateTxt = (EditText) findViewById(R.id.creationDateTxt);
		creationDateTxt.setText(metadata.optString("creationDate"));
		
		EditText channelTypeTxt = (EditText) findViewById(R.id.channelTypeTxt);
		channelTypeTxt.setText(metadata.optString("channelType"));
		
		EditText defaultAffiliationTxt = (EditText) findViewById(R.id.defaultAffiliationTxt);
		defaultAffiliationTxt.setText(metadata.optString("defaultAffiliation"));
		
		ImageView avatarView = (ImageView) findViewById(R.id.avatarView);
		String avatarURL = AvatarUtils.avatarURL(this, channelJid);
		ImageHelper.picasso(this).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
	}

}
