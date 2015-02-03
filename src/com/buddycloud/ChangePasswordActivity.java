package com.buddycloud;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;

public class ChangePasswordActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
		View changePasswordBtn = findViewById(R.id.changePasswordBtn);
		changePasswordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgress();
				EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
				EditText repeatPasswordTxt = (EditText) findViewById(R.id.repeatPasswordTxt);
				
				final String password = passwordTxt.getText().toString();
				if (!password.equals(
						repeatPasswordTxt.getText().toString())) {
//					Toast.makeText(getApplicationContext(), getString(
//							R.string.message_account_passwords_donot_match), 
//							Toast.LENGTH_SHORT).show();
					hideProgress();
					return;
				}
				
				EditText currentPasswordTxt = (EditText) findViewById(R.id.currentPasswordTxt);
				AccountModel.getInstance().changePassword(getApplicationContext(), 
						currentPasswordTxt.getText().toString(), password, 
						new ModelCallback<Void>() {
							@Override
							public void success(Void response) {
								hideProgress();
								Preferences.setPreference(getApplicationContext(), 
										Preferences.PASSWORD, password);
								Toast.makeText(getApplicationContext(), getString(
										R.string.message_change_password_success), 
										Toast.LENGTH_SHORT).show();
								finish();
							}

							@Override
							public void error(Throwable throwable) {
								hideProgress();
								Toast.makeText(getApplicationContext(), getString(
										R.string.message_change_password_failed), 
										Toast.LENGTH_SHORT).show();
							}
						});
			}
		});
	}
	
	protected void showProgress() {
		findViewById(R.id.changePasswordBtn).setVisibility(View.GONE);
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
	}
	
	protected void hideProgress() {
		findViewById(R.id.changePasswordBtn).setVisibility(View.VISIBLE);
		findViewById(R.id.progressBar).setVisibility(View.GONE);
	}
}
