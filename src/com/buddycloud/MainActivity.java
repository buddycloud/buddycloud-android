package com.buddycloud;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.buddycloud.preferences.Preferences;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setBehindContentView(R.layout.menu_frame);
		setContentView(R.layout.content_frame);
		
		Intent loginActivity = new Intent();
		loginActivity.setClass(getApplicationContext(), LoginActivity.class);
		startActivityForResult(loginActivity, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		addMenuFragment();
		addContentFragment();
		customizeMenu();
	}

	private void customizeMenu() {
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	private void addContentFragment() {
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		String channelJid = (String) Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
		Fragment myChannelFrag = new ChannelStreamFragment();
		Bundle args = new Bundle();
		args.putString(SubscribedChannelsFragment.CHANNEL, channelJid);
		myChannelFrag.setArguments(args);
		t.replace(R.id.content_frame, myChannelFrag);
		t.commitAllowingStateLoss();
	}

	private void addMenuFragment() {
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		Fragment subscribedFrag = new SubscribedChannelsFragment();
		t.replace(R.id.menu_frame, subscribedFrag);
		t.commitAllowingStateLoss();
	}
}
