package com.buddycloud.fragments.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.model.FollowersModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SubscribedChannelsModel;

public class FollowersAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "FOLLOWERS";
	
	private static final String OWNER = "OWNER";
	private static final String MODERATOR = "MODERATOR";
	private static final String FOLLOW_POST = "FOLLOW+POST";
	private static final String FOLLOW = "FOLLOW";
	private static final String BANNED = "BANNED";
	
	private String channelJid;
	private String role;
	private Map<String, JSONObject> selection = new HashMap<String, JSONObject>();
	protected ActionMode mActionMode;

	private GenericSelectableChannelsFragment fragment;
	private View parentView;
	
	public FollowersAdapter(String channelJid, String role) {
		this.channelJid = channelJid;
		this.role = role;
		setCategoryOrder(OWNER, MODERATOR, FOLLOW_POST, FOLLOW, BANNED);
	}
	
	public void load(final Context context) {
		showProgress();
		FollowersModel.getInstance().getFromServer(context, new ModelCallback<JSONObject>() {
			@SuppressWarnings("unchecked")
			@Override
			public void success(JSONObject response) {
				hideProgress();
				Iterator<String> keys = response.keys();
				while (keys.hasNext()) {
					String channelJid = keys.next();
					addChannel(getCategory(response.optString(channelJid)), 
							createChannelItem(channelJid), context);
					notifyDataSetChanged();
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				hideProgress();
				Toast.makeText(context, context.getString(
						R.string.message_followers_load_failed), 
						Toast.LENGTH_LONG).show();
			}

		}, channelJid);
	}
	
	private void hideProgress() {
		parentView.findViewById(R.id.channelListProgress).setVisibility(View.GONE);
	}

	private void showProgress() {
		parentView.findViewById(R.id.channelListProgress).setVisibility(View.VISIBLE);
	}
	
	
	private Callback createActionModeCallback(final ViewGroup viewGroup) {
		return new ActionMode.Callback() {

			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.channel_followers_options, menu);
				if (!SubscribedChannelsModel.canMakeModerator(role)) {
					MenuItem menuModerator = menu.findItem(R.id.menu_role_moderator);
					menuModerator.setVisible(false);
				}
				return true;
			}

			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				Context context = viewGroup.getContext();
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
				mode.finish();
				return true;
			}

			public void onDestroyActionMode(ActionMode mode) {
				for (int i = 0; i < viewGroup.getChildCount(); i++) {
					viewGroup.getChildAt(i).setSelected(false);
				}
				selection.clear();
				mActionMode = null;
			}
		};
	}
	
	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		this.fragment = fragment;
		this.parentView = view;
	}
	
	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, final ViewGroup viewGroup) {
		final View childView = super.getChildView(groupPosition, childPosition, isLastChild,
						convertView, viewGroup);
		
		if (!SubscribedChannelsModel.canChangeAffiliation(role)) {
			return childView;
		}
		
		childView.setBackgroundResource(R.drawable.channel_item_background_selector);
		childView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				
				JSONObject channelItem = getChild(groupPosition, childPosition);
				
				if (mActionMode == null) {
					Callback actionModeCallback = createActionModeCallback(viewGroup);
	            	mActionMode = fragment.getSherlockActivity().startActionMode(actionModeCallback);
	            }
	            
				String jid = channelItem.optString("jid");
	            boolean mustSelect = !selection.containsKey(jid);
	            if (mustSelect) {
	            	selection.put(jid, channelItem);
	            } else {
	            	selection.remove(jid);
	            	if (selection.isEmpty()) {
	            		mActionMode.finish();
	            	}
	            }
	            childView.setSelected(mustSelect);
	            return true;
			}
		});
		
		return childView;
	}

	private void changeRoles(final Context context, String newRole) {
		Collection<JSONObject> channelItems = selection.values();
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
				if (mActionMode != null) {
					mActionMode.finish();
				}
				clear();
				notifyDataSetChanged();
				load(context);
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, 
						context.getString(R.string.message_affiliation_change_success), 
						Toast.LENGTH_LONG).show();
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
}
