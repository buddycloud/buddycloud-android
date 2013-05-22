package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.buddycloud.card.CardListAdapter;
import com.buddycloud.card.PostCard;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SyncModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ChannelStreamFragment extends ContentFragment {

	private String channelJid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.channelJid = getArguments().getString(SubscribedChannelsFragment.CHANNEL);
	
		final View view = inflater.inflate(R.layout.fragment_channel_stream, container, false);
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.VISIBLE);
		//fetchPosts(channelJid);
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		SmartImageView avatarView = (SmartImageView) view.findViewById(R.id.bcCommentPic);
		avatarView.setImageUrl(avatarURL, R.drawable.personal_50px);
		
		final ImageView postButton = (ImageView) view.findViewById(R.id.postButton);
		postButton.setEnabled(false);
		
		EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
		
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (!postButton.isEnabled()) {
					return;
				}
				
				final EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
				
				JSONObject post = createPost(postContent);
				
				SyncModel.getInstance().save(getActivity(), post, new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Toast.makeText(getActivity().getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
						postContent.setText("");
						syncd();
					}
					
					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
					}
				}, channelJid);
				
			}

			private JSONObject createPost(final EditText postContent) {
				JSONObject post = new JSONObject();
				try {
					post.putOpt("content", postContent.getText().toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return post;
			}
		});
		
		configurePostSection(postContent, postButton);
		
		return view;
	}
	
	public void syncd() {
		getView().findViewById(R.id.subscribedProgress).setVisibility(View.GONE);
		JSONArray posts = SyncModel.getInstance().postsFromChannel(channelJid);
		ListView contentView = (ListView) getView().findViewById(R.id.postsStream);
		CardListAdapter cardAdapter = new CardListAdapter();
		contentView.setAdapter(cardAdapter);
		for (int i = 0; i < posts.length(); i++) {
			JSONObject j = posts.optJSONObject(i);
			PostCard card = toCard(j, channelJid);
			cardAdapter.addCard(card);
		}
		cardAdapter.notifyDataSetChanged();
	}
	
	private PostCard toCard(JSONObject post, final String channelJid) {
		final String postId = post.optString("id");
		String postAuthor = post.optString("author");
		String postContent = post.optString("content");
		String published = post.optString("published");
		
		String avatarURL = AvatarUtils.avatarURL(getActivity(), postAuthor);
		
		Integer commentCount = SyncModel.getInstance().commentsFromPost(postId).length();
		
		PostCard postCard = new PostCard(postAuthor, avatarURL, postContent, published, commentCount);
		postCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContentFragment postDetailsFrag = new PostDetailsFragment();
				Bundle args = new Bundle();
				args.putString(PostDetailsFragment.POST_ID, postId);
				args.putString(SubscribedChannelsFragment.CHANNEL, channelJid);
				
				postDetailsFrag.setArguments(args);
				
				MainActivity activity = (MainActivity) getActivity();
				activity.setRightFragment(postDetailsFrag);
			}
		});
		return postCard;
	}
	
	private void loadTitle(String channelJid) {
		SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
		activity.getSupportActionBar().setTitle(channelJid);
	}
	
	public static void configurePostSection(EditText postContent, final ImageView postButton) {
		postContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				boolean wasEnabled = postButton.isEnabled();
				boolean enabled = arg0 != null && arg0.length() > 0;
				postButton.setEnabled(enabled);
				if (!enabled) {
					postButton.setImageResource(R.drawable.speech_balloon_plus_disabled);
				} else if (!wasEnabled) {
					postButton.setImageResource(R.drawable.speech_balloon_plus);
				}
			}
			
		});
	}

	@Override
	void attached() {
		loadTitle(channelJid);
	}
}
