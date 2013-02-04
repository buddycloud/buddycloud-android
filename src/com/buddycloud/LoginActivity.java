package com.buddycloud;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {

	private static final String PREFS_NAME = "BuddycloudPrefsFile";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button postBtn = (Button) findViewById(R.id.postBtn);
        final EditText loginTxt = (EditText) findViewById(R.id.loginTxt);
        final EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
        
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
        String loginPref = preferences.getString("login", null);
        if (loginPref != null) {
        	loginTxt.setText(loginPref);
        }
        
        String passPref = preferences.getString("password", null);
        if (passPref != null) {
        	passwordTxt.setText(passPref);
        }
        
        postBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
				Editor editor = preferences.edit();
				
				String login = loginTxt.getText().toString();
				String password = passwordTxt.getText().toString();
				
				editor.putString("login", login);
				editor.putString("password", password);
				
				editor.commit();
				
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
