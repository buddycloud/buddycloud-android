package com.buddycloud;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;

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
		View retView = inflater.inflate(R.layout.post_entry, viewGroup, false);

		JSONObject post = PostsModel.getInstance().get(parent, channelJid).optJSONObject(position);

		TextView postContent = (TextView) retView.findViewById(R.id.bcPostContent);
		postContent.setText(post.optString("content"));
		
        return retView;
	}
}
