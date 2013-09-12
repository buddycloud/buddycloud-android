package com.buddycloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.buddycloud.http.SSLUtils;
import com.buddycloud.model.LoginModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;

public class LoginActivity extends Activity {

	public static final int REQUEST_CODE = 101;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final RelativeLayout loginBtn = (RelativeLayout) findViewById(R.id.loginBtn);
        final RelativeLayout createAccountBtn = (RelativeLayout) findViewById(R.id.createAccountBtn);
        final EditText myChannelTxt = (EditText) findViewById(R.id.loginTxt);
        final EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        final View progressBar = findViewById(R.id.progressBar);
        
        String myChannelPref = Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
        if (myChannelPref != null) {
        	myChannelTxt.setText(myChannelPref);
        }
        
        String passPref = Preferences.getPreference(this, Preferences.PASSWORD);
        if (passPref != null) {
        	passwordTxt.setText(passPref);
        }
        
        loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				loginBtn.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				
				String myChannelJid = myChannelTxt.getText().toString();
				String password = passwordTxt.getText().toString();
				
				Preferences.setPreference(LoginActivity.this, Preferences.MY_CHANNEL_JID, myChannelJid);
				Preferences.setPreference(LoginActivity.this, Preferences.PASSWORD, password);
				
				String[] myChannelJidSplit = myChannelJid.split("@");
				
				if (myChannelJidSplit.length < 2) {
					showLoginError(R.string.login_error_bad_channel_format);
					return;
				}
				
				DNSUtils.resolveAPISRV(new ModelCallback<String>() {
					
					@Override
					public void success(String apiAddress) {
						Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, apiAddress);
						SSLUtils.checkSSL(getApplicationContext(), apiAddress, new ModelCallback<Void>() {
							@Override
							public void success(Void response) {
								checkCredentials();
							}
							@Override
							public void error(Throwable throwable) {
								// Do nothing, SSL error not tolerable
								hideProgress();
							}
						});
					}

					@Override
					public void error(Throwable throwable) {
						showLoginError(R.string.login_error_wrong_credentials);
					}
					
					private void checkCredentials() {
						LoginModel.getInstance().getFromServer(LoginActivity.this, new ModelCallback<Void>() {
							@Override
							public void success(Void response) {
								LoginActivity.this.finish();
							}
							
							@Override
							public void error(Throwable throwable) {
								showLoginError(R.string.login_error_wrong_credentials);
							}
						});
					}
					
				}, myChannelJidSplit[1]);
			}
		});
        
        createAccountBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getApplicationContext(), CreateAccountActivity.class);
				startActivityForResult(intent, CreateAccountActivity.REQUEST_CODE);
			}
		});
    }

    private void showLoginError(int stringId) {
    	Toast.makeText(getApplicationContext(), getString(stringId), 
    			Toast.LENGTH_LONG).show();
    	Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, null);
    	hideProgress();
	}

	private void hideProgress() {
		final RelativeLayout postBtn = (RelativeLayout) findViewById(R.id.loginBtn);
        final View progressBar = findViewById(R.id.progressBar);
        postBtn.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == CreateAccountActivity.REQUEST_CODE && resultCode == CreateAccountActivity.ACCOUNT_CREATED_RESULT) {
    		finish();
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_share, menu);
        return true;
    }
}
