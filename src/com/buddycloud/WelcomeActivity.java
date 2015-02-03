package com.buddycloud;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.notifications.GCMUtils;

/**
 * This activity used to show the welcome screen 
 * and handle all the actions.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class WelcomeActivity extends SherlockActivity {

	private static final String TAG = WelcomeActivity.class.getSimpleName();
	
	public static final int REQUEST_CODE = 101;
	public static final int RESULT_CODE_OK = 1010;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		getSupportActionBar().hide();

		final Button signupBtn = (Button) findViewById(R.id.signupBtn);
		final Button loginBtn = (Button) findViewById(R.id.loginBtn);

		signupBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), CreateAccountActivity.class);
				startActivityForResult(intent, CreateAccountActivity.REQUEST_CODE);
			}
		});

		loginBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), LoginActivity.class);
				startActivityForResult(intent, LoginActivity.REQUEST_CODE);
			}
		});
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Check for the Google Play Services for GCM.
		GCMUtils.checkPlayServices(this);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == LoginActivity.REQUEST_CODE) {
			if (resultCode == LoginActivity.LOGGED_IN_RESULT) {
				
				// created account through > Welcome > Login
				setResult(RESULT_CODE_OK);
	    		finish();
			}
			else if (resultCode == CreateAccountActivity.ACCOUNT_CREATED_RESULT) {
				
				// created account through > Welcome > Login > Create Account
	    		setResult(RESULT_CODE_OK);
	    		finish();
			}
		}
		else if (requestCode == CreateAccountActivity.REQUEST_CODE && 
    			resultCode == CreateAccountActivity.ACCOUNT_CREATED_RESULT) {
			
			// created account through > Welcome > Create Account
    		setResult(RESULT_CODE_OK);
    		finish();
    	}
		
    	super.onActivityResult(requestCode, resultCode, data);
    }
}