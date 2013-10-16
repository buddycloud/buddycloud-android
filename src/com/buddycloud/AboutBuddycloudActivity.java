package com.buddycloud;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class AboutBuddycloudActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_buddycloud);

		Uri contributorsUri = Uri.parse("android.resource://" 
				+ getPackageName() + "/" + R.raw.contributors);
		List<String> contributors = new LinkedList<String>();
		try {
			contributors = IOUtils.readLines(getContentResolver().openInputStream(
					contributorsUri));
		} catch (IOException e1) {
			//Best effort
		}
		
		LinearLayout contributorFrame = (LinearLayout) findViewById(R.id.contributorsFrame);
		for (String contributor : contributors) {
			TextView contributorTxt = new TextView(this);
			contributorTxt.setMovementMethod(LinkMovementMethod.getInstance());
			contributorTxt.setText(Html.fromHtml("<a href=\"http://github.com/" 
					+ contributor + "\">@" + contributor + "</a>"));
			contributorTxt.setTextSize(16);
			
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		    llp.setMargins(0, 4, 0, 4);
		    contributorTxt.setLayoutParams(llp);
			
			contributorFrame.addView(contributorTxt);
		}
		
		TextView versionTxt = (TextView) findViewById(R.id.versionTxt);
		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionTxt.setText(pInfo.versionName);
		} catch (NameNotFoundException e) {
			//Best effort
		}
		
	}
	
}
