package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.buddycloud.card.CommentCard;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.PostsModel;
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
		JSONArray comments = PostsModel.getInstance().commentsFromPost(postId);

		((TextView) view.findViewById(R.id.title)).setText(post.optString("author"));
		
		((SmartImageView) view.findViewById(R.id.bcProfilePic)).setImageUrl(
				AvatarUtils.avatarURL(getActivity(), channelJid), R.drawable.personal_50px);
		
		((TextView) view.findViewById(R.id.bcPostContent)).setText(post.optString("content"));
		
		String avatarURL = AvatarUtils.avatarURL(getActivity(), post.optString("author"));
		SmartImageView avatarView = (SmartImageView) view.findViewById(R.id.bcCommentPic);
		avatarView.setImageUrl(avatarURL, R.drawable.personal_50px);
		
		for (int i = 0; i < comments.length(); i++) {
			JSONObject comment = comments.optJSONObject(i);
			CardUI contentView = (CardUI) view.findViewById(R.id.postsStream);
			contentView.addCard(toCard(comment));
			contentView.refresh();
		}
		
		return view;
	}
	
	private CommentCard toCard(JSONObject comment) {
		String postAuthor = comment.optString("author");
		String postContent = comment.optString("content");
		String avatarURL = AvatarUtils.avatarURL(getActivity(), postAuthor);
		
		CommentCard commentCard = new CommentCard(postAuthor, avatarURL, postContent);
		return commentCard;
	}
}
