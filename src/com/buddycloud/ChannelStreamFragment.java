package com.buddycloud;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ChannelStreamFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.activity_channel, container, false);
		
		// Stream
		ListView contentView = (ListView) view.findViewById(R.id.posts);
		contentView.setEmptyView(view.findViewById(R.id.subscribedProgress));

		final String channelJid = getArguments().getString(SubscribedChannelsFragment.CHANNEL);
		
		loadTitle(channelJid);
		
		// Set adapter
		final ChannelStreamAdapter streamAdapter = new ChannelStreamAdapter(getActivity(), channelJid);
		contentView.setAdapter(streamAdapter);
		
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
						streamAdapter.fetchPosts();
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
	
	private void loadTitle(String channelJid) {
		SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
		activity.getSupportActionBar().setTitle(channelJid);
	}
}
