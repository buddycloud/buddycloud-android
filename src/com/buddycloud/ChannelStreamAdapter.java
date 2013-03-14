package com.buddycloud;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.http.ProfilePicCache;
import com.buddycloud.model.Channel;
import com.buddycloud.model.Post;
import com.buddycloud.preferences.Preferences;

public class ChannelStreamAdapter extends BaseAdapter {

	private static final double AVATAR_DIP = 75.;
	
	private List<Post> posts = new ArrayList<Post>();
	private final Activity parent;
	private final Channel channel;
	
	public ChannelStreamAdapter(Activity parent, Channel channel) {
		this.parent = parent;
		this.channel = channel;
		refetchPosts();
	}

	public void refetchPosts() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				fetchPosts();
				return null;
			}
			
		}.execute();
	}
	
	private void fetchPosts() {
		notifyCleared();
		
		String apiAddress = Preferences.getPreference(parent, Preferences.API_ADDRESS);

//		JSONArray jsonArray = BuddycloudHTTPHelper.getArray(
//				apiAddress + "/" + channel.getJid() + "/content/posts?max=25", true, parent);
//		
//		for (int i = 0; i < jsonArray.length(); i++) {
//			try {
//				JSONObject postObject = (JSONObject) jsonArray.getJSONObject(i);
//				Post post = new Post(channel.getJid(), postObject.optString("id"));
//				post.setAuthorJid(postObject.optString("author"));
//				post.setContent(postObject.optString("content"));
////				post.setPublished(Preferences.ISO_8601.parse(postObject.optString("published")));
//				post.setInReplyTo(postObject.optString("replyTo"));
//				post.setAuthorAvatarURL(fetchAvatar(post.getAuthorJid(), apiAddress));
//				notifyChanged(post);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

	private void notifyCleared() {
		parent.findViewById(R.id.contentListView).post(new Runnable() {
			@Override
			public void run() {
				posts.clear();
				notifyDataSetChanged();
			}
		});
	}
	
	private void notifyChanged(final Post post) {
		parent.findViewById(R.id.contentListView).post(new Runnable() {
			@Override
			public void run() {
				posts.add(post);
				notifyDataSetChanged();
			}
		});
	}

	private String fetchAvatar(String channel, String apiAddress) {
		int avatarSize = (int) (AVATAR_DIP * parent.getResources().getDisplayMetrics().density + 0.5);
		String url = apiAddress + "/" + channel + "/media/avatar?maxheight=" + avatarSize;
		Bitmap avatar = ProfilePicCache.getInstance().getBitmap(url);
		if (avatar == null) {
			url = Preferences.FALLBACK_PERSONAL_AVATAR;
			if (channel.contains("@topics.buddycloud.org")) {
				url = Preferences.FALLBACK_TOPIC_AVATAR;
			}
			ProfilePicCache.getInstance().getBitmap(url);
		}
		return url;
	}
	
	@Override
	public int getCount() {
		return posts.size();
	}

	@Override
	public Object getItem(int arg0) {
		return posts.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
		View retView = inflater.inflate(R.layout.post_entry, viewGroup, false);

		Post post = posts.get(position);

		TextView userIdView = (TextView) retView.findViewById(R.id.fbUserId);
		userIdView.setText(post.getAuthorJid());
		
		ImageView avatarView = (ImageView) retView.findViewById(R.id.fbProfilePic);
		avatarView.setImageBitmap(ProfilePicCache.getInstance().getBitmap(post.getAuthorAvatarURL()));
		
		TextView descriptionView = (TextView) retView.findViewById(R.id.fbMessage);
		descriptionView.setText(post.getContent());
		
		if (post.getInReplyTo() != null && !post.getInReplyTo().equals("")) {
			retView.setBackgroundColor(
					viewGroup.getResources().getColor(R.color.bc_bg_grey));
		}
		
        return retView;
	}
}
