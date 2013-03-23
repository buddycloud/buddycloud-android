package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.buddycloud.card.PostCard;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.utils.AvatarUtils;
import com.fima.cardsui.objects.CardStack;
import com.fima.cardsui.views.CardUI;

public class ChannelStreamAdapter extends BaseAdapter {

	private final Activity parent;
	private final String channelJid;

	
	public ChannelStreamAdapter(Activity parent, String channelJid) {
		this.parent = parent;
		this.channelJid = channelJid;
		fetchPosts();
	}
	
	public void fetchPosts() {
		PostsModel.getInstance().refresh(parent, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				System.err.println(throwable);
				
			}
		}, channelJid);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return PostsModel.getInstance().get(parent, channelJid).length();
	}

	@Override
	public Object getItem(int arg0) {
		return PostsModel.getInstance().get(parent, channelJid);
	}

	@Override
	public long getItemId(int arg0) {
		return PostsModel.getInstance().get(parent, channelJid).hashCode();
	}

	@Override
	public View getView(int position, View arg1, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
		View retView = inflater.inflate(R.layout.posts_stream, viewGroup, false);
		
		CardUI postStream = (CardUI) retView.findViewById(R.id.postsStream);
		
		JSONObject post = PostsModel.getInstance().postsFromChannel(parent, channelJid).optJSONObject(position);
		
		CardStack stack = new CardStack();
		stack.add(toCard(post));
		
//		JSONArray comments = PostsModel.getInstance().commentsFromPost(post.optString("id"));
//		for (int i = 0; i < comments.length(); i++) {
//			JSONObject comment = comments.optJSONObject(i);
//			stack.add(toCard(comment));
//		}
		
		postStream.addStack(stack);
		postStream.refresh();
		
        return retView;
	}

	private PostCard toCard(JSONObject post) {
		String postAuthor = post.optString("author");
		String postContent = post.optString("content");
		String avatarURL = AvatarUtils.avatarURL(parent, postAuthor);
		
		PostCard postCard = new PostCard(postAuthor, avatarURL, postContent);
		postCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO: Load single post view
			}
		});
		return postCard;
	}
}
