package com.buddycloud;

import org.json.JSONObject;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;

public class ChannelDetailActivity extends SherlockActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_details);
		
		final String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
		final String role = getIntent().getStringExtra(SubscribedChannelsModel.ROLE);
		
		final boolean editable = role != null && (role.equals(SubscribedChannelsModel.ROLE_OWNER)
				|| role.equals(SubscribedChannelsModel.ROLE_MODERATOR));

		setTitle(channelJid);
		
		if (!fillFields(editable, channelJid)) {
			ChannelMetadataModel.getInstance().fill(getApplicationContext(), new ModelCallback<Void>() {
				@Override
				public void success(Void response) {
					fillFields(editable, channelJid);
				}
				
				@Override
				public void error(Throwable throwable) {
				}
			}, channelJid);
		}
		
		ImageView avatarView = (ImageView) findViewById(R.id.avatarView);
		String avatarURL = AvatarUtils.avatarURL(this, channelJid);
		ImageHelper.picasso(this).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
	}

	private boolean fillFields(boolean editable, String channelJid) {
		
		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(this, channelJid);
		
		if (metadata == null) {
			return false;
		}
		
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
		setEditable(false, creationDateTxt);
		
		EditText channelTypeTxt = (EditText) findViewById(R.id.channelTypeTxt);
		channelTypeTxt.setText(metadata.optString("channelType"));
		setEditable(false, channelTypeTxt);
		
		EditText defaultAffiliationTxt = (EditText) findViewById(R.id.defaultAffiliationTxt);
		defaultAffiliationTxt.setText(metadata.optString("defaultAffiliation"));
		setEditable(editable, defaultAffiliationTxt);
		
		return true;
	}

	private void setEditable(boolean editable, EditText editText) {
		if (!editable) {
			editText.setKeyListener(null);
		}
	}

}
