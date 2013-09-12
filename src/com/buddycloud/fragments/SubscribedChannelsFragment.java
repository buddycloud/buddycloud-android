package com.buddycloud.fragments;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.GenericChannelActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.SearchActivity;
import com.buddycloud.SettingsActivity;
import com.buddycloud.fragments.adapter.MostActiveChannelsAdapter;
import com.buddycloud.fragments.adapter.RecommendedChannelsAdapter;
import com.buddycloud.fragments.adapter.SubscribedChannelsAdapter;
import com.buddycloud.model.ChannelMetadataModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.ModelListener;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.model.SyncModel;
import com.buddycloud.model.TopicChannelModel;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class SubscribedChannelsFragment extends ContentFragment implements ModelListener {

	private SubscribedChannelsAdapter adapter = new SubscribedChannelsAdapter();
	private GenericChannelsFragment genericChannelFrag = new GenericChannelsFragment(adapter) {
		@Override
		public void channelSelected(JSONObject channelItem) {
			selectChannel(channelItem.optString("jid"));
		}
	};
	
	public SubscribedChannelsFragment() {
		TopicChannelModel.getInstance().addListener(this);
		SubscribedChannelsModel.getInstance().addListener(this);
		ModelListener notifyChangeListener = new ModelListener() {
			@Override
			public void dataChanged() {
				adapter.sort(getActivity());
				adapter.notifyDataSetChanged();
			}
		};
		SyncModel.getInstance().addListener(notifyChangeListener);
		ChannelMetadataModel.getInstance().addListener(notifyChangeListener);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		adapter.load(getActivity());
		return genericChannelFrag.onCreateView(inflater, container, savedInstanceState);
	}

	private void selectChannel(String channelJid) {
		showChannelFragment(channelJid);
		SyncModel.getInstance().resetCounter(getActivity(), channelJid);
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
		activity.showChannelFragment(channelJid);
	}

	@Override
	public void attached(Activity activity) {
		SherlockFragmentActivity sherlockActivity = (SherlockFragmentActivity) activity;
		sherlockActivity.getSupportActionBar().setTitle("");
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
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle(getString(R.string.title_topic_channel_create));
		alert.setMessage(getString(R.string.message_topic_channel_hint));
		final EditText input = new EditText(getActivity());
		alert.setView(input);

		alert.setPositiveButton(getString(R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String channelJid = input.getText().toString();
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

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

}
