package com.buddycloud;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.InputUtils;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TimeUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;

public class ChannelDetailActivity extends SherlockActivity {

	private static final int SELECT_PHOTO_REQUEST_CODE = 110;

	private ProgressDialog mProgressDialog;
	
	private final static List<String> ROLES = Arrays.asList(new String[] {
			SubscribedChannelsModel.ROLE_MEMBER,
			SubscribedChannelsModel.ROLE_PUBLISHER });
	
	private final static Integer[] ROLE_DETAILS = new Integer[] {
			R.string.channel_details_role_member,
			R.string.channel_details_role_publisher };

	private final static List<String> ACCESS_MODELS = Arrays.asList(new String[] { 
			SubscribedChannelsModel.ACCESS_MODEL_OPEN,
			SubscribedChannelsModel.ACCESS_MODEL_AUTHORIZE });

	private final static Integer[] ACCESS_MODELS_DETAILS = new Integer[] {
		R.string.channel_details_access_model_open,
		R.string.channel_details_access_model_member };
	
	private int selectedRole;
	private int selectedAccessModel;
	
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = imageReturnedIntent.getData();
				Context context = getApplicationContext();
				File tempAvatar = null;
				try {
					tempAvatar = AvatarUtils.downSample(context, selectedImage);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(),
							getString(R.string.message_avatar_upload_failed),
							Toast.LENGTH_SHORT).show();
					return;
				}
				uploadAvatar(tempAvatar);
			}
		}
	}

	private void uploadAvatar(final File tempAvatar) {
		final String channelJid = getIntent().getStringExtra(
				GenericChannelsFragment.CHANNEL);

		Toast.makeText(getApplicationContext(),
				getString(R.string.message_avatar_uploading), Toast.LENGTH_LONG).show();
	
		MediaModel.getInstance().saveAvatar(this, null,
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						tempAvatar.delete();
						loadAvatar(channelJid, true);
						Toast.makeText(getApplicationContext(),
								getString(R.string.message_avatar_uploaded),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void error(Throwable throwable) {
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.message_avatar_upload_failed),
								Toast.LENGTH_SHORT).show();
					}
				}, Uri.fromFile(tempAvatar).toString(), channelJid);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_details);

		final String channelJid = getIntent().getStringExtra(
				GenericChannelsFragment.CHANNEL);
		final String role = getIntent().getStringExtra(
				SubscribedChannelsModel.ROLE);
		final boolean editable = SubscribedChannelsModel.canEditMetadata(role);

		ActionbarUtil.showActionBarwithBack(this, channelJid);

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.message_metadata_saving));
		mProgressDialog.setCancelable(false);
		
		if (!fillFields(editable, channelJid)) {
			ChannelMetadataModel.getInstance().fill(getApplicationContext(),
					new ModelCallback<Void>() {
						@Override
						public void success(Void response) {
							fillFields(editable, channelJid);
						}

						@Override
						public void error(Throwable throwable) {
						}
					}, channelJid);
		}

		ImageView avatarView = loadAvatar(channelJid, false);
		if (editable) {
			avatarView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent,
							SELECT_PHOTO_REQUEST_CODE);
				}
			});
		}

		final Button saveMetadataBtn = (Button) findViewById(R.id.saveSettingsBtn);
		if (editable) {
			saveMetadataBtn.setEnabled(true);
			saveMetadataBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mProgressDialog.show();
					
					JSONObject newMetadata = createMetadataJSON();
					ChannelMetadataModel.getInstance().save(
							getApplicationContext(), newMetadata,
							new ModelCallback<JSONObject>() {
								@Override
								public void success(JSONObject response) {
									mProgressDialog.dismiss();
									Toast.makeText(
											getApplicationContext(),
											getString(R.string.message_metadata_updated),
											Toast.LENGTH_SHORT).show();
									finish();
								}

								@Override
								public void error(Throwable throwable) {
									mProgressDialog.dismiss();
									Toast.makeText(
											getApplicationContext(),
											getString(R.string.message_metadata_update_failed)
													+ throwable.getMessage(),
											Toast.LENGTH_SHORT).show();
								}

							}, channelJid);
				}

			});
		}
		else {
			saveMetadataBtn.setEnabled(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ImageLoader.getInstance().resume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		ImageLoader.getInstance().stop();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(ChannelDetailActivity.this);
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}

	private ImageView loadAvatar(final String channelJid, boolean skipCache) {
		ImageView avatarView = (ImageView) findViewById(R.id.bcProfilePic);
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.showImageOnFail(R.drawable.ic_avatar)
				.showImageOnLoading(R.drawable.ic_avatar)
				.preProcessor(ImageHelper.createRoundProcessor(16, false, -1))
				.resetViewBeforeLoading(true)
				.build();

		String avatarURL = AvatarUtils.avatarURL(this, channelJid);
		if (skipCache) {
			DiscCacheUtil.removeFromCache(avatarURL, ImageLoader.getInstance()
					.getDiscCache());
			MemoryCacheUtil.removeFromCache(avatarURL, ImageLoader
					.getInstance().getMemoryCache());
		}

		ImageLoader.getInstance().displayImage(avatarURL, avatarView, dio);

		return avatarView;
	}

	private JSONObject createMetadataJSON() {
		Map<String, String> metadata = new HashMap<String, String>();

		metadata.put("title", getEditText(R.id.titleTxt));
		metadata.put("description", getEditText(R.id.descriptionTxt));
		metadata.put("access_model", ACCESS_MODELS.get(selectedAccessModel));
		metadata.put("default_affiliation", ROLES.get(selectedRole));

		return new JSONObject(metadata);
	}

	private String getEditText(int resId) {
		return ((EditText) findViewById(resId)).getText().toString();
	}

	private void setSpinnerText(Spinner spinner, 
			List<String> values,
			Integer[] valueDetails,
			String selectedValue) {
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, R.layout.spinner_item_layout);
		
		for (int pos=0; pos < valueDetails.length; pos++) {
			adapter.add(getString(valueDetails[pos]));
		}
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(values.indexOf(selectedValue));
	}

	private boolean fillFields(boolean editable, String channelJid) {

		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(
				this, channelJid);

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
		setSpinnerText(accessModelTxt, ACCESS_MODELS, ACCESS_MODELS_DETAILS, metadata.optString("accessModel"));
		setSelectedAccessModel(ACCESS_MODELS.indexOf(metadata.optString("accessModel")));
		accessModelTxt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				setSelectedAccessModel(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		setEditable(editable, accessModelTxt);

		TextView creationDateTxt = (TextView) findViewById(R.id.creationDateTxt);
		try {
			if (metadata != null && !TextUtils.isEmpty(metadata.optString("creationDate"))) {
				long creationDateTime = TimeUtils.fromISOToDate(metadata.optString("creationDate")).getTime();
				creationDateTxt.setText(
						DateUtils.getRelativeTimeSpanString(creationDateTime, 
								new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
			}
	
		} catch (ParseException e) {
			e.printStackTrace();
		}

		TextView channelTypeTxt = (TextView) findViewById(R.id.channelTypeTxt);
		channelTypeTxt.setText(metadata.optString("channelType"));

		Spinner defaultAffiliationTxt = (Spinner) findViewById(R.id.defaultAffiliationTxt);
		setSpinnerText(defaultAffiliationTxt, ROLES, ROLE_DETAILS, metadata.optString("defaultAffiliation"));
		setSelectedRole(ROLES.indexOf(metadata.optString("defaultAffiliation")));
		defaultAffiliationTxt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				setSelectedRole(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
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

	public int getSelectedRole() {
		return selectedRole;
	}

	public int getSelectedAccessModel() {
		return selectedAccessModel;
	}

	public void setSelectedRole(int selectedRole) {
		this.selectedRole = selectedRole;
	}

	public void setSelectedAccessModel(int selectedAccessModel) {
		this.selectedAccessModel = selectedAccessModel;
	}
}
