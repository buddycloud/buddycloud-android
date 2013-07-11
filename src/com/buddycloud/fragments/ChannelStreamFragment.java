package com.buddycloud.fragments;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;
import android.app.Activity;
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
import com.buddycloud.ChannelDetailActivity;
import com.buddycloud.GenericChannelActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
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
import com.buddycloud.utils.JSONUtils;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class ChannelStreamFragment extends ContentFragment implements OnRefreshListener {

	private CardListAdapter cardAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_channel_stream, container, false);
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.VISIBLE);
		
		String myChannelJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
		String avatarURL = AvatarUtils.avatarURL(getActivity(), myChannelJid);
		ImageView avatarView = (ImageView) view.findViewById(R.id.bcCommentPic);
		ImageHelper.picasso(getActivity()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
		
		final ImageView postButton = (ImageView) view.findViewById(R.id.postButton);
		postButton.setEnabled(false);
		
		EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
		
		postButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createPost(view);
			}
		});
		
		configurePostSection(postContent, postButton);
		
		syncd(view, getActivity());
		
		getPullToRefreshAttacher().setRefreshableView((ListView) view.findViewById(R.id.postsStream), this);
		
		return view;
	}

	private PullToRefreshAttacher getPullToRefreshAttacher() {
		MainActivity activity = (MainActivity) getActivity();
        PullToRefreshAttacher attacher = activity.getPullToRefreshAttacher();
		return attacher;
	}

	private String getChannelJid() {
		return getArguments().getString(GenericChannelsFragment.CHANNEL);
	}
	
	private void syncd(View view, Context context) {
		
		if (view == null) {
			view = getView();
		}
		
		view.findViewById(R.id.subscribedProgress).setVisibility(View.GONE);
		ListView contentView = (ListView) view.findViewById(R.id.postsStream);
		this.cardAdapter = new CardListAdapter();
		contentView.setAdapter(cardAdapter);
		fillAdapter(context);
		contentView.setOnScrollListener(new EndlessScrollListener(this));
		
		PostsModel.getInstance().fill(getActivity(), new ModelCallback<Void>() {
			@Override
			public void success(Void voidd) {
				fillAdapter(getActivity());
			}

			@Override
			public void error(Throwable throwable) {
				
			}
		}, getChannelJid());
	}

	protected void fillMore() {
		PostsModel.getInstance().fillMore(getActivity(), new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				fillAdapter(getActivity());
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		}, getChannelJid());
	}
	
	private void fillAdapter(Context context) {
		String channelJid = getChannelJid();
		JSONArray allPosts = PostsModel.getInstance().getFromCache(context, channelJid);
		for (int i = 0; i < allPosts.length(); i++) {
			JSONObject post = allPosts.optJSONObject(i);
			PostCard card = toCard(post, channelJid);
			cardAdapter.addCard(card);
			cardAdapter.notifyDataSetChanged();
		}
	}
	
	private PostCard toCard(JSONObject post, final String channelJid) {
		PostCard postCard = new PostCard(channelJid, post, (MainActivity) getActivity());
		postCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO Show single post fragment
			}
		});
		return postCard;
	}
	
	private void createPost(final View view) {
		
		final ImageView postButton = (ImageView) view.findViewById(R.id.postButton);
		
		if (!postButton.isEnabled()) {
			return;
		}
		
		final EditText postContent = (EditText) view.findViewById(R.id.postContentTxt);
		
		JSONObject post = createJSONPost(postContent);
		postContent.setText("");
		InputUtils.hideKeyboard(getActivity(), postContent);
		
		PostsModel.getInstance().save(getActivity(), post, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(getActivity().getApplicationContext(), "Post created", Toast.LENGTH_LONG).show();
				syncd(null, getActivity());
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getActivity().getApplicationContext(), throwable.getMessage(), Toast.LENGTH_LONG).show();
			}
		}, getChannelJid());
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
		return JSONUtils.contains(subscribed, getChannelJid());
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
		getActivity().startActivity(intent);
	}

	private void toogleRole() {
		final boolean isFollowing = isFollowing();
		
		Map<String, String> subscription = new HashMap<String, String>();
		String newRole = isFollowing ? SubscribedChannelsModel.ROLE_NONE : SubscribedChannelsModel.ROLE_PRODUCER;
		final String channelJid = getChannelJid();
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

	private void showFollowers() {
		showGenericChannelActivity(FollowersAdapter.ADAPTER_NAME);
	}
	
	private void showSimilarChannels() {
		showGenericChannelActivity(SimilarChannelsAdapter.ADAPTER_NAME);
	}
	
	private void showGenericChannelActivity(String adapterName) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), GenericChannelActivity.class);
		intent.putExtra(GenericChannelActivity.ADAPTER_NAME, 
				adapterName);
		intent.putExtra(GenericChannelsFragment.CHANNEL, getChannelJid());
		getActivity().startActivityForResult(
				intent, GenericChannelActivity.REQUEST_CODE);
	}

	@Override
    public void onRefreshStarted(View view) {
		PostsModel.getInstance().fill(getActivity(), new ModelCallback<Void>() {
			@Override
			public void success(Void voidd) {
				fillAdapter(getActivity());
				getPullToRefreshAttacher().setRefreshComplete();
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getActivity(), 
						getString(R.string.pull_to_refresh_error), Toast.LENGTH_LONG).show();
				getPullToRefreshAttacher().setRefreshComplete();
			}
		}, getChannelJid());
    }
}
