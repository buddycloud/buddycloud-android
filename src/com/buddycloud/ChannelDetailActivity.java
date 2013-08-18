package com.buddycloud;

import org.json.JSONObject;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;

public class ChannelDetailActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_details);
		
		String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
		String role = getIntent().getStringExtra(SubscribedChannelsModel.ROLE);
		
		boolean editable = role.equals(SubscribedChannelsModel.ROLE_OWNER)
				|| role.equals(SubscribedChannelsModel.ROLE_MODERATOR);

		setTitle(channelJid);
		
		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(this, channelJid);
		EditText titleTxt = (EditText) findViewById(R.id.titleTxt);
		titleTxt.setText(metadata.optString("title"));
		setEditable(editable, titleTxt);
		
		EditText descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);
		descriptionTxt.setText(metadata.optString("description"));
		setEditable(editable, descriptionTxt);
		
		EditText accessModelTxt = (EditText) findViewById(R.id.accessModelTxt);
		accessModelTxt.setText(metadata.optString("accessModel"));
		setEditable(editable, accessModelTxt);
		
		EditText creationDateTxt = (EditText) findViewById(R.id.creationDateTxt);
		creationDateTxt.setText(metadata.optString("creationDate"));
		setEditable(false, descriptionTxt);
		
		EditText channelTypeTxt = (EditText) findViewById(R.id.channelTypeTxt);
		channelTypeTxt.setText(metadata.optString("channelType"));
		setEditable(false, descriptionTxt);
		
		EditText defaultAffiliationTxt = (EditText) findViewById(R.id.defaultAffiliationTxt);
		defaultAffiliationTxt.setText(metadata.optString("defaultAffiliation"));
		setEditable(editable, descriptionTxt);
		
		ImageView avatarView = (ImageView) findViewById(R.id.avatarView);
		String avatarURL = AvatarUtils.avatarURL(this, channelJid);
		ImageHelper.picasso(this).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
	}

	private void setEditable(boolean editable, EditText titleTxt) {
		if (!editable) {
			titleTxt.setKeyListener(null);
		}
	}

}
