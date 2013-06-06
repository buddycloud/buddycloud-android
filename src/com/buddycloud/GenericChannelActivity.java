package com.buddycloud;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;
import com.buddycloud.fragments.adapter.FollowersAdapter;
import com.buddycloud.fragments.adapter.GenericChannelAdapter;
import com.buddycloud.fragments.adapter.MostActiveChannelsAdapter;
import com.buddycloud.fragments.adapter.RecommendedChannelsAdapter;
import com.buddycloud.fragments.adapter.SimilarChannelsAdapter;

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

	private GenericChannelAdapter createAdapter(String adapterName) {
		if (adapterName.equals(MostActiveChannelsAdapter.ADAPTER_NAME)) {
			return new MostActiveChannelsAdapter();
		}
		if (adapterName.equals(RecommendedChannelsAdapter.ADAPTER_NAME)) {
			return new RecommendedChannelsAdapter();
		}
		if (adapterName.equals(SimilarChannelsAdapter.ADAPTER_NAME)) {
			String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
			return new SimilarChannelsAdapter(channelJid);
		}
		if (adapterName.equals(FollowersAdapter.ADAPTER_NAME)) {
			String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
			return new FollowersAdapter(channelJid);
		}
		
		return null;
	}
}
