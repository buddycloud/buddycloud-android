package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.RecommendedChannelsModel;

public class RecommendedChannelsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "RECOMMENDED";
	private static final String RECOMMENDED = "RECOMMENDED";
	
	public RecommendedChannelsAdapter() {
		setCategoryOrder(RECOMMENDED);
	}
	
	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		super.configure(fragment, view);
	}
	
	public String getTitle(final Context context) {
		return (context != null) ? context.getResources().getString(R.string.menu_find_recommended) : null;
	}

	public void load(final Context context) {
		RecommendedChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				if (response.length() > 0) {
					for (int i = 0; i < response.length(); i++) {
						final String channel = response.optString(i);
						addChannel(RECOMMENDED, createChannelItem(channel), context);
						notifyDataSetChanged();
					}
				} else {
					showNoResultsFoundView(context.getString(R.string.message_recommended_not_found));
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				showNoResultsFoundView(context.getString(R.string.message_recommended_not_found));
				Toast.makeText(context, context.getString(
						R.string.message_recommended_load_failed), 
						Toast.LENGTH_LONG).show();
			}
		});
	}
}
