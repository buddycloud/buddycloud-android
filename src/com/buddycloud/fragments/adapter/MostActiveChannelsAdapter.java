package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.MostActiveChannelsModel;

public class MostActiveChannelsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "MOST_ACTIVE";
	private static final String MOST_ACTIVE = "MOST ACTIVE";

	public MostActiveChannelsAdapter() {
		setCategoryOrder(MOST_ACTIVE);
	}
	
	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		super.configure(fragment, view);
	}
	
	public String getTitle(final Context context) {
		return (context != null) ? context.getResources().getString(R.string.menu_find_active) : null;
	}
	
	public void load(final Context context) {
		MostActiveChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				if (response.length() > 0) {
					for (int i = 0; i < response.length(); i++) {
						final String channel = response.optString(i);
						addChannel(MOST_ACTIVE, createChannelItem(channel), context);
						notifyDataSetChanged();
					}
				} else {
					showNoResultsFoundView(context.getString(R.string.message_most_active_not_found));
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				showNoResultsFoundView(context.getString(R.string.message_most_active_not_found));
				Toast.makeText(context, context.getString(
						R.string.message_most_active_load_failed), 
						Toast.LENGTH_LONG).show();
			}
		});
	}
}
