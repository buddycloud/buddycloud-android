package com.buddycloud.fragments.adapter;

import org.json.JSONArray;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.SimilarChannelsModel;

public class SimilarChannelsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "SIMILAR";
	private static final String SIMILAR = "SIMILAR";
	private String channelJid;
	
	public SimilarChannelsAdapter(String channelJid) {
		this.channelJid = channelJid;
		setCategoryOrder(SIMILAR);
	}
	
	public String getTitle(final Context context) {
		return (context != null) ? context.getResources().getString(R.string.menu_similar_channels) : null;
	}
	
	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		super.configure(fragment, view);
	}
	
	public void load(final Context context) {
		SimilarChannelsModel.getInstance().getFromServer(context, new ModelCallback<JSONArray>() {
			@Override
			public void success(JSONArray response) {
				if (response.length() > 0) {
					for (int i = 0; i < response.length(); i++) {
						final String channel = response.optString(i);
						addChannel(SIMILAR, createChannelItem(channel), context);
						notifyDataSetChanged();
					}
				} else {
					showNoResultsFoundView(context.getString(R.string.message_similar_not_found));
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				showNoResultsFoundView(context.getString(R.string.message_similar_not_found));
				Toast.makeText(context, context.getString(
						R.string.message_similar_load_failed), 
						Toast.LENGTH_LONG).show();
			}
		}, channelJid);
	}
}
