package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.buddycloud.customviews.MeasuredMediaView;
import com.buddycloud.customviews.MeasuredMediaView.MeasureListener;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.SearchChannelsFragment;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.FileUtils;
import com.buddycloud.utils.ImageHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ShareActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share);
		
		final Intent intent = getIntent();
		if (!intent.getAction().equals(Intent.ACTION_SEND)) {
			return;
		}

		String myJid = Preferences.getPreference(getApplicationContext(), Preferences.MY_CHANNEL_JID);
		EditText targetChannelView = (EditText) findViewById(R.id.channelText);
		
		String avatarURL = AvatarUtils.avatarURL(this, myJid);
		ImageView avatarView = (ImageView) findViewById(R.id.bcProfilePic);
		
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.showImageOnFail(R.drawable.personal_50px)
				.showImageOnLoading(R.drawable.personal_50px)
				.preProcessor(ImageHelper.createRoundProcessor(16, false, -1))
				.build();
		ImageLoader.getInstance().displayImage(avatarURL, avatarView, dio);
		
		targetChannelView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent searchActivityIntent = new Intent();
					searchActivityIntent.setClass(ShareActivity.this, SearchActivity.class);
					searchActivityIntent.putExtra(SearchChannelsFragment.AFFILIATIONS, new String[] {
							SubscribedChannelsModel.ROLE_OWNER, 
							SubscribedChannelsModel.ROLE_MODERATOR, 
							SubscribedChannelsModel.ROLE_PUBLISHER});
					startActivityForResult(searchActivityIntent, SearchActivity.REQUEST_CODE);
				}
				return true;
			}
		});
		
		final String mediaType = intent.getType();
		
		if (mediaType.contains("image/")) {
			layoutShareImage();
		} else if (mediaType.contains("video/")) {
			layoutShareVideo();
		} else if (mediaType.contains("text/")) {
			layoutShareText();
		}
		
		RelativeLayout shareMediaBtn = (RelativeLayout) findViewById(R.id.shareMediaBtn);
		shareMediaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mediaType.contains("image/") || 
						mediaType.contains("video/")) {
					shareMedia();
				} else if (mediaType.contains("text/")) {
					shareText();
				}
			}
		});
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

	protected void layoutShareText() {
		findViewById(R.id.shareImagePreview).setVisibility(View.GONE);
		findViewById(R.id.captionTextAlt).setVisibility(View.GONE);
		
		EditText caption = (EditText) findViewById(R.id.captionText);
		String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
		caption.setText(sharedText);
		
		findViewById(R.id.captionText).requestFocus();
	}

	protected void layoutShareVideo() {
		findViewById(R.id.captionText).setVisibility(View.GONE);
		
		Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
		final Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
				FileUtils.getRealPathFromURI(this, uri),
		        MediaStore.Images.Thumbnails.MINI_KIND);
		final MeasuredMediaView imageView = (MeasuredMediaView) findViewById(R.id.shareImagePreview);
		imageView.setImageBitmap(thumbnail);
		
		imageView.setMeasureListener(new MeasureListener() {
			@Override
			public void measure(int widthMeasureSpec, int heightMeasureSpec) {
				imageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(
						thumbnail, 8, true, widthMeasureSpec));
			}
		});
		findViewById(R.id.captionTextAlt).requestFocus();
		imageView.setVisibility(View.VISIBLE);
	}

	protected void layoutShareImage() {
		findViewById(R.id.captionText).setVisibility(View.GONE);
		
		final MeasuredMediaView imageView = (MeasuredMediaView) findViewById(R.id.shareImagePreview);
		final Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
		
		imageView.setMeasureListener(new MeasureListener() {
			@Override
			public void measure(int widthMeasureSpec, int heightMeasureSpec) {
				DisplayImageOptions dio = new DisplayImageOptions.Builder()
						.cloneFrom(ImageHelper.defaultImageOptions())
						.preProcessor(ImageHelper.createRoundProcessor(8, true, widthMeasureSpec))
						.build();
				ImageLoader.getInstance().displayImage(uri.toString(), imageView, dio);
			}
		});
		findViewById(R.id.captionTextAlt).requestFocus();
		imageView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SearchActivity.REQUEST_CODE) {
			if (data != null) {
				final String channelJid = data.getStringExtra(GenericChannelsFragment.CHANNEL);
				EditText targetChannelView = (EditText) findViewById(R.id.channelText);
				targetChannelView.setText(channelJid);
			}
		}
	}
	
	protected void shareText() {
		showProgress();
		postToBuddycloud();
	}
	
	protected void shareMedia() {
		
		Uri uri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
		
		showProgress();
		
		Toast.makeText(getApplicationContext(),
				getString(R.string.message_media_uploading), 
				Toast.LENGTH_SHORT).show();
		EditText targetChannelView = (EditText) findViewById(R.id.channelText);
		MediaModel.getInstance().save(getApplicationContext(), null, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.message_media_uploaded),
						Toast.LENGTH_SHORT).show();
				postToBuddycloud(response.optString("entityId"), 
						response.optString("id"));
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.message_media_upload_failed),
						Toast.LENGTH_LONG).show();
				hideProgress();
			}
		}, uri.toString(), targetChannelView.getText().toString());
	}

	protected void showProgress() {
		findViewById(R.id.shareMediaBtn).setVisibility(View.GONE);
		findViewById(R.id.uploadProgress).setVisibility(View.VISIBLE);
	}

	protected void hideProgress() {
		findViewById(R.id.shareMediaBtn).setVisibility(View.VISIBLE);
		findViewById(R.id.uploadProgress).setVisibility(View.GONE);
	}
	
	protected void postToBuddycloud() {
		postToBuddycloud(null, null);
	}
	
	protected void postToBuddycloud(String picChannel, String picId) {
		EditText targetChannelView = (EditText) findViewById(R.id.channelText);
		
		JSONObject post = null;
		try {
			post = createPost(picChannel, picId);
		} catch (JSONException e) {
//			Toast.makeText(getApplicationContext(),
//					getString(R.string.message_post_creation_failed), 
//					Toast.LENGTH_LONG).show();
//			hideProgress();
			return;
		}
		
		PostsModel.getInstance().save(this, post, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
//				Toast.makeText(getApplicationContext(),
//						getString(R.string.message_post_created),
//						Toast.LENGTH_LONG).show();
//				finish();
			}
			
			@Override
			public void error(Throwable throwable) {
//				Toast.makeText(getApplicationContext(),
//						getString(R.string.message_post_creation_failed), 
//						Toast.LENGTH_LONG).show();
//				hideProgress();
			}
		}, targetChannelView.getText().toString());
		
	}

	private JSONObject createPost(String picChannel, String picId) throws JSONException {
		
		JSONObject post = new JSONObject();
		
		if (picId != null && picChannel != null) {
			EditText caption = (EditText) findViewById(R.id.captionTextAlt);
			post.putOpt("content", caption.getText().toString());
			JSONArray mediaArray = new JSONArray();
			JSONObject mediaObject = new JSONObject();
			mediaObject.putOpt("id", picId);
			mediaObject.putOpt("channel", picChannel);
			mediaArray.put(mediaObject);
			post.putOpt("media", mediaArray);
		} else {
			EditText caption = (EditText) findViewById(R.id.captionText);
			post.putOpt("content", caption.getText().toString());
		}
		return post;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
}
