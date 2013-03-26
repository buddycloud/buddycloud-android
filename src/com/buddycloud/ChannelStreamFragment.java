package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.buddycloud.card.PostCard;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.fima.cardsui.views.CardUI;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ChannelStreamFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_channel_stream, container, false);
		final String channelJid = getArguments().getString(SubscribedChannelsFragment.CHANNEL);
		
		loadTitle(channelJid);
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.VISIBLE);
		fetchPosts(channelJid);
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		SmartImageView avatarView = (SmartImageView) view.findViewById(R.id.bcProfilePic);
		avatarView.setImageUrl(avatarURL, R.drawable.personal_50px);
		
		View postButton = view.findViewById(R.id.postButton);
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
				
				JSONObject post = createPost(postContent);
				
				PostsModel.getInstance().save(getActivity(), post, new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Toast.makeText(getActivity().getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
						postContent.setText("");
						fetchPosts(channelJid);
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
		
		return view;
	}
	
	public void fetchPosts(final String channelJid) {
		PostsModel.getInstance().refresh(getActivity(), new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				getView().findViewById(R.id.subscribedProgress).setVisibility(View.GONE);
				
				JSONArray posts = PostsModel.getInstance().postsFromChannel(getActivity(), channelJid);
				for (int i = 0; i < posts.length(); i++) {
					JSONObject j = posts.optJSONObject(i);
					CardUI contentView = (CardUI) getView().findViewById(R.id.postsStream);
					PostCard card = toCard(j, channelJid);
					contentView.addCard(card);
					contentView.refresh();
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				System.err.println(throwable);
				
			}
		}, channelJid);
	}
	
	private PostCard toCard(JSONObject post, final String channelJid) {
		final String postId = post.optString("id");
		String postAuthor = post.optString("author");
		String postContent = post.optString("content");
		String avatarURL = AvatarUtils.avatarURL(getActivity(), postAuthor);
		
		Integer commentCount = PostsModel.getInstance().commentsFromPost(postId).length();
		
		PostCard postCard = new PostCard(postAuthor, avatarURL, postContent, commentCount);
		postCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Fragment postDetailsFrag = new PostDetailsFragment();
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
}
