package com.buddycloud;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.buddycloud.preferences.Preferences;

public class LoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button postBtn = (Button) findViewById(R.id.postBtn);
        final EditText loginTxt = (EditText) findViewById(R.id.loginTxt);
        final EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        final EditText apiAddressTxt = (EditText) findViewById(R.id.apiAddressTxt);
        
        String loginPref = Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
        if (loginPref != null) {
        	loginTxt.setText(loginPref);
        }
        
        String passPref = Preferences.getPreference(this, Preferences.PASSWORD);
        if (passPref != null) {
        	passwordTxt.setText(passPref);
        }
        
        String apiAddressPref = Preferences.getPreference(this, Preferences.API_ADDRESS);
        if (apiAddressPref != null) {
        	apiAddressTxt.setText(apiAddressPref);
        } else {
        	apiAddressTxt.setText(Preferences.DEFAULT_API_ADDRESS);
        }
        
        postBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String login = loginTxt.getText().toString();
				String password = passwordTxt.getText().toString();
				String apiAddress = apiAddressTxt.getText().toString();
				
				Preferences.setPreference(LoginActivity.this, Preferences.MY_CHANNEL_JID, login);
				Preferences.setPreference(LoginActivity.this, Preferences.PASSWORD, password);
				Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, apiAddress);
				
				LoginActivity.this.finish();
			}
		});
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_share, menu);
        return true;
    }
}
