package com.buddycloud;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;

public class ChannelDetailActivity extends SherlockActivity {

	private static final int SELECT_PHOTO_REQUEST_CODE = 110;
	
	private final static List<String> ROLES = Arrays.asList(new String[] {
			SubscribedChannelsModel.ROLE_MEMBER, 
			SubscribedChannelsModel.ROLE_MODERATOR, 
			SubscribedChannelsModel.ROLE_PUBLISHER});
	
	private final static List<String> ACCESS_MODELS = Arrays.asList(new String[] {
			SubscribedChannelsModel.ACCESS_MODEL_OPEN,
			SubscribedChannelsModel.ACCESS_MODEL_WHITELIST});
	
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();

				Bitmap selectedBitmap = BitmapFactory.decodeFile(filePath);
				// TODO Downsample and upload
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_details);
		
		final String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
		final String role = getIntent().getStringExtra(SubscribedChannelsModel.ROLE);
		
		final boolean editable = SubscribedChannelsModel.canEditMetadata(role);

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
		if (editable) {
			avatarView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, SELECT_PHOTO_REQUEST_CODE);  
				}
			});
		}
		
		final FrameLayout postMetadataBtn = (FrameLayout) findViewById(R.id.postMetadataBtn);
		postMetadataBtn.setVisibility(editable ? View.VISIBLE : View.GONE);
		
		postMetadataBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				postMetadataBtn.setVisibility(View.GONE);
				findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
				
				JSONObject newMetadata = createMetadataJSON();
				ChannelMetadataModel.getInstance().save(getApplicationContext(), newMetadata, 
						new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Toast.makeText(getApplicationContext(), 
								"Channel metadata updated", 
								Toast.LENGTH_SHORT).show();
						finish();
					}

					@Override
					public void error(Throwable throwable) {
						postMetadataBtn.setVisibility(View.VISIBLE);
						findViewById(R.id.progressBar).setVisibility(View.GONE);
						Toast.makeText(getApplicationContext(), 
								"Could update channel metadata. " + throwable.getMessage(), 
								Toast.LENGTH_SHORT).show();
					}
					
				}, channelJid);
			}

		});
	}

	private JSONObject createMetadataJSON() {
		Map<String, String> metadata = new HashMap<String, String>();
		
		metadata.put("title", getEditText(R.id.titleTxt));
		metadata.put("description", getEditText(R.id.descriptionTxt));
		metadata.put("accessModel", getSpinnerText(R.id.accessModelTxt));
		metadata.put("defaultAffiliation", getSpinnerText(R.id.defaultAffiliationTxt));
		
		return new JSONObject(metadata);
	}

	private String getEditText(int resId) {
		return ((EditText) findViewById(resId)).getText().toString();
	}
	
	private String getSpinnerText(int resId) {
		Spinner spinner = (Spinner) findViewById(resId);
		return spinner.getSelectedItem().toString();
	}
	
	private void setSpinnerText(Spinner spinner, List<String> values, String selectedValue) {
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, R.layout.spinner_item_layout);
		for (String value : values) {
			adapter.add(value);
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(values.indexOf(selectedValue));
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
		
		Spinner accessModelTxt = (Spinner) findViewById(R.id.accessModelTxt);
		setSpinnerText(accessModelTxt, ACCESS_MODELS, metadata.optString("accessModel"));
		setEditable(editable, accessModelTxt);
		
		EditText creationDateTxt = (EditText) findViewById(R.id.creationDateTxt);
		creationDateTxt.setText(metadata.optString("creationDate"));
		setEditable(false, creationDateTxt);
		
		EditText channelTypeTxt = (EditText) findViewById(R.id.channelTypeTxt);
		channelTypeTxt.setText(metadata.optString("channelType"));
		setEditable(false, channelTypeTxt);
		
		Spinner defaultAffiliationTxt = (Spinner) findViewById(R.id.defaultAffiliationTxt);
		setSpinnerText(defaultAffiliationTxt, ROLES, metadata.optString("defaultAffiliation"));
		setEditable(editable, defaultAffiliationTxt);
		
		return true;
	}

	private void setEditable(boolean editable, EditText editText) {
		if (!editable) {
			editText.setKeyListener(null);
		}
	}
	
	private void setEditable(boolean editable, Spinner spinner) {
		if (!editable) {
			spinner.setEnabled(false);
			spinner.setClickable(false);
		}
	}

}
