package com.buddycloud;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.buddycloud.preferences.Preferences;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {
	
	private ContentPageAdapter pageAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setBehindContentView(R.layout.menu_frame);
		
		ViewPager vp = new ViewPager(this);
		vp.setId("VP".hashCode());
		this.pageAdapter = new ContentPageAdapter(getSupportFragmentManager());
		vp.setAdapter(pageAdapter);
		setContentView(vp);
		
		Intent loginActivity = new Intent();
		loginActivity.setClass(getApplicationContext(), LoginActivity.class);
		startActivityForResult(loginActivity, 0);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
		String channelJid = (String) Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
		Fragment myChannelFrag = new ChannelStreamFragment();
		Bundle args = new Bundle();
		args.putString(SubscribedChannelsFragment.CHANNEL, channelJid);
		myChannelFrag.setArguments(args);
		
		pageAdapter.setLeftFragment(myChannelFrag);
	}

	public ContentPageAdapter getPageAdapter() {
		return pageAdapter;
	}
	
	private void addMenuFragment() {
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		Fragment subscribedFrag = new SubscribedChannelsFragment();
		t.replace(R.id.menu_frame, subscribedFrag);
		t.commitAllowingStateLoss();
	}
	
	static class ContentPageAdapter extends FragmentPagerAdapter {
		
		private ArrayList<Fragment> mFragments;

		public ContentPageAdapter(FragmentManager fm) {
			super(fm);
			mFragments = new ArrayList<Fragment>();
		}

		public void setLeftFragment(Fragment fragment) {
			if (mFragments.isEmpty()) {
				mFragments.add(fragment);
			} else {
				mFragments.set(0, fragment);
			}
			notifyDataSetChanged();
		}
		
		public void setRightFragment(Fragment fragment) {
			if (mFragments.size() < 2) {
				mFragments.add(fragment);
			} else {
				mFragments.set(1, fragment);
			}
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

	}
}
