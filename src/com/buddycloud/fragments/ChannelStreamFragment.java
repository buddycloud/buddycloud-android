package com.buddycloud.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
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
import com.buddycloud.GenericChannelActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.card.CardListAdapter;
import com.buddycloud.card.PostCard;
import com.buddycloud.fragments.adapter.SimilarChannelsAdapter;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.JSONUtils;
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
						syncd(null, getActivity());
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
		
		syncd(view, getActivity());
		
		return view;
	}
	
	private boolean isComment(JSONObject item) {
		return item.has("replyTo");
	}
	
	public void syncd(View view, Context context) {
		
		if (view == null) {
			view = getView();
		}
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.GONE);
		ListView contentView = (ListView) view.findViewById(R.id.postsStream);
		final CardListAdapter cardAdapter = new CardListAdapter();
		contentView.setAdapter(cardAdapter);
		
		PostsModel.getInstance().fill(getActivity(), new ModelCallback<Void>() {

			@Override
			public void success(Void voidd) {
				JSONArray response = PostsModel.getInstance().getFromCache(getActivity(), channelJid);
				
				Map<String, Integer> commentsPerItem = new HashMap<String, Integer>();
				for (int i = 0; i < response.length(); i++) {
					JSONObject post = response.optJSONObject(i);
					if (isComment(post)) {
						String topicId = post.optString("replyTo");
						Integer replyCount = commentsPerItem.get(topicId);
						commentsPerItem.put(topicId, replyCount == null ? 1 : replyCount + 1);
					}
				}
				
				for (int i = 0; i < response.length(); i++) {
					JSONObject post = response.optJSONObject(i);
					Integer commentCount = commentsPerItem.get(post.optString("id"));
					try {
						post.put("replyCount", commentCount == null ? 0 : commentCount);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					if (!isComment(post)) {
						PostCard card = toCard(post, channelJid);
						cardAdapter.addCard(card);
					}
				}
				
				cardAdapter.notifyDataSetChanged();
			}

			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
				
			}
		}, channelJid);
	}
	
	private PostCard toCard(JSONObject post, final String channelJid) {
		final String postId = post.optString("id");
		String postAuthor = post.optString("author");
		String postContent = post.optString("content");
		String published = post.optString("published");
		
		String avatarURL = AvatarUtils.avatarURL(getActivity(), postAuthor);
		
		Integer commentCount = post.optInt("replyCount");
		
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
		
		MenuItem followItem = menu.getItem(0);
		if (isFollowing()) {
			followItem.setTitle(R.string.menu_unfollow);
		} else {
			followItem.setTitle(R.string.menu_follow);
		}
	}

	private boolean isFollowing() {
		JSONArray subscribed = SubscribedChannelsModel.getInstance().getFromCache(getActivity());
		return JSONUtils.contains(subscribed, channelJid);
	}
	
	@Override
	public boolean menuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.menu_similar_channels) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), GenericChannelActivity.class);
			intent.putExtra(GenericChannelActivity.ADAPTER_NAME, 
					SimilarChannelsAdapter.ADAPTER_NAME);
			intent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
			getActivity().startActivityForResult(
					intent, GenericChannelActivity.REQUEST_CODE);
		} else if (item.getItemId() == R.id.menu_follow) {
			
			final boolean isFollowing = isFollowing();
			
			Map<String, String> subscription = new HashMap<String, String>();
			String newRole = isFollowing ? SubscribedChannelsModel.ROLE_NONE : SubscribedChannelsModel.ROLE_PRODUCER;
			subscription.put(channelJid + SubscribedChannelsModel.POST_NODE_SUFIX, newRole);
			
			SubscribedChannelsModel.getInstance().save(getActivity(), new JSONObject(subscription), 
					new ModelCallback<JSONObject>() {
						@Override
						public void success(JSONObject response) {
							Toast.makeText(getActivity(),  
									getString(isFollowing ? R.string.action_unfollowed : R.string.action_followed, channelJid), 
									Toast.LENGTH_LONG).show();
							getSherlockActivity().supportInvalidateOptionsMenu();
						}

						@Override
						public void error(Throwable throwable) {
							Toast.makeText(getActivity(), 
									getString(R.string.action_follow_failed, channelJid),
									Toast.LENGTH_LONG).show();
						}
					});
		}
		return false;
	}
}
