package com.buddycloud.fragments.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.model.FollowersModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;

public class FollowersAdapter extends SelectableChannelAdapter {

	public static final String ADAPTER_NAME = "FOLLOWERS";
	
	private static final String OWNER = "OWNER";
	private static final String MODERATOR = "MODERATOR";
	private static final String FOLLOW_POST = "FOLLOW+POST";
	private static final String FOLLOW = "FOLLOW";
	private static final String BANNED = "BANNED";
	
	private String channelJid;
	private String role;

	public FollowersAdapter(String channelJid, String role) {
		this.channelJid = channelJid;
		this.role = role;
		setCategoryOrder(OWNER, MODERATOR, FOLLOW_POST, FOLLOW, BANNED);
	}
	
	public String getTitle(final Context context) {
		return (context != null) ? context.getResources().getString(R.string.menu_channel_followers) : null;
	}
	
	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		super.configure(fragment, view);
	}
		
	public void load(final Context context) {
		showProgress();
		FollowersModel.getInstance().getFromServer(context, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				hideProgress();
				if (response.length() > 0) {
					for (int i = 0; i < response.length(); i++) {
						Iterator<String> keys = response.keys();
						while (keys.hasNext()) {
							String channelJid = keys.next();
							addChannel(getCategory(response.optString(channelJid)), 
									createChannelItem(channelJid), context);
							notifyDataSetChanged();
						}
					}
				} else {
					showNoResultsFoundView(context.getString(R.string.message_followers_not_found));
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				hideProgress();
				showNoResultsFoundView(context.getString(R.string.message_followers_not_found));
				Toast.makeText(context, context.getString(
						R.string.message_followers_load_failed), 
						Toast.LENGTH_LONG).show();
			}

		}, channelJid);
	}
	
	
	private void changeRoles(final Context context, String newRole) {
		Collection<JSONObject> channelItems = getSelection();
		Map<String, String> newRoles = new HashMap<String, String>();
		for (JSONObject channelItem : channelItems) {
			newRoles.put(channelItem.optString("jid"), newRole);
		}
		FollowersModel.getInstance().save(context, new JSONObject(newRoles), new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(context, 
						context.getString(R.string.message_affiliation_change_success), 
						Toast.LENGTH_LONG).show();
				finishActionMode();
				clear();
				notifyDataSetChanged();
				load(context);
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, 
						context.getString(R.string.message_affiliation_change_failed), 
						Toast.LENGTH_LONG).show();
				finishActionMode();
			}
		}, channelJid);
	}
	
	private static String getCategory(String role) {
		if (SubscribedChannelsModel.ROLE_OWNER.equals(role)) {
			return OWNER;
		}
		if (SubscribedChannelsModel.ROLE_MODERATOR.equals(role)) {
			return MODERATOR;
		}
		if (SubscribedChannelsModel.ROLE_PUBLISHER.equals(role)) {
			return FOLLOW_POST;
		}
		if (SubscribedChannelsModel.ROLE_MEMBER.equals(role)) {
			return FOLLOW;
		}
		if (SubscribedChannelsModel.ROLE_OUTCAST.equals(role)) {
			return BANNED;
		}
		return null;
	}

	@Override
	protected boolean canSelect() {
		return SubscribedChannelsModel.canChangeAffiliation(role);
	}

	@Override
	protected void onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.channel_followers_options, menu);
		if (!SubscribedChannelsModel.canMakeModerator(role)) {
			MenuItem menuModerator = menu.findItem(R.id.menu_role_moderator);
			menuModerator.setVisible(false);
		}
	}

	@Override
	protected void onActionMenuClicked(Context context, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_role_moderator:
			changeRoles(context, SubscribedChannelsModel.ROLE_MODERATOR);
			break;
		case R.id.menu_role_publisher:
			changeRoles(context, SubscribedChannelsModel.ROLE_PUBLISHER);
			break;
		case R.id.menu_role_member:
			changeRoles(context, SubscribedChannelsModel.ROLE_MEMBER);
			break;
		case R.id.menu_role_outcast:
			changeRoles(context, SubscribedChannelsModel.ROLE_OUTCAST);
			break;	
		default:
			break;
		}
	}
}
