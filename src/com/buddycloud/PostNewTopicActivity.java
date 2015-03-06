package com.buddycloud;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.SearchChannelsFragment;
import com.buddycloud.log.Logger;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.MediaModelCallback;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.InputUtils;
import com.buddycloud.utils.NetworkUtil;
import com.buddycloud.utils.TextUtils;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenVideo;
import com.kbeanie.imagechooser.api.MediaChooserListener;
import com.kbeanie.imagechooser.api.MediaChooserManager;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * This class used to show the post activity controller that
 * allow the user to attach media and post content.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class PostNewTopicActivity extends SherlockActivity
		implements MediaChooserListener {

	private static final String TAG = PostNewTopicActivity.class.getName();
	
	public static final int REQUEST_CODE = 109;
	public static final int NEW_POST_CREATED_RESULT = 209;
	
	private TextView mPostContentTxtCounter;
	private MediaChooserManager mChooserManager;
	private ProgressDialog mProgressDialog;
	
	private String attachedMediaChannel;
	private String attachedMediaId;
	private boolean isAttachMediaInProgress;
	private boolean isShareIntent;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_topic);

		ActionbarUtil.showActionBarwithBack(this, getString(R.string.post_new_topic_title));
		
        // Post Content
        final EditText postContentTxt = (EditText)findViewById(R.id.postTxt);
        postContentTxt.addTextChangedListener(mNewPostTextWatcher);
        postContentTxt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
					final String postTxt = postContentTxt.getText().toString();
					final TextView channelJidTxt = (TextView)findViewById(R.id.channelJidTxt);
					
					createPost(channelJidTxt.getText().toString(), postTxt);
				}
				return false;
			}
		});
        mPostContentTxtCounter = (TextView)findViewById(R.id.postCounterTxt);
        
        // media selection
        ImageView mediaSelectionIcon = (ImageView)findViewById(R.id.mediaSelection);
        mediaSelectionIcon.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mediaSelection();
			}
		});
        
        // share intent
		final Intent intent = getIntent();
		if (intent != null && intent.getAction() != null) {
			if (intent.getAction().equals(Intent.ACTION_SEND)) {
				isShareIntent = true;
				
				final String mediaType = intent.getType();
				if (mediaType.contains("image/")
						|| mediaType.contains("video/")) {
					
					Uri mediaUri = (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM);
					showAttachment(mediaUri, mediaUri);	
				} else if (mediaType.contains("text/")) {
					String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
					postContentTxt.setText(sharedText);
				}
			}
		}
		
        final String channelJid = getChannelJid();
        TextView postChannelJidTxt = (TextView)findViewById(R.id.channelJidTxt);
        postChannelJidTxt.setText(channelJid);
        if (isShareIntent) {
        	postChannelJidTxt.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					channelSelection();
				}
			}); 
        }
        
        // channel JID selection arrow
        ImageView postChannelJidArrowIcon = (ImageView)findViewById(R.id.bcArrowRight);
        if (isShareIntent) {
        	postChannelJidArrowIcon.setVisibility(View.VISIBLE);
        	postChannelJidArrowIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					channelSelection();
				}
			}); 
        }

        // Progress dialog
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.message_post_creation));
		mProgressDialog.setCancelable(false);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.post_screen_options, menu);

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		final EditText postContent = (EditText)findViewById(R.id.postTxt);
		final TextView channelJid = (TextView)findViewById(R.id.channelJidTxt);
		
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(PostNewTopicActivity.this);
        		finish();
        		return true;
        	case R.id.menu_post_send:
        		createPost(channelJid.getText().toString(), postContent.getText().toString());
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK
				&& requestCode == ChooserType.REQUEST_PICK_PICTURE_OR_VIDEO) {
			mChooserManager.submit(requestCode, data);
		}
		else if (requestCode == SearchActivity.REQUEST_CODE) {
			if (data != null) {
				final TextView channelJidTxt = (TextView)findViewById(R.id.channelJidTxt);
				final String channelJid = data.getStringExtra(GenericChannelsFragment.CHANNEL);
				channelJidTxt.setText(channelJid);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

    /**
     * Create new topic post
     * 
     * @param channelJid
     * @param postContent
     */
    private void createPost(final String channelJid, final String postContent) {
    	
    	if (!NetworkUtil.isNetworkAvailable()) {
			Toast.makeText(PostNewTopicActivity.this, 
					getString(R.string.message_no_network_connection), 
					Toast.LENGTH_LONG).show();
    	}
    	
    	if (isAttachMediaInProgress) {
			Toast.makeText(PostNewTopicActivity.this, 
					getString(R.string.message_post_topic_attachment_in_progress), 
					Toast.LENGTH_LONG).show();
    		return ;
    	}
    	
    	if (TextUtils.isEmpty(channelJid)) {
			Toast.makeText(PostNewTopicActivity.this, 
					getString(R.string.message_post_topic_channelJid_mandatory), 
					Toast.LENGTH_LONG).show();	
    		return ;
    	}
    		
    	if (TextUtils.isEmpty(postContent)) {
			Toast.makeText(PostNewTopicActivity.this, 
					getString(R.string.message_post_topic_mandatory), 
					Toast.LENGTH_LONG).show();
			return ;
    	}
    	
		mProgressDialog.show();
		
		JSONObject post = createJSONPost(postContent);
		PostsModel.getInstance().save(PostNewTopicActivity.this, post, 
				new ModelCallback<JSONObject>() {
					
					@Override
					public void success(JSONObject response) {
						setResult(NEW_POST_CREATED_RESULT);
						finish();
						
						Toast.makeText(getApplicationContext(), 
								getString(R.string.message_post_created), 
								Toast.LENGTH_LONG).show();
						mProgressDialog.dismiss();
					}
					
					@Override
					public void error(Throwable throwable) {
						mProgressDialog.dismiss();
						Toast.makeText(PostNewTopicActivity.this, 
								getString(R.string.message_post_creation_failed), 
								Toast.LENGTH_LONG).show();
					}
				}, channelJid);
    }
    
	private JSONObject createJSONPost(final String postContent) {
		JSONObject post = new JSONObject();
		try {
			// post content info
			post.putOpt("content", postContent);
			
			// attach media info
			if (getAttachedMediaChannel() != null
					&& getAttachedMediaId() != null) {
				
				JSONArray mediaArray = new JSONArray();
				JSONObject mediaObject = new JSONObject();
				mediaObject.putOpt("id", getAttachedMediaId());
				mediaObject.putOpt("channel", getAttachedMediaChannel());
				mediaArray.put(mediaObject);
				post.putOpt("media", mediaArray);
			}
			
		} catch (JSONException e) {
			Logger.error(TAG, "createJSONPost error: ", e);
		}
		return post;
	}
	
	private String getAttachedMediaChannel() {
		return attachedMediaChannel;
	}

	private void setAttachedMediaChannel(String attachedMediaChannel) {
		this.attachedMediaChannel = attachedMediaChannel;
	}

	private String getAttachedMediaId() {
		return attachedMediaId;
	}

	private void setAttachedMediaId(String attachedMediaId) {
		this.attachedMediaId = attachedMediaId;
	}
	
	private String getChannelJid() {
		return (getIntent() != null) ? getIntent().getStringExtra(GenericChannelsFragment.CHANNEL) : "";
	}
	
	private View getThumbnailMediaView(int viewResc) {
		return (View)findViewById(viewResc);
	}

	private void channelSelection() {
		Intent searchActivityIntent = new Intent();
		searchActivityIntent.setClass(PostNewTopicActivity.this, SearchActivity.class);
		searchActivityIntent.putExtra(SearchChannelsFragment.AFFILIATIONS, new String[] {
				SubscribedChannelsModel.ROLE_OWNER, 
				SubscribedChannelsModel.ROLE_MODERATOR, 
				SubscribedChannelsModel.ROLE_PUBLISHER});
		startActivityForResult(searchActivityIntent, SearchActivity.REQUEST_CODE);
	}
	
	private void mediaSelection() {
		mChooserManager = new MediaChooserManager(this,
				ChooserType.REQUEST_PICK_PICTURE_OR_VIDEO);
		mChooserManager.setMediaChooserListener(this);
		
		try {
			String path = mChooserManager.choose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showAttachment(final Uri originalFilePath, 
			final Uri thumbnailFilePath) {
		
		if (TextUtils.isEmpty(originalFilePath.toString())
				|| TextUtils.isEmpty(thumbnailFilePath.toString())) {
			return;
		}
		
		runOnUiThread(new Runnable() {
			public void run() {
				final View thumbnailAttachmentPanel = getThumbnailMediaView(R.id.thumbnailAttachmentPanel);
				final TextView channelJid = (TextView)findViewById(R.id.channelJidTxt);
				
				thumbnailAttachmentPanel.setVisibility(View.VISIBLE);
				ImageView cancelBtn = (ImageView)getThumbnailMediaView(R.id.closeBtn);

				ImageView mediaImage = (ImageView)getThumbnailMediaView(R.id.thumbnailMediaImage);
				
				// display selected media image preview
				DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.build();
				ImageLoader.getInstance().displayImage(thumbnailFilePath.toString(), mediaImage, dio);

				// cancel button
				cancelBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						removeAttachment();
					}
				});
				
				// upload the file on server
				if (NetworkUtil.isNetworkAvailable()) {
					final ProgressBar pb = (ProgressBar)getThumbnailMediaView(R.id.progressBar);
					pb.setVisibility(View.VISIBLE);
					MediaModel.getInstance().saveWithProgress(getApplicationContext(), null, new MediaModelCallback<JSONObject>() {

						@Override
						public void success(JSONObject response) {
							Toast.makeText(getApplicationContext(),
									getString(R.string.message_media_uploaded),
									Toast.LENGTH_SHORT).show();
							
							setAttachedMediaChannel(response.optString("entityId"));
							setAttachedMediaId(response.optString("id"));
							isAttachMediaInProgress = false;
						}

						@Override
						public void error(Throwable throwable) {
							Toast.makeText(getApplicationContext(),
									getString(R.string.message_media_upload_failed),
									Toast.LENGTH_LONG).show();
							
							// reset attachment properties
							setAttachedMediaChannel(null);
							setAttachedMediaId(null);
							isAttachMediaInProgress = false;
						}

						@Override
						public void progress(final long num, final long totalSize) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									int progress = (int) ((num / (float) totalSize) * 100);
									pb.setProgress(progress);
									
									Logger.info(TAG, "Uploading the media progress (" + progress + 
											") total of (" + totalSize + ")");
								}
							});
						}
					}, originalFilePath.toString(), channelJid.getText().toString());
					isAttachMediaInProgress = true;
				}
			}
		});
	}
	
	private void removeAttachment() {
		
		// reset attachment properties
		setAttachedMediaChannel(null);
		setAttachedMediaId(null);
		isAttachMediaInProgress = false;
		
		runOnUiThread(new Runnable() {
			public void run() {
				final View thumbnailAttachmentPanel = getThumbnailMediaView(R.id.thumbnailAttachmentPanel);
				final ImageView mediaImage = (ImageView)getThumbnailMediaView(R.id.thumbnailMediaImage);
				
				mediaImage.setImageBitmap(null);
				thumbnailAttachmentPanel.setVisibility(View.GONE);
			}
		});
	}
	
	private final TextWatcher mNewPostTextWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (mPostContentTxtCounter != null) {
				mPostContentTxtCounter.setText(String.valueOf(s.length()));
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	/* ######################################
	 * ### Image Chooser Delegate Methods ### 
	 * ######################################
	 */

	/**
	 * Selected image object
	 * 
	 * @param image
	 */
	@Override
	public void onImageChosen(ChosenImage image) {
		if (image == null) return;
		
		Uri originalFilePathUri = Uri.fromFile(new File(image.getFilePathOriginal()));
		Uri thumbnailUri = Uri.fromFile(new File(image.getFilePathOriginal()));
		
		showAttachment(originalFilePathUri, thumbnailUri);
	}

	/**
	 * Selected video object
	 * 
	 * @param video
	 */
	@Override
	public void onVideoChosen(ChosenVideo video) {
		if (video == null) return;
		
		Uri originalFilePathUri = Uri.fromFile(new File(video.getVideoFilePath()));
		Uri thumbnailUri = Uri.fromFile(new File(video.getVideoPreviewImage()));
		
		showAttachment(originalFilePathUri, thumbnailUri);
	}
	
	/**
	 * If the error occurred during the media selection
	 * 
	 * @param error
	 */
	@Override
	public void onError(String error) {
		removeAttachment();
	}
}
