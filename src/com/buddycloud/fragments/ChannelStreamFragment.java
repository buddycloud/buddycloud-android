package com.buddycloud.fragments;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.ChannelDetailActivity;
import com.buddycloud.FullScreenImageActivity;
import com.buddycloud.GenericChannelActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.PostNewTopicActivity;
import com.buddycloud.R;
import com.buddycloud.card.Card;
import com.buddycloud.card.CardListAdapter;
import com.buddycloud.card.PostCard;
import com.buddycloud.fragments.adapter.FollowersAdapter;
import com.buddycloud.fragments.adapter.PendingSubscriptionsAdapter;
import com.buddycloud.fragments.adapter.SimilarChannelsAdapter;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.ModelListener;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.TopicChannelModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.InputUtils;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.software.shell.fab.FloatingActionButton;

public class ChannelStreamFragment extends ContentFragment implements ModelListener {

	private FloatingActionButton addPostTopicBtn;
	private SwipeRefreshLayout postStreamRefreshLayout;
	private ListView postStream;
	private CardListAdapter cardAdapter;
	private EndlessScrollListener scrollListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_channel_stream, container, false);		
		showProgress(view);

		postStream = (ListView) view.findViewById(R.id.postsStream);
		postStream.setEmptyView(getEmptyListView(view));
		postStreamRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
		postStreamRefreshLayout.setColorSchemeResources(R.color.bc_green_blue_color);
		postStreamRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				postStreamRefreshLayout.setRefreshing(true);
				scrollListener.setRefreshing(true);

				// refresh the post stream
				synchPostStream();
			}
		});
		
		addPostTopicBtn = (FloatingActionButton) view.findViewById(R.id.addNewTopicBtn);
		addPostTopicBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				Intent intent = new Intent();
				intent.setClass(getActivity(), PostNewTopicActivity.class);
				intent.putExtra(GenericChannelsFragment.CHANNEL, getChannelJid());
				getActivity().startActivityForResult(intent, PostNewTopicActivity.REQUEST_CODE);
			}
		});
		
		if (!SubscribedChannelsModel.canPost(getRole())) {
			addPostTopicBtn.setVisibility(View.GONE);
		} else {
			addPostTopicBtn.setVisibility(View.VISIBLE);
		}
				
		PostsModel.getInstance().setListener(this);
		synchChannelMetadata(); 
		synchPostStream();
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ImageLoader.getInstance().resume();
	}

	@Override
	public void onStop() {
		super.onStop();
		ImageLoader.getInstance().stop();
	}

	public void showAddPostTopicBtn() {
		if (addPostTopicBtn != null) {
			addPostTopicBtn.show();
		}
	}
	
	public void hideAddPostTopicBtn() {
		if (addPostTopicBtn != null) {
			addPostTopicBtn.hide();
		}
	}
	
	public ListView getPostStreamView() {
		return postStream;
	}
	
	public SwipeRefreshLayout getSwipeRefreshLayout() {
		return postStreamRefreshLayout;
	}
	
	public CardListAdapter getPostStreamAdapter() {
		return cardAdapter;
	}

	private String getChannelJid() {
		return getArguments().getString(GenericChannelsFragment.CHANNEL);
	}
	
	private View getEmptyListView(View view) {
		return (view != null) ? view.findViewById(android.R.id.empty) : null;
	}
	
	/**
	 * Synchronize the channel metadata
	 * 
	 */
	private void synchChannelMetadata() {
		ChannelMetadataModel.getInstance().fill(getActivity(), new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				getSherlockActivity().supportInvalidateOptionsMenu();
			}
			@Override
			public void error(Throwable throwable) {}
		}, getChannelJid());
	}
	
	/**
	 * Synchronize the post stream with latest posts
	 * from remote
	 * 
	 * @param view
	 * @param context
	 */
	public void synchPostStream() {
		
		if (cardAdapter == null) {
			this.cardAdapter = new CardListAdapter();
			cardAdapter.setFragment(this);
			postStream.setAdapter(cardAdapter);
			this.scrollListener = new EndlessScrollListener(this);
			postStream.setOnScrollListener(scrollListener);
		} 
		
		// get the latest post from remote
		fillRemotely(null, null);
	}
	
	protected void fillMore() {
		scrollListener.setLoading(true);
		String lastUpdated = null;
		String lastId = null;
		
		JSONObject lastPost = getLastPost();
		if (lastPost != null) {
			lastUpdated = lastPost.optString("updated");
			lastId = lastPost.optString("id");
		}
		
		fillAdapter(getActivity(), lastUpdated, 
				fillAdapterCallback(lastUpdated, lastId));
	}
	
	private JSONObject getLastPost() {
		JSONObject post = null;
		for (int i = cardAdapter.getCount() - 1; i >= 0; i--) {
			Card lastCard = (Card) cardAdapter.getItem(i);
			if (!PostsModel.isPending(lastCard.getPost())) {
				post = lastCard.getPost();
				break;
			}
		}
		return post;
	}

	public void scrollUp() {
		final ListView postsStream = (ListView) getView().findViewById(R.id.postsStream);
		postsStream.post(new Runnable() {
	        @Override
	        public void run() {
	        	postsStream.setSelection(0);
	        	postsStream.smoothScrollToPosition(0);
	        }
	    });
	}

	/**
	 * A post card adapter callback
	 * 
	 * @param lastUpdated
	 * @param lastId
	 * @return
	 */
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

	/**
	 * Get the posts from the local cache w.r.t last updated
	 * timestamp.
	 * 
	 * @param context
	 * @param lastUpdated
	 * @param callback
	 */
	private void fillAdapter(final Context context, final String lastUpdated, 
			final ModelCallback<Boolean> callback) {
		if (!isAttachedToActivity()) {
			return;
		}
		new AsyncTask<Void, Void, List<PostCard>>() {

			@Override
			protected List<PostCard> doInBackground(Void... params) {
				JSONArray cachedPosts = PostsModel.getInstance().getFromCache(context, 
						getChannelJid(), lastUpdated);
				List<PostCard> posts = new LinkedList<PostCard>();
				for (int i = 0; i < cachedPosts.length(); i++) {
					JSONObject post = cachedPosts.optJSONObject(i);
					PostCard card = toCard(post, getChannelJid());
					posts.add(card);
				}
				return posts;
			}
			
			protected void onPostExecute(List<PostCard> cachedPosts) {
				for (PostCard card : cachedPosts) {
					cardAdapter.addCard(card);
				}
				cardAdapter.sort();
				if (callback != null) {
					callback.success(cachedPosts.size() > 0);
				}
			};
			
		}.execute();
	}
	
	/**
	 * Get the latest posts from the remote w.r.t last
	 * post id and last updated timestamp.
	 * 
	 * @param lastId
	 * @param lastUpdated
	 */
	public void fillRemotely(final String lastId, final String lastUpdated) {
		PostsModel.getInstance().fillMore(getActivity(), smartify(new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				fillAdapter(getActivity(), lastUpdated, smartify(new ModelCallback<Boolean>() {
					@Override
					public void success(Boolean response) {
						if (response) {
							postStreamRefreshLayout.setRefreshing(false);
							scrollListener.setRefreshing(false);
							scrollListener.setLoading(false);
						}
						hideProgress(getView());
					}

					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getActivity(), 
								getString(R.string.message_post_fetch_failed), 
								Toast.LENGTH_LONG).show();
						postStreamRefreshLayout.setRefreshing(false);
						scrollListener.setRefreshing(false);
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
				postStreamRefreshLayout.setRefreshing(false);
				scrollListener.setRefreshing(false);
				scrollListener.setLoading(false);
				hideProgress(getView());
			}
		}), getChannelJid(), lastId);
	}

	private PostCard toCard(JSONObject post, final String channelJid) {
		return new PostCard(channelJid, post, this, getRole());
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
		if (!isAttachedToActivity()) {
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
		
		MenuItem pendingSubscriptionsItem = menu.findItem(R.id.menu_pending_subscriptions);
		boolean canApprove = SubscribedChannelsModel.canChangeAffiliation(getRole());
		pendingSubscriptionsItem.setVisible(canApprove);
		
		MenuItem deleteChannelItem = menu.findItem(R.id.menu_delete_channel);
		JSONObject metadata = ChannelMetadataModel.getInstance().getFromCache(getActivity(), getChannelJid());
		
		boolean isPersonal = metadata == null || metadata.optString("channelType").equals("personal");
		boolean canDelete = SubscribedChannelsModel.canDeleteChannel(getRole());
		deleteChannelItem.setVisible(canDelete && !isPersonal);
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
		case R.id.menu_pending_subscriptions:
			showPendingSubscriptions();
			return true;
		case R.id.menu_delete_channel:
			confirmDeleteChannel();
			return true;
		default:
			return false;
		}
	}

	private void confirmDeleteChannel() {
		Context context = getActivity();
		new AlertDialog.Builder(context)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(context.getString(R.string.title_confirm_delete_channel))
			.setMessage(context.getString(R.string.message_confirm_delete_channel))
			.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							deleteChannel();
						}
					}).setNegativeButton(R.string.no, null).show();	
	}
	
	private void deleteChannel() {
		TopicChannelModel.getInstance().delete(getActivity(), new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				Toast.makeText(getActivity(),  
						getString(R.string.message_channel_deleted), 
						Toast.LENGTH_LONG).show();
				MainActivity mainActivity = (MainActivity) getSherlockActivity();
				String myJid = (String) Preferences.getPreference(getActivity(), Preferences.MY_CHANNEL_JID);
				mainActivity.showChannelFragment(myJid);
				mainActivity.showMenu();
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(getActivity(),  
						getString(R.string.message_channel_deletion_failed), 
						Toast.LENGTH_LONG).show();

			}
		}, getChannelJid());
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
		String newRole = isFollowing ? SubscribedChannelsModel.SUBSCRIPTION_NONE : SubscribedChannelsModel.ROLE_PUBLISHER;
		final String channelJid = getChannelJid();
		subscription.put(channelJid + SubscribedChannelsModel.POST_NODE_SUFIX, newRole);
		
		SubscribedChannelsModel.getInstance().save(getActivity(), new JSONObject(subscription), 
				smartify(new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Toast.makeText(getActivity(),  
								getString(isFollowing ? R.string.action_unfollowed : R.string.action_followed, channelJid), 
								Toast.LENGTH_LONG).show();
						((MainActivity) getActivity()).showChannelFragment(channelJid, true, true);
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
	
	private void showPendingSubscriptions() {
		showGenericChannelActivity(PendingSubscriptionsAdapter.ADAPTER_NAME);
	}
	
	private void showSimilarChannels() {
		showGenericChannelActivity(SimilarChannelsAdapter.ADAPTER_NAME);
	}
	
	private void showProgress(View container) {
		SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
	    activity.setProgressBarIndeterminateVisibility(true);
	}
	
	private void hideProgress(View container) {
		SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
	    activity.setProgressBarIndeterminateVisibility(false);
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

	@Override
	public void dataChanged() {
		if (!isAttachedToActivity()) {
			return;
		}
		showProgress(getView());
		fillRemotely(null, null);
	}

	@Override
	public void itemRemoved(String channelJid, String itemId, String parentId) {
		if (!channelJid.equals(getChannelJid()) || !isAttachedToActivity()) {
			return;
		}
		CardListAdapter adapter = getAdapter(itemId, parentId);
		if (adapter != null) {
			adapter.remove(itemId);
			adapter.notifyDataSetChanged();
		}
	}
	
	private CardListAdapter getAdapter(String itemId, String parentId) {
		Card card = cardAdapter.getCard(itemId);
		if (card != null) {
			return cardAdapter;
		} else if (parentId != null) {
			PostCard parentCard = (PostCard) cardAdapter.getCard(parentId);
			if (parentCard == null) {
				return null;
			}
			return parentCard.getRepliesAdapter();
		}
		return null;
	}

	@Override
	public void pendingItemAdded(String channelJid, JSONObject pendingItem) {
		if (!channelJid.equals(getChannelJid()) || !isAttachedToActivity()) {
			return;
		}
		String replyTo = pendingItem.optString("replyTo", null);
		if (replyTo == null) {
			PostCard card = toCard(pendingItem, getChannelJid());
			cardAdapter.addCard(card);
			cardAdapter.sort();
		} else {
			PostCard parentCard = (PostCard) cardAdapter.getCard(replyTo);
			if (parentCard != null) {
				parentCard.addPendingCard(pendingItem);
			}
		}
		InputUtils.hideKeyboard(getActivity());
		scrollUp();
	}
}
