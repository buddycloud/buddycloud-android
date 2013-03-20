package com.buddycloud;

import com.buddycloud.image.SmartImageView;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.preferences.Preferences;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ChannelStreamFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_channel, container, false);
		
		// Stream
		ListView contentView = (ListView) view.findViewById(R.id.contentListView);
		contentView.setEmptyView(view.findViewById(R.id.subscribedProgress));

		String channelJid = getArguments().getString(SubscribedChannelsFragment.CHANNEL);
		
		// Set adapter
		final ChannelStreamAdapter streamAdapter = new ChannelStreamAdapter(getActivity(), channelJid);
		contentView.setAdapter(streamAdapter);
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = ChannelMetadataModel.getInstance().avatarURL(getActivity(), myChannelJid);
		SmartImageView avatarView = (SmartImageView) view.findViewById(R.id.bcProfilePic);
		avatarView.setImageUrl(avatarURL, R.drawable.personal_50px);
		
		/*Button postButton = (Button) findViewById(R.id.postButton);
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText postContent = (EditText) findViewById(R.id.postContentTxt);
				
				String apiAddress = Preferences.getPreference(ChannelStreamActivity.this, 
						Preferences.API_ADDRESS);
				
				String url = apiAddress + "/" + channel.getJid() + "/content/posts";
				StringEntity requestEntity;
				try {
					requestEntity = new StringEntity(
							"{\"content\": \"" + postContent.getText().toString() + "\"}",
						    "UTF-8");
					requestEntity.setContentType("application/json");
//					BuddycloudHTTPHelper.post(url, true, requestEntity, ChannelStreamActivity.this);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				Toast.makeText(getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
				postContent.setText("");
				streamAdapter.refetchPosts();
			}
		});*/
		
		return view;
	}
	
	
//	private void loadTitle(String channelJid) {
//		TextView channelTitleView = (TextView) findViewById(R.id.channelTitle);
//		channelTitleView.setText(channelJid);
//	}
}
