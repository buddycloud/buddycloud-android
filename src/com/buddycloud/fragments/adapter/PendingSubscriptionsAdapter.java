package com.buddycloud.fragments.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
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
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PendingSubscriptionsModel;
import com.buddycloud.model.SubscribedChannelsModel;

public class PendingSubscriptionsAdapter extends SelectableChannelAdapter {

	public static final String ADAPTER_NAME = "PENDING";
	private static final String PENDING = "PENDING";
	
	private String channelJid;
	private String role;

	public PendingSubscriptionsAdapter(String channelJid, String role) {
		this.channelJid = channelJid;
		this.role = role;
		setCategoryOrder(PENDING);
	}
	
	public String getTitle(final Context context) {
		return (context != null) ? context.getString(R.string.menu_pending_subscriptions) : null;
	}
	
	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		super.configure(fragment, view);
	}
	
	public void load(final Context context) {
		showProgress();
		PendingSubscriptionsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				hideProgress();
				if (response.length() > 0) {
					for (int i = 0; i < response.length(); i++) {
						String channelJid = response.optString(i);
						addChannel(PENDING, createChannelItem(channelJid), context);
						notifyDataSetChanged();
					}
				} else {
					showNoResultsFoundView(context.getString(R.string.message_subscription_not_found));
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				hideProgress();
				showNoResultsFoundView(context.getString(R.string.menu_pending_subscriptions));
				Toast.makeText(context, context.getString(
						R.string.message_pending_subsriptions_load_failed), 
						Toast.LENGTH_LONG).show();
			}

		}, channelJid);
	}

	private void changeSubscriptions(final Context context, String newSubscription) {
		Collection<JSONObject> channelItems = getSelection();
		List<JSONObject> subscriptions = new LinkedList<JSONObject>();
		for (JSONObject channelItem : channelItems) {
			Map<String, String> subscription = new HashMap<String, String>();
			subscription.put("jid", channelItem.optString("jid"));
			subscription.put("subscription", newSubscription);
			subscriptions.add(new JSONObject(subscription));
		}
		PendingSubscriptionsModel.getInstance().save(context, new JSONArray(subscriptions), new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				Toast.makeText(context, 
						context.getString(R.string.message_subscription_change_success), 
						Toast.LENGTH_LONG).show();
				finishActionMode();
				clear();
				notifyDataSetChanged();
				load(context);
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, 
						context.getString(R.string.message_subscription_change_failed), 
						Toast.LENGTH_LONG).show();
			}
		}, channelJid);
	}
	
	@Override
	protected boolean canSelect() {
		return SubscribedChannelsModel.canChangeAffiliation(role);
	}

	@Override
	protected void onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.pending_subscriptions_options, menu);
	}

	@Override
	protected void onActionMenuClicked(Context context, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_pending_subscription_approve:
			changeSubscriptions(context, SubscribedChannelsModel.SUBSCRIPTION_SUBSCRIBED);
			break;
		case R.id.menu_pending_subscription_deny:
			changeSubscriptions(context, SubscribedChannelsModel.SUBSCRIPTION_NONE);
			break;
		default:
			break;
		}
	}
}
