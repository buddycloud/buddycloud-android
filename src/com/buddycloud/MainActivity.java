package com.buddycloud;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationMetadataModel;
import com.buddycloud.preferences.Preferences;
import com.google.android.gcm.GCMRegistrar;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

	private static final String TAG = MainActivity.class.getName();
	private ContentPageAdapter pageAdapter;
	private ViewPager viewPager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setBehindContentView(R.layout.menu_frame);

		this.viewPager = new ViewPager(this);
		this.pageAdapter = new ContentPageAdapter(getSupportFragmentManager());
		viewPager.setId("VP".hashCode());
		viewPager.setAdapter(pageAdapter);

		viewPager.setOnPageChangeListener(createPageChangeListener());
		viewPager.setOffscreenPageLimit(2);

		setContentView(viewPager);

		if (shouldLogin()) {
			Intent loginActivity = new Intent();
			loginActivity.setClass(getApplicationContext(), LoginActivity.class);
			startActivityForResult(loginActivity, 0);
		} else { 
			startActivity();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private boolean shouldLogin() {
		return Preferences.getPreference(this, Preferences.MY_CHANNEL_JID) == null || 
				Preferences.getPreference(this, Preferences.PASSWORD) == null || 
				Preferences.getPreference(this, Preferences.API_ADDRESS) == null;
	}

	private OnPageChangeListener createPageChangeListener() {
		return new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) { }

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }

			@Override
			public void onPageSelected(int position) {
				switch (position) {
				case 0:
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
					break;
				default:
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
					break;
				}
			}
		};
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		startActivity();
	}

	private void startActivity() {
		registerInGCM();
		addMenuFragment();
		showMyChannelFragment();
		customizeMenu();
	}

	private void registerInGCM() {
		try {
			issueGCMRegistration();
		} catch (Exception e) {
			Log.e(TAG, "Failed to register in GCM.", e);
		}
	}

	private void issueGCMRegistration() {
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			NotificationMetadataModel.getInstance().refresh(this, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
					String sender = response.optString("google_project_id", null);
					if (sender != null) {
						GCMRegistrar.register(getApplicationContext(), sender);
					} else {
						Log.w(TAG, "GCM project id not found.");
					}
				}
				@Override
				public void error(Throwable throwable) {
					Log.e(TAG, "Failed to register in GCM.", throwable);
				}
			});
		} else {
			GCMIntentService.sendToPusher(this, regId);
		}
	}

	private void showMyChannelFragment() {
		String channelJid = (String) Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
		Fragment myChannelFrag = new ChannelStreamFragment();
		Bundle args = new Bundle();
		args.putString(SubscribedChannelsFragment.CHANNEL, channelJid);
		myChannelFrag.setArguments(args);
		setLeftFragment(myChannelFrag);
	}

	private void customizeMenu() {
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	public void setLeftFragment(Fragment frag) {
		pageAdapter.setLeftFragment(frag);
		viewPager.setCurrentItem(0);
	}

	public void setRightFragment(Fragment frag) {
		pageAdapter.setRightFragment(frag);
		viewPager.setCurrentItem(1);
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

	private static class ContentPageAdapter extends FragmentPagerAdapter {

		private ArrayList<Fragment> mFragments;
		private final FragmentManager fm;

		public ContentPageAdapter(FragmentManager fm) {
			super(fm);
			this.fm = fm;
			this.mFragments = new ArrayList<Fragment>();
		}

		public void setLeftFragment(Fragment fragment) {
			if (mFragments.isEmpty()) {
				mFragments.add(fragment);
			} else {
				fm.beginTransaction().remove(mFragments.get(0)).commit();
				mFragments.set(0, fragment);
			}
			notifyDataSetChanged();
		}

		public void setRightFragment(Fragment fragment) {
			if (mFragments.size() < 2) {
				mFragments.add(fragment);
			} else {
				fm.beginTransaction().remove(mFragments.get(1)).commit();
				mFragments.set(1, fragment);
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public int getItemPosition(Object object) {
			if (mFragments.contains(object)) {
				return POSITION_UNCHANGED;
			}
			return POSITION_NONE;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

	}
}
