package com.buddycloud;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;

public class LoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button postBtn = (Button) findViewById(R.id.postBtn);
        final EditText myChannelTxt = (EditText) findViewById(R.id.loginTxt);
        final EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        
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
				String myChannelJid = myChannelTxt.getText().toString();
				String password = passwordTxt.getText().toString();
				
				Preferences.setPreference(LoginActivity.this, Preferences.MY_CHANNEL_JID, myChannelJid);
				Preferences.setPreference(LoginActivity.this, Preferences.PASSWORD, password);
				
				DNSUtils.resolveAPISRV(new ModelCallback<String>() {
					
					@Override
					public void success(String apiAddress) {
						Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, apiAddress);
						LoginActivity.this.finish();
					}
					
					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getApplicationContext(), "Failure to discover API, using default.", 
								Toast.LENGTH_LONG).show();
						Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, Preferences.DEFAULT_API_ADDRESS);
						LoginActivity.this.finish();
						
					}
				}, myChannelJid.split("@")[1]);
			}
		});
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_share, menu);
        return true;
    }
}
