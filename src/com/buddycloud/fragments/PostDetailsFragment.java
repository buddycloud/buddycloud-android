package com.buddycloud.fragments;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.R;
import com.buddycloud.card.CardListAdapter;
import com.buddycloud.card.CommentCard;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.squareup.picasso.Picasso;

public class PostDetailsFragment extends ContentFragment {

	public static final String POST_ID = "com.buddycloud.POST_ID";
	private CardListAdapter commentAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_post_details, container, false);
		final String postId = getArguments().getString(POST_ID);
		final String channelJid = getArguments().getString(GenericChannelsFragment.CHANNEL);
		
		updateView(view, postId, channelJid);
		
		final ImageView postButton = (ImageView) view.findViewById(R.id.postButton);
		postButton.setEnabled(false);
		final EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
		
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (!postButton.isEnabled()) {
					return;
				}
				
				JSONObject post = createPost(postContent);
				
				PostsModel.getInstance().save(getActivity(), post, new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Toast.makeText(getActivity().getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
						postContent.setText("");
						sync(channelJid, postId);
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
		
		ChannelStreamFragment.configurePostSection(postContent, postButton);
		
		return view;
	}

	private void updateView(View view, final String postId, final String channelJid) {
		JSONObject post = PostsModel.getInstance().postWithId(postId, channelJid);
		if (post == null) {
			return;
		}
		
		if (view == null) {
			view = getView();
		}
		
		((TextView) view.findViewById(R.id.title)).setText(post.optString("author"));
		
		String authorAvatarURL = AvatarUtils.avatarURL(getActivity(), post.optString("author"));
		ImageView authorAvatarView = (ImageView) view.findViewById(R.id.bcProfilePic);
		Picasso.with(getActivity()).load(authorAvatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(authorAvatarView);
		
		((TextView) view.findViewById(R.id.bcPostContent)).setText(post.optString("content"));
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		ImageView avatarView = (ImageView) view.findViewById(R.id.bcCommentPic);
		Picasso.with(getActivity()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
		
		
		ListView commentList = (ListView) view.findViewById(R.id.postsStream);
		this.commentAdapter = new CardListAdapter();
		commentList.setAdapter(commentAdapter);
		
		loadComments(view, postId);
	}

	public void sync(final String channelJid, final String postId) {
		commentAdapter.clear();
		
		final View progress = getView().findViewById(R.id.subscribedProgress);
		progress.setVisibility(View.VISIBLE);
		
		SyncModel.getInstance().refresh(getActivity(), new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
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
		List<JSONObject> comments = PostsModel.getInstance().cachedCommentsFromPost(postId);
		for (JSONObject comment : comments) {
			commentAdapter.addCard(toCard(comment));
			commentAdapter.notifyDataSetChanged();
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

	@Override
	public void syncd(Context context) {
		final String postId = getArguments().getString(POST_ID);
		final String channelJid = getArguments().getString(GenericChannelsFragment.CHANNEL);
		updateView(getView(), postId, channelJid);
	}

	@Override
	public void attached() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createOptions(Menu menu) {
		getSherlockActivity().getSupportMenuInflater().inflate(
				R.menu.post_fragment_options, menu);
	}

	@Override
	public boolean menuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
}
