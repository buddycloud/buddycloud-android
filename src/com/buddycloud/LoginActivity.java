package com.buddycloud;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
        final RelativeLayout postBtn = (RelativeLayout) findViewById(R.id.loginBtn);
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
        
        postBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				postBtn.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				
				String myChannelJid = myChannelTxt.getText().toString();
				String password = passwordTxt.getText().toString();
				
				Preferences.setPreference(LoginActivity.this, Preferences.MY_CHANNEL_JID, myChannelJid);
				Preferences.setPreference(LoginActivity.this, Preferences.PASSWORD, password);
				
				DNSUtils.resolveAPISRV(new ModelCallback<String>() {
					
					@Override
					public void success(String apiAddress) {
						Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, apiAddress);
						checkCredentials();
					}

					@Override
					public void error(Throwable throwable) {
						showLoginError();
					}
					
					private void checkCredentials() {
						LoginModel.getInstance().getAsync(LoginActivity.this, new ModelCallback<Void>() {
							@Override
							public void success(Void response) {
								LoginActivity.this.finish();
							}
							
							@Override
							public void error(Throwable throwable) {
								showLoginError();
							}
						});
					}
					
				}, myChannelJid.split("@")[1]);
			}
		});
    }

    private void showLoginError() {
    	final RelativeLayout postBtn = (RelativeLayout) findViewById(R.id.loginBtn);
        final View progressBar = findViewById(R.id.progressBar);
		Toast.makeText(getApplicationContext(), "Wrong credentials", 
				Toast.LENGTH_LONG).show();
		Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, null);
		postBtn.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_share, menu);
        return true;
    }
}
