package com.buddycloud.fragments;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.CreateAccountActivity;
import com.buddycloud.GenericChannelActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.SearchActivity;
import com.buddycloud.SettingsActivity;
import com.buddycloud.customviews.TypefacedEditText;
import com.buddycloud.customviews.TypefacedTextView;
import com.buddycloud.fragments.adapter.FindFriendsAdapter;
import com.buddycloud.fragments.adapter.MostActiveChannelsAdapter;
import com.buddycloud.fragments.adapter.RecommendedChannelsAdapter;
import com.buddycloud.fragments.adapter.SubscribedChannelsAdapter;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.ModelListener;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.model.TopicChannelModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.ChannelAdapterHelper;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TypefacesUtil;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class SubscribedChannelsFragment extends ContentFragment implements ModelListener {

	private ExpandableListView channelListView;
	private SubscribedChannelsAdapter adapter = new SubscribedChannelsAdapter();
	
	private GenericChannelsFragment genericChannelFrag = new GenericChannelsFragment(adapter) {
		@Override
		public void channelSelected(JSONObject channelItem) {
			selectChannel(channelItem.optString("jid"));
		}
	};
	
	public SubscribedChannelsFragment() {
		TopicChannelModel.getInstance().setListener(this);
		SubscribedChannelsModel.getInstance().setListener(this);
		ModelListener notifyChangeListener = new ModelListener() {
			@Override
			public void dataChanged() {
				adapter.sort(getActivity());
				adapter.notifyDataSetChanged();
				
				// my personal channel header
				updateChannelPersonalHeader();
			}

			@Override
			public void itemRemoved(String channelJid, String itemId, String parentId) {
				
			}

			@Override
			public void pendingItemAdded(String channelJid, JSONObject pendingItem) {

			}
		};
		SyncModel.getInstance().setListener(notifyChangeListener);
		ChannelMetadataModel.getInstance().setListener(notifyChangeListener);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		adapter.load(getActivity());
		
		final String channelJid = getMyChannelJid(container.getContext());
		
		final View view = genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
		channelListView = (ExpandableListView) view.findViewById(R.id.channelListView);
		channelListView.addHeaderView(ChannelAdapterHelper.createChannelPersonalHeader(container.getContext(), null, container, channelJid));
		
		// update personal channel counters
		updateChannelPersonalHeader();
		
		return view;
	}

	/**
	 * Update the personal channel header information
	 * such as (@mention, @replies, @views)
	 */
	private void updateChannelPersonalHeader() {
		
		if (channelListView == null)
			return ;
		
		final String channelJid = getMyChannelJid(getActivity());
		JSONObject allCounters = SyncModel.getInstance().getFromCache(getActivity(), channelJid);
		JSONObject counters = allCounters.optJSONObject(channelJid);
		
		if (counters != null) {
			Integer mentionsCount = counters.has("mentionsCount") ? counters.optInt("mentionsCount") : 0;
			Integer replyCount = counters.has("replyCount") ? counters.optInt("replyCount") : 0;
			Integer visitCount = counters.has("visitCount") ? counters.optInt("visitCount") : 0;

			final String counts = String.format(getString(R.string.channel_list_counts_formatted_txt), mentionsCount, replyCount, visitCount);
			SpannableString boldSpan = new SpannableString(counts);
			boldSpan.setSpan(new StyleSpan(Typeface.BOLD), 0, counts.indexOf("Mentions") - 1, 0);
			boldSpan.setSpan(new StyleSpan(Typeface.BOLD), counts.indexOf("|") + 1, counts.indexOf("Replies") - 1, 0);
			boldSpan.setSpan(new StyleSpan(Typeface.BOLD), counts.lastIndexOf("|") + 1, counts.indexOf("Views") - 1, 0);
			
			final TextView notifCount = (TextView)channelListView.findViewById(R.id.bcUserNotifCounts);
			notifCount.setText(boldSpan);
		}
	}
	
	private String getMyChannelJid(final Context context) {
		return Preferences.getPreference(context, Preferences.MY_CHANNEL_JID);
	}
	
	private void selectChannel(String channelJid) {
		showChannelFragment(channelJid);
		hideMenu();
	}

	private void hideMenu() {
		SlidingFragmentActivity activity = (SlidingFragmentActivity) getActivity();
		if (activity.getSlidingMenu().isMenuShowing()) {
			activity.getSlidingMenu().showContent();
		}
	}
	
	private void showChannelFragment(String channelJid) {
		MainActivity activity = (MainActivity) getActivity();
		activity.getBackStack().clear();
		activity.showChannelFragment(channelJid, true, true);
	}

	@Override
	public void attached(Activity activity) {
		SherlockFragmentActivity sherlockActivity = (SherlockFragmentActivity) activity;
		sherlockActivity.getSupportActionBar().setTitle(R.string.app_name);
	}

	@Override
	public void createOptions(Menu menu) {
		getSherlockActivity().getSupportMenuInflater().inflate(
				R.menu.subscribed_fragment_options, menu);
	}

	@Override
	public boolean menuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.menu_search) {
			Intent searchActivityIntent = new Intent();
			searchActivityIntent.setClass(getActivity(), SearchActivity.class);
			getActivity().startActivityForResult(
					searchActivityIntent, SearchActivity.REQUEST_CODE);
			return true;
		} else if (item.getItemId() == R.id.menu_find_active) {
			showGenericActivity(MostActiveChannelsAdapter.ADAPTER_NAME);
			return true;
		} else if (item.getItemId() == R.id.menu_find_recommended) {
			showGenericActivity(RecommendedChannelsAdapter.ADAPTER_NAME);
			return true;
		} else if (item.getItemId() == R.id.menu_create_channel) {
			createNewTopicChannel();
			return true;
		} else if (item.getItemId() == R.id.menu_find_friends) {
			showGenericActivity(FindFriendsAdapter.ADAPTER_NAME);
			return true;
		} else if (item.getItemId() == R.id.menu_settings) {
			Intent settingsActivityIntent = new Intent();
			settingsActivityIntent.setClass(getActivity(), SettingsActivity.class);
			getActivity().startActivityForResult(
					settingsActivityIntent, SettingsActivity.REQUEST_CODE);
			return true;
		}
		
		return false;
	}

	private void createNewTopicChannel() {
		
		final View view = LayoutInflater.from(getActivity()).inflate(R.layout.topic_channel_create, null);
		if (view == null) return;
		
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(getString(R.string.title_topic_channel_create));
		alert.setView(view);
		
		final EditText channelTopicTxt = (EditText)view.findViewById(R.id.topicChannelTxt);
		channelTopicTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					channelTopicTxt.setError(getString(R.string.message_topic_channel_manadatory));
				} else if (s.toString().contains("@") || s.toString().contains(" ")) {
					channelTopicTxt.setError(getString(R.string.message_topic_channel_invalid));
				}
			}
		});
		
		final TextView topicChannelJidTxt = (TextView)view.findViewById(R.id.topicChannelJidTxt);
		topicChannelJidTxt.setText(String.format(getString(R.string.topic_channel_domain_hint), CreateAccountActivity.BUDDYCLOUD_DOMAIN));
		
		alert.setPositiveButton(getString(R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				String channelJid = channelTopicTxt.getText().toString();
				if (TextUtils.isEmpty(channelJid)) {
					channelTopicTxt.setError(getString(R.string.message_topic_channel_manadatory));
					return ;
				}

				channelJid = channelJid + "@" + CreateAccountActivity.BUDDYCLOUD_DOMAIN;
				TopicChannelModel.getInstance().save(getActivity(), null, new ModelCallback<Void>() {
					@Override
					public void success(Void response) {
						Toast.makeText(getActivity(), 
								getString(R.string.message_topic_channel_create), 
								Toast.LENGTH_LONG).show();
					}
					
					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getActivity(), 
								getString(R.string.message_topic_channel_creation_failed), 
								Toast.LENGTH_LONG).show();
					}
				}, channelJid);
			}
		});

		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		alert.show();
	}

	private void showGenericActivity(String adapterName) {
		Intent intent = new Intent();
		intent.setClass(getActivity(), GenericChannelActivity.class);
		intent.putExtra(GenericChannelActivity.ADAPTER_NAME, adapterName);
		getActivity().startActivityForResult(
				intent, GenericChannelActivity.REQUEST_CODE);
	}

	@Override
	public void dataChanged() {
		adapter.reload(getActivity());
		
	}

	@Override
	public void itemRemoved(String channelJid, String itemId, String parentId) {
	
	}

	@Override
	public void pendingItemAdded(String channelJid, JSONObject pendingItem) {
		// TODO Auto-generated method stub
	}

}
