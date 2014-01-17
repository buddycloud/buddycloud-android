package com.buddycloud.fragments.adapter;

import java.util.Locale;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.fragments.contacts.ContactMatcher;
import com.buddycloud.fragments.contacts.FacebookContactMatcher;
import com.buddycloud.model.ModelCallback;

public class FindFriendsAdapter extends GenericChannelAdapter {

	public static final String ADAPTER_NAME = "FIND_FRIENDS";
	
	@SuppressLint("DefaultLocale")
	public void configure(final GenericSelectableChannelsFragment fragment, View view) {
		
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                fragment.getActivity(),
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("facebook");
        arrayAdapter.add("twitter");
        arrayAdapter.add("google");
        arrayAdapter.add("contact list");
		
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(
        		fragment.getActivity());
        builderSingle.setTitle("Find friends");
        builderSingle.setAdapter(arrayAdapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ContactMatcher matcher = null;
				if (which == 0) {
					matcher = new FacebookContactMatcher();
				}
				final String matcherName = matcher.getName();
				matcher.match(fragment.getActivity(), new ModelCallback<JSONArray>() {
					@Override
					public void success(JSONArray response) {
						for (int i = 0; i < response.length(); i++) {
							addChannel(matcherName.toUpperCase(Locale.getDefault()), 
									response.optJSONObject(i), fragment.getActivity());
						}
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
        builderSingle.show();
	}
	

}
