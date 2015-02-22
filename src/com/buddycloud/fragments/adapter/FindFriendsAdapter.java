package com.buddycloud.fragments.adapter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.fragments.contacts.ContactMatcher;
import com.buddycloud.fragments.contacts.DeviceContactMatcher;
import com.buddycloud.fragments.contacts.FacebookContactMatcher;
import com.buddycloud.model.ModelCallback;

public class FindFriendsAdapter extends GenericChannelAdapter {

	private static final String ADAPTER_STATE = "ADAPTER_STATE";
	private static final int IN_DEVICE = 1;
	private static final int FACEBOOK = 0;
	
	public static final String ADAPTER_NAME = "FIND_FRIENDS";
	
	public String getTitle(final Context context) {
		return (context != null) ? context.getResources().getString(R.string.menu_find_friends) : null;
	}
	
	@Override
	public void configure(final GenericSelectableChannelsFragment fragment, View view) {
		
		String adapterState = fragment.getActivity().getIntent().getStringExtra(ADAPTER_STATE);
		final Context context = fragment.getActivity();
		
		if (adapterState != null) {
			restoreAdapterState(adapterState, context);
			return;
		}
		
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				context, android.R.layout.select_dialog_item);
		
        arrayAdapter.add(context.getString(R.string.contact_matching_facebook));
        arrayAdapter.add(context.getString(R.string.contact_matching_contact_list));
		
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle(context.getString(R.string.contact_matching_title));
        builderSingle.setAdapter(arrayAdapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContactMatcher matcher = createMatcher(which);
				final String matcherName = matcher.getName();
				matcher.match(fragment.getActivity(), new ModelCallback<JSONArray>() {
					@SuppressLint("DefaultLocale")
					@Override
					public void success(JSONArray response) {
						if (response.length() == 0) {
							Toast.makeText(context, 
									context.getString(R.string.contact_matching_no_friends_found), 
									Toast.LENGTH_LONG).show();
							builderSingle.show();
							return;
						}
						
						for (int i = 0; i < response.length(); i++) {
							addChannel(matcherName.toUpperCase(Locale.getDefault()), 
									response.optJSONObject(i), context);
						}
						saveAdapterState(fragment.getActivity());
					}
					
					@Override
					public void error(Throwable throwable) {
						Toast.makeText(fragment.getActivity(), 
								throwable.getMessage(), Toast.LENGTH_LONG).show();
						builderSingle.show();
					}
				});
			}
		});
        builderSingle.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				fragment.getActivity().finish();
			}
		});
        builderSingle.show();
        
        super.configure(fragment, view);
	}

	private void restoreAdapterState(String adapterStateStr, Context context) {
		JSONObject adapterState = null;
		try {
			adapterState = new JSONObject(adapterStateStr);
		} catch (JSONException e) {
			return; // Do not restore anything
		}
		JSONArray categories = adapterState.names();
		for (int i = 0; i < categories.length(); i++) {
			String category = categories.optString(i);
			JSONArray channelsPerCategory = adapterState.optJSONArray(category);
			for (int j = 0; j < channelsPerCategory.length(); j++) {
				JSONObject channel = channelsPerCategory.optJSONObject(j);
				addChannel(category, channel, context);
			}
		}
	}

	private void saveAdapterState(FragmentActivity fragmentActivity) {
		Map<String, List<JSONObject>> categories = getChannelsPerCategory();
		JSONObject adapterState = new JSONObject();
		for (String category : categories.keySet()) {
			try {
				adapterState.put(category, new JSONArray(categories.get(category)));
			} catch (JSONException e) {
				// Skip category
			}
		}
		fragmentActivity.getIntent().putExtra(ADAPTER_STATE, adapterState.toString());
	}
	
	protected ContactMatcher createMatcher(int which) {
		ContactMatcher matcher = null;
		switch (which) {
		case FACEBOOK:
			matcher = new FacebookContactMatcher();
			break;
		case IN_DEVICE:
			matcher = new DeviceContactMatcher();
			break;
		default:
			break;
		}
		return matcher;
	}
	
}
