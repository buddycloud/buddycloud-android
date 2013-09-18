package com.buddycloud.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.buddycloud.ChannelDetailActivity;
import com.buddycloud.GenericChannelActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.card.Card;
import com.buddycloud.card.CardListAdapter;
import com.buddycloud.card.PostCard;
import com.buddycloud.fragments.adapter.FollowersAdapter;
import com.buddycloud.fragments.adapter.SimilarChannelsAdapter;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.InputUtils;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ChannelStreamFragment extends ContentFragment {

	private CardListAdapter cardAdapter;
	private EndlessScrollListener scrollListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_channel_stream, container, false);
		
		showProgress(view);
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		ImageView avatarView = (ImageView) view.findViewById(R.id.bcCommentPic);
		ImageHelper.picasso(getActivity()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
		
		final ImageView postButton = (ImageView) view.findViewById(R.id.postButton);
		postButton.setEnabled(false);
		if (SubscribedChannelsModel.canPost(getRole())) {
			EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
			postButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					createPost(view);
				}
			});
			configurePostSection(postContent, postButton);
		} else {
			view.findViewById(R.id.bcCommentSection).setVisibility(View.GONE);
		}
		
		syncd(view, getActivity());
		
		return view;
	}
	
	private String getChannelJid() {
		return getArguments().getString(GenericChannelsFragment.CHANNEL);
	}
	
	private void syncd(View view, Context context) {
		
		ListView contentView = (ListView) view.findViewById(R.id.postsStream);
		if (cardAdapter == null) {
			this.cardAdapter = new CardListAdapter();
			cardAdapter.setFragment(this);
			contentView.setAdapter(cardAdapter);
			this.scrollListener = new EndlessScrollListener(this);
			contentView.setOnScrollListener(scrollListener);
		} else {
			cardAdapter.clear();
			cardAdapter.notifyDataSetChanged();
		}
		
		fillMore();
	}
	
	public void scrollUp() {
		ListView postsStream = (ListView) getView().findViewById(R.id.postsStream);
		postsStream.smoothScrollToPosition(0);
	}

	protected void fillMore() {
		
		scrollListener.setLoading(true);
		String lastUpdated = null;
		String lastId = null;
		
		if (cardAdapter.getCount() > 0) {
			Card lastCard = (Card) cardAdapter.getItem(cardAdapter.getCount() - 1);
			lastUpdated = lastCard.getPost().optString("updated");
			lastId = lastCard.getPost().optString("id");
		}
		
		fillAdapter(getActivity(), lastUpdated, 
				fillAdapterCallback(lastUpdated, lastId));
	}

	private ModelCallback<Boolean> fillAdapterCallback(final String lastUpdated,
			final String lastId) {
		return smartify(new ModelCallback<Boolean>() {
			@Override
			public void success(Boolean response) {
				fillRemotely(lastId, lastUpdated);
			}

			@Override
			public void error(Throwable throwable) {}
		});
	}

	private void fillRemotely(final String lastId, final String lastUpdated) {
		PostsModel.getInstance().fillMore(getActivity(), smartify(new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				fillAdapter(getActivity(), lastUpdated, smartify(new ModelCallback<Boolean>() {
					@Override
					public void success(Boolean response) {
						if (response) {
							scrollListener.setLoading(false);
						}
						hideProgress(getView());
					}

					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getActivity(), 
								getString(R.string.message_post_fetch_failed), 
								Toast.LENGTH_LONG).show();
						scrollListener.setLoading(false);
						hideProgress(getView());
					}
				}));
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getActivity(), 
						getString(R.string.message_post_fetch_failed), 
						Toast.LENGTH_LONG).show();
				scrollListener.setLoading(false);
				hideProgress(getView());
			}
		}), getChannelJid(), lastId);
	}
	
	private void fillAdapter(final Context context, final String lastUpdated, 
			final ModelCallback<Boolean> callback) {
		new AsyncTask<Void, Void, JSONArray>() {

			@Override
			protected JSONArray doInBackground(Void... params) {
				return PostsModel.getInstance().getFromCache(context, getChannelJid(), lastUpdated);
			}
			
			@Override
			protected void onPostExecute(JSONArray allPosts) {
				if (isDetached()) {
					return;
				}
				for (int i = 0; i < allPosts.length(); i++) {
					JSONObject post = allPosts.optJSONObject(i);
					PostCard card = toCard(post, getChannelJid());
					cardAdapter.addCard(card);
				}
				cardAdapter.sort();
				if (callback != null) {
					callback.success(allPosts.length() > 0);
				}
			}
			
		}.execute();
	}
	
	private PostCard toCard(JSONObject post, final String channelJid) {
		return new PostCard(channelJid, post, (MainActivity) getActivity(), 
				cardAdapter, getRole());
	}
	
	private void createPost(final View view) {
		
		final ImageView postButton = (ImageView) view.findViewById(R.id.postButton);
		
		if (!postButton.isEnabled()) {
			return;
		}
		
		showProgress(view);
		
		final EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
		
		JSONObject post = createJSONPost(postContent);
		postContent.setText("");
		InputUtils.hideKeyboard(getActivity(), postContent);
		
		PostsModel.getInstance().save(getActivity(), post, smartify(
				new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(getActivity().getApplicationContext(), 
						getString(R.string.message_post_created), Toast.LENGTH_LONG).show();
				fillRemotely(null, null);
				scrollUp();
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getActivity().getApplicationContext(), 
						throwable.getMessage(), Toast.LENGTH_LONG).show();
				hideProgress(view);
			}
		}), getChannelJid());
	}

	private JSONObject createJSONPost(final EditText postContent) {
		JSONObject post = new JSONObject();
		try {
			post.putOpt("content", postContent.getText().toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return post;
	}
	
	private void loadTitle(Activity activity) {
		SlidingFragmentActivity sherlockActivity = (SlidingFragmentActivity) activity;
		sherlockActivity.getSupportActionBar().setTitle(getChannelJid());
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
				boolean enabled = arg0 != null && arg0.length() > 0;
				postButton.setEnabled(enabled);
			}
			
		});
	}

	@Override
	public void attached(Activity activity) {
		loadTitle(activity);
	}

	@Override
	public void createOptions(Menu menu) {
		if (isDetached()) {
			return;
		}
		getSherlockActivity().getSupportMenuInflater().inflate(
				R.menu.channel_fragment_options, menu);
		
		MenuItem followItem = menu.findItem(R.id.menu_follow);
		if (SubscribedChannelsModel.isFollowing(getRole())) {
			followItem.setTitle(R.string.menu_unfollow);
		} else {
			followItem.setTitle(R.string.menu_follow);
		}
	}

	private String getRole() {
		JSONObject subscribed = SubscribedChannelsModel.getInstance().getFromCache(getActivity());
		if (!subscribed.has(getChannelJid())) {
			return null;
		}
		return subscribed.optString(getChannelJid());
	}
	
	@Override
	public boolean menuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_similar_channels:
			showSimilarChannels();
			return true;
		case R.id.menu_follow:
			toogleRole();
			return true;
		case R.id.menu_channel_followers:
			showFollowers();
			return true;
		case R.id.menu_channel_details:
			showDetails();
			return true;
		default:
			return false;
		}
	}

	private void showDetails() {
		Intent intent = new Intent();
		intent.setClass(getActivity(), ChannelDetailActivity.class);
		intent.putExtra(GenericChannelsFragment.CHANNEL, getChannelJid());
		intent.putExtra(SubscribedChannelsModel.ROLE, getRole());
		getActivity().startActivity(intent);
	}

	private void toogleRole() {
		final boolean isFollowing = SubscribedChannelsModel.isFollowing(getRole());
		
		Map<String, String> subscription = new HashMap<String, String>();
		String newRole = isFollowing ? SubscribedChannelsModel.ROLE_NONE : SubscribedChannelsModel.ROLE_PUBLISHER;
		final String channelJid = getChannelJid();
		subscription.put(channelJid + SubscribedChannelsModel.POST_NODE_SUFIX, newRole);
		
		SubscribedChannelsModel.getInstance().save(getActivity(), new JSONObject(subscription), 
				smartify(new ModelCallback<JSONObject>() {
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
				}));
	}

	private void showFollowers() {
		showGenericChannelActivity(FollowersAdapter.ADAPTER_NAME);
	}
	
	private void showSimilarChannels() {
		showGenericChannelActivity(SimilarChannelsAdapter.ADAPTER_NAME);
	}
	
	private void showProgress(View container) {
		container.findViewById(R.id.channelStreamProgress).setVisibility(View.VISIBLE);
	}
	
	private void hideProgress(View container) {
		container.findViewById(R.id.channelStreamProgress).setVisibility(View.GONE);
	}
	
	private void showGenericChannelActivity(String adapterName) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), GenericChannelActivity.class);
		intent.putExtra(GenericChannelActivity.ADAPTER_NAME, 
				adapterName);
		intent.putExtra(GenericChannelsFragment.CHANNEL, getChannelJid());
		intent.putExtra(SubscribedChannelsModel.ROLE, getRole());
		getActivity().startActivityForResult(
				intent, GenericChannelActivity.REQUEST_CODE);
	}
}
