package com.buddycloud;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.fragments.ChannelStreamFragment;
import com.buddycloud.fragments.ContentFragment;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.fragments.PostDetailsFragment;
import com.buddycloud.fragments.SearchChannelsFragment;
import com.buddycloud.fragments.SubscribedChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.NotificationSettingsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.Backstack;
import com.buddycloud.utils.GCMUtils;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

public class MainActivity extends SlidingFragmentActivity {

	private static final String TAG = MainActivity.class.getName();
	private static final boolean DEVELOPER_MODE = false;
	private ContentPageAdapter pageAdapter;
	private String myJid;
	private SubscribedChannelsFragment subscribedChannelsFrag;
	private Backstack backStack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		strict();
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(false);
		setBehindContentView(R.layout.menu_frame);
		
		ViewPager viewPager = new ViewPager(this);
		this.pageAdapter = new ContentPageAdapter(getSupportFragmentManager(), viewPager);
		viewPager.setId("VP".hashCode());
		viewPager.setAdapter(pageAdapter);

		viewPager.setOnPageChangeListener(createPageChangeListener());

		this.backStack = new Backstack(this);
		setContentView(viewPager);
		
		if (shouldLogin()) {
			Intent loginActivity = new Intent();
			loginActivity.setClass(getApplicationContext(), LoginActivity.class);
			startActivityForResult(loginActivity, LoginActivity.REQUEST_CODE);
		} else { 
			startActivity();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@SuppressLint("NewApi")
	private void strict() {
		if (DEVELOPER_MODE) {
	         StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
	                 .detectDiskReads()
	                 .detectDiskWrites()
	                 .detectNetwork()   // or .detectAll() for all detectable problems
	                 .penaltyLog()
	                 .build());
	         StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
	                 .detectLeakedSqlLiteObjects()
	                 .detectLeakedClosableObjects()
	                 .penaltyLog()
	                 .penaltyDeath()
	                 .build());
	     }
	}
	
	private boolean shouldLogin() {
		//TODO Check credentials here too
		return Preferences.getPreference(this, Preferences.MY_CHANNEL_JID) == null || 
				Preferences.getPreference(this, Preferences.PASSWORD) == null || 
				Preferences.getPreference(this, Preferences.API_ADDRESS) == null;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onBackPressed();
			return false;
	    }
	    return true;
	}
	
	@Override
	public void onBackPressed() {
		//TODO Animation between fragments
		if (getSlidingMenu().isMenuShowing()) {
			finish();
		} else if (pageAdapter.getCurrentFragmentIndex() == 0) {
			if (!backStack.pop()) {
				getSlidingMenu().showMenu();
			}
		} else {
			pageAdapter.setCurrentFragment(pageAdapter.getCurrentFragmentIndex() - 1);
		}
	}
	
	private ContentFragment getCurrentFragment() {
		if (getSlidingMenu().isMenuShowing()) {
			return subscribedChannelsFrag;
		} 
		return pageAdapter.getCurrentFragment();
	}
	
	private OnPageChangeListener createPageChangeListener() {
		return new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) { }

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) { }

			@Override
			public void onPageSelected(int position) {
				fragmentChanged();
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
		if (requestCode == LoginActivity.REQUEST_CODE) {
			startActivity();
		} else if (requestCode == SearchActivity.REQUEST_CODE) {
			if (data != null) {
				String channelJid = data.getStringExtra(GenericChannelsFragment.CHANNEL);
				String postId = data.getStringExtra(GenericChannelsFragment.POST_ID);
				String filter = data.getStringExtra(SearchChannelsFragment.FILTER);
				
				backStack.pushSearch(filter);
				
				if (postId != null) {
					showPostDetailFragment(channelJid, postId);
				} else {
					showChannelFragment(channelJid);
				}
			}
		} else if (requestCode == GenericChannelActivity.REQUEST_CODE) {
			if (data != null) {
				final String channelJid = data.getStringExtra(GenericChannelsFragment.CHANNEL);
				showChannelFragment(channelJid);
			}
		}  else if (requestCode == SettingsActivity.REQUEST_CODE) {
			NotificationSettingsModel.getInstance().saveFromPreferences(this, new ModelCallback<JSONObject>() {
				@Override
				public void success(JSONObject response) {
				}

				@Override
				public void error(Throwable throwable) {
				}
			});
		}
	}

	private void startActivity() {
		myJid = (String) Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
		registerInGCM();
		addMenuFragment();
		customizeMenu();
	}

	@Override
	public void onAttachedToWindow() {
		String channelJid = getIntent().getStringExtra(GenericChannelsFragment.CHANNEL);
		if (channelJid == null) {
			showChannelFragment(myJid);
			showMenu();
		} else {
			showChannelFragment(channelJid);
		}
		boolean isNotification = getIntent().getBooleanExtra(
				GCMIntentService.GCM_NOTIFICATION, false);
		if (isNotification) {
			GCMUtils.clearGCMAuthors(this);
		}
		super.onAttachedToWindow();
	}
	
	private void registerInGCM() {
		try {
			GCMUtils.issueGCMRegistration(this);
		} catch (Exception e) {
			Log.e(TAG, "Failed to register in GCM.", e);
		}
	}
	
	public Backstack getBackStack() {
		return backStack;
	}

	public ChannelStreamFragment showChannelFragment(String channelJid) {
		
		ChannelStreamFragment channelFrag = new ChannelStreamFragment();
		Bundle args = new Bundle();
		args.putString(GenericChannelsFragment.CHANNEL, channelJid);
		channelFrag.setArguments(args);
		
		pageAdapter.setLeftFragment(channelFrag);
		
		if (getSlidingMenu().isMenuShowing()) {
			getSlidingMenu().showContent();
		} else {
			fragmentChanged();
		}
		
		return channelFrag;
	}
	
	public PostDetailsFragment showPostDetailFragment(final String channelJid,
			final String postId) {
		PostDetailsFragment postDetailsFrag = new PostDetailsFragment();
		Bundle args = new Bundle();
		args.putString(PostDetailsFragment.POST_ID, postId);
		args.putString(GenericChannelsFragment.CHANNEL, channelJid);
		postDetailsFrag.setArguments(args);
		pageAdapter.setRightFragment(postDetailsFrag);
		getSlidingMenu().showContent();
		return postDetailsFrag;
	}

	private void customizeMenu() {
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.15f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setOnOpenedListener(new OnOpenedListener() {
			@Override
			public void onOpened() {
				fragmentChanged();
			}
		});
		
		sm.setOnClosedListener(new OnClosedListener() {
			@Override
			public void onClosed() {
				fragmentChanged();
			}
		});
	}

	public ContentPageAdapter getPageAdapter() {
		return pageAdapter;
	}

	private void addMenuFragment() {
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		SubscribedChannelsFragment subscribedFrag = new SubscribedChannelsFragment();
		t.replace(R.id.menu_frame, subscribedFrag);
		t.commitAllowingStateLoss();
		this.subscribedChannelsFrag = subscribedFrag;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		ContentFragment currentFragment = getCurrentFragment();
		if (currentFragment != null) {
			currentFragment.createOptions(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return getCurrentFragment().menuItemSelected(featureId, item);
	}

	private void fragmentChanged() {
		getCurrentFragment().attached(this);
		supportInvalidateOptionsMenu();
	}
}
