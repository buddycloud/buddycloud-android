package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.InputUtils;
import com.buddycloud.utils.TextUtils;

public class PostNewTopicActivity extends SherlockActivity {

	public static final int REQUEST_CODE = 109;
	public static final int NEW_POST_CREATED_RESULT = 209;
	
	private TextView mPostCounterTxt;
	private ProgressDialog mProgressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_topic);

        ActionbarUtil.showActionBarwithBack(this, getString(R.string.post_new_topic_title));
        
        mPostCounterTxt = (TextView) findViewById(R.id.postCounterTxt);
        
        final EditText newPostTxt = (EditText) findViewById(R.id.postTxt);
        newPostTxt.addTextChangedListener(mNewPostTextWatcher);
        newPostTxt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
					final String postTxt = newPostTxt.getText().toString();
					final String channelJid = getChannelJid();
					
					createPost(channelJid, postTxt);
				}
				return false;
			}
		});
        
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.message_topic_creation));
		mProgressDialog.setCancelable(false);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(PostNewTopicActivity.this);
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}
	
    /**
     * Create new topic post
     * 
     * @param channelJid
     * @param postContent
     */
    private void createPost(final String channelJid, final String postContent) {
    	
    	if (TextUtils.isEmpty(channelJid))
    		return ;
    	
    	if (TextUtils.isEmpty(postContent)) {
			Toast.makeText(PostNewTopicActivity.this, 
					getString(R.string.message_post_topic_mandatory), 
					Toast.LENGTH_LONG).show();
			return ;
    	}
    	
		JSONObject post = createJSONPost(postContent);
		mProgressDialog.show();
		
		PostsModel.getInstance().save(PostNewTopicActivity.this, post, 
				new ModelCallback<JSONObject>() {
					
					@Override
					public void success(JSONObject response) {
						setResult(NEW_POST_CREATED_RESULT);
						finish();
						
						Toast.makeText(getApplicationContext(), 
								getString(R.string.message_topic_created), 
								Toast.LENGTH_LONG).show();
						mProgressDialog.dismiss();
					}
					
					@Override
					public void error(Throwable throwable) {
						mProgressDialog.dismiss();
						Toast.makeText(PostNewTopicActivity.this, 
								getString(R.string.message_topic_creation_failed), 
								Toast.LENGTH_LONG).show();
					}
				}, channelJid);
    }
    
	private JSONObject createJSONPost(final String postContent) {
		JSONObject post = new JSONObject();
		try {
			post.putOpt("content", postContent);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return post;
	}
	
	private String getChannelJid() {
		return (getIntent() != null) ? getIntent().getStringExtra(GenericChannelsFragment.CHANNEL) : null;
	}
	
	private final TextWatcher mNewPostTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			mPostCounterTxt.setText(String.valueOf(s.length()));
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
}
