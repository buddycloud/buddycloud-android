package com.buddycloud.fragments;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.card.CardListAdapter;
import com.buddycloud.card.PostCard;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.slidingmenu.lib.app.SlidingFragmentActivity;
import com.squareup.picasso.Picasso;

public class ChannelStreamFragment extends ContentFragment {

	private String channelJid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.channelJid = getArguments().getString(GenericChannelsFragment.CHANNEL);
	
		final View view = inflater.inflate(R.layout.fragment_channel_stream, container, false);
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.VISIBLE);
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		ImageView avatarView = (ImageView) view.findViewById(R.id.bcCommentPic);
		Picasso.with(getActivity()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
		
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
				
				PostsModel.getInstance().save(getActivity(), post, new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Toast.makeText(getActivity().getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
						postContent.setText("");
						syncd(getActivity());
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
	
	public void syncd(Context context) {
		getView().findViewById(R.id.subscribedProgress).setVisibility(View.GONE);
		List<String> postsIds = PostsModel.getInstance().cachedPostsFromChannel(channelJid);
		ListView contentView = (ListView) getView().findViewById(R.id.postsStream);
		CardListAdapter cardAdapter = new CardListAdapter();
		contentView.setAdapter(cardAdapter);
		
		for (String postId : postsIds) {
			JSONObject post = PostsModel.getInstance().postWithId(postId, channelJid);
			PostCard card = toCard(post, channelJid);
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
		
		Integer commentCount = PostsModel.getInstance().cachedCommentsFromPost(postId).size();
		
		PostCard postCard = new PostCard(postAuthor, avatarURL, postContent, published, commentCount);
		postCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showPostDetailFragment(channelJid, postId);
			}

			private void showPostDetailFragment(final String channelJid,
					final String postId) {
				MainActivity activity = (MainActivity) getActivity();
				activity.showPostDetailFragment(channelJid, postId);
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
	public void attached() {
		loadTitle(channelJid);
	}

	@Override
	public void createOptions(Menu menu) {
		if (getSherlockActivity() == null) {
			return;
		}
		getSherlockActivity().getSupportMenuInflater().inflate(
				R.menu.channel_fragment_options, menu);
	}

	@Override
	public boolean menuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
}
