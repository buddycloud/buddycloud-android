package com.buddycloud;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.log.Logger;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.VersionUtils;

public class AboutBuddycloudActivity extends SherlockActivity {

	private static final String TAG = AboutBuddycloudActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_buddycloud);

		ActionbarUtil.showActionBarwithBack(this, getString(R.string.pref_about_bc_title));
		
		Uri contributorsUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.contributors);
		List<String> contributors = new LinkedList<String>();
		try {
			contributors = IOUtils.readLines(getContentResolver().openInputStream(
					contributorsUri));
		} catch (IOException e) {
			Logger.error(TAG, "Error msg ", e);
		}
		
		LinearLayout contributorFrame = (LinearLayout) findViewById(R.id.contributorsFrame);
		for (String c : contributors) {
			
			String[] contribtorInfo = c.split("-");
			if (contribtorInfo.length >= 2) {
	
				TextView contributorTxt = getContributorsView(getApplicationContext(), contribtorInfo[0], contribtorInfo[1]);
				int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
				LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			    llp.setMargins(0, margin, 0, margin);
			    contributorTxt.setLayoutParams(llp);
				contributorFrame.addView(contributorTxt);
			}
		}
		
		final TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
		versionTxt.setText(VersionUtils.getVersionName(getApplicationContext()));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}
	
	/**
	 * Get the contributor text view
	 * 
	 * @param context
	 * @param contributor
	 * @param githubAuthor
	 * @return
	 */
	private TextView getContributorsView(final Context context, 
			final String contributor,
			final String githubAuthor) {
		if (contributor == null || githubAuthor == null) return null;
		
		final String githubUri = "http://github.com/" + githubAuthor.trim();
		final String contributorInfo = contributor.trim() + "\n" + "@" + githubAuthor.trim();
		TextView tv = (TextView)View.inflate(context, R.layout.contributors, null);
		SpannableString hyperLinkSpan = new SpannableString(contributorInfo);
		hyperLinkSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.bc_about_contributor_link_color)), 
				contributorInfo.indexOf("@"), hyperLinkSpan.length(), 0);
		hyperLinkSpan.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(githubUri));
				startActivity(intent);
			}
		}, contributorInfo.indexOf("@"), hyperLinkSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		hyperLinkSpan.setSpan(new UnderlineSpan(), contributorInfo.indexOf("@"), hyperLinkSpan.length(), 0);
		tv.setText(hyperLinkSpan);
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		return tv;
	}
}
