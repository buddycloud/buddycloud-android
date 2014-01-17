package com.buddycloud;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.fragments.adapter.FindFriendsAdapter;
import com.buddycloud.fragments.adapter.FollowersAdapter;
import com.buddycloud.fragments.adapter.GenericChannelAdapter;
import com.buddycloud.fragments.adapter.MostActiveChannelsAdapter;
import com.buddycloud.fragments.adapter.PendingSubscriptionsAdapter;
import com.buddycloud.fragments.adapter.RecommendedChannelsAdapter;
import com.buddycloud.fragments.adapter.SimilarChannelsAdapter;
import com.buddycloud.model.SubscribedChannelsModel;
import com.facebook.Session;

public class GenericChannelActivity extends SherlockFragmentActivity {

	public final static String ADAPTER_NAME = "com.buddycloud.ADAPTER_NAME";
	public static final int REQUEST_CODE = 103;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_channels);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		final GenericSelectableChannelsFragment frag = new GenericSelectableChannelsFragment();
		String adapterName = getIntent().getStringExtra(ADAPTER_NAME);
		frag.setAdapter(createAdapter(adapterName));
		transaction.replace(R.id.contentFrame, frag);
		transaction.commitAllowingStateLoss();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

	private GenericChannelAdapter createAdapter(String adapterName) {
		if (adapterName.equals(MostActiveChannelsAdapter.ADAPTER_NAME)) {
			return new MostActiveChannelsAdapter();
		}
		if (adapterName.equals(RecommendedChannelsAdapter.ADAPTER_NAME)) {
			return new RecommendedChannelsAdapter();
		}
		if (adapterName.equals(FindFriendsAdapter.ADAPTER_NAME)) {
			return new FindFriendsAdapter();
		}
		if (adapterName.equals(SimilarChannelsAdapter.ADAPTER_NAME)) {
			String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
			return new SimilarChannelsAdapter(channelJid);
		}
		if (adapterName.equals(FollowersAdapter.ADAPTER_NAME)) {
			String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
			String role = getIntent().getStringExtra(SubscribedChannelsModel.ROLE);
			return new FollowersAdapter(channelJid, role);
		}
		if (adapterName.equals(PendingSubscriptionsAdapter.ADAPTER_NAME)) {
			String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
			String role = getIntent().getStringExtra(SubscribedChannelsModel.ROLE);
			return new PendingSubscriptionsAdapter(channelJid, role);
		}
		
		return null;
	}
}
