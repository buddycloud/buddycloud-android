package com.buddycloud;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.buddycloud.fragments.ContentFragment;

public class ContentPageAdapter extends FragmentPagerAdapter {

	private ArrayList<ContentFragment> mFragments;
	private final FragmentManager fm;
	private ViewPager viewPager;

	public ContentPageAdapter(FragmentManager fm, ViewPager viewPager) {
		super(fm);
		this.fm = fm;
		this.viewPager = viewPager;
		this.mFragments = new ArrayList<ContentFragment>();
	}

	public void setLeftFragment(ContentFragment fragment) {
		if (mFragments.isEmpty()) {
			mFragments.add(fragment);
		} else {
			fm.beginTransaction().remove(mFragments.get(0)).commitAllowingStateLoss();
			mFragments.set(0, fragment);
		}
		notifyDataSetChanged();
		setCurrentFragment(0);
	}

	public ContentFragment getLeftFragment() {
		return mFragments.get(0);
	}
	
	public void setRightFragment(ContentFragment fragment) {
		if (mFragments.size() < 2) {
			mFragments.add(fragment);
		} else {
			fm.beginTransaction().remove(mFragments.get(1)).commitAllowingStateLoss();
			mFragments.set(1, fragment);
		}
		notifyDataSetChanged();
		setCurrentFragment(1);
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
		if (mFragments.isEmpty()) {
			return null;
		}
		return mFragments.get(position);
	}

	public int getCurrentFragmentIndex() {
		return viewPager.getCurrentItem();
	}
	
	public void setCurrentFragment(int fragIdx) {
		viewPager.setCurrentItem(fragIdx, true);
	}
	
	public ContentFragment getCurrentFragment() {
		return (ContentFragment) getItem(viewPager.getCurrentItem());
	}
}
