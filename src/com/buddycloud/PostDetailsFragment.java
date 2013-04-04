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
import android.widget.TextView;
import android.widget.Toast;

import com.buddycloud.card.CommentCard;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.fima.cardsui.views.CardUI;

public class PostDetailsFragment extends Fragment {

	public static final String POST_ID = "com.buddycloud.POST_ID";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_post_details, container, false);
		final String postId = getArguments().getString(POST_ID);
		final String channelJid = getArguments().getString(SubscribedChannelsFragment.CHANNEL);
		
		JSONObject post = PostsModel.getInstance().getById(getActivity(), postId, channelJid);

		((TextView) view.findViewById(R.id.title)).setText(post.optString("author"));
		
		((SmartImageView) view.findViewById(R.id.bcProfilePic)).setImageUrl(
				AvatarUtils.avatarURL(getActivity(), post.optString("author")), 
				R.drawable.personal_50px);
		
		((TextView) view.findViewById(R.id.bcPostContent)).setText(post.optString("content"));
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		SmartImageView avatarView = (SmartImageView) view.findViewById(R.id.bcCommentPic);
		avatarView.setImageUrl(avatarURL, R.drawable.personal_50px);
		
		loadComments(view, postId);
		
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
						fetchPosts(channelJid, postId);
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
					post.putOpt("replyTo", postId);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return post;
			}
		});
		
		return view;
	}

	public void fetchPosts(final String channelJid, final String postId) {
		CardUI cardsUI = (CardUI) getView().findViewById(R.id.postsStream);
		cardsUI.clearCards();
		
		final View progress = getView().findViewById(R.id.subscribedProgress);
		progress.setVisibility(View.VISIBLE);
		
		PostsModel.getInstance().refresh(getActivity(), new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				progress.setVisibility(View.GONE);
				loadComments(getView(), postId);
			}
			
			@Override
			public void error(Throwable throwable) {
				System.err.println(throwable);
				
			}
		}, channelJid);
	}
	
	private void loadComments(final View view, final String postId) {
		JSONArray comments = PostsModel.getInstance().commentsFromPost(postId);
		for (int i = 0; i < comments.length(); i++) {
			JSONObject comment = comments.optJSONObject(i);
			CardUI contentView = (CardUI) view.findViewById(R.id.postsStream);
			contentView.addCard(toCard(comment));
			contentView.refresh();
		}
	}
	
	private CommentCard toCard(JSONObject comment) {
		String postAuthor = comment.optString("author");
		String postContent = comment.optString("content");
		String published = comment.optString("published");
		String avatarURL = AvatarUtils.avatarURL(getActivity(), postAuthor);
		
		CommentCard commentCard = new CommentCard(postAuthor, avatarURL, postContent, published);
		return commentCard;
	}
}
