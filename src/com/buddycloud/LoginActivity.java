package com.buddycloud;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.buddycloud.http.SSLUtils;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.LoginModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;

public class LoginActivity extends SherlockActivity {

	public static final int REQUEST_CODE = 101;
	public static final int RESULT_CODE_OK = 1010;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        
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
        
		passwordTxt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					login(loginBtn, myChannelTxt, passwordTxt, progressBar);
				}
				return false;
			}
		});
        
        loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				login(loginBtn, myChannelTxt, passwordTxt, progressBar);
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
        
        TextView forgotPasswordTxt = (TextView) findViewById(R.id.forgotPasswordText);
        forgotPasswordTxt.setMovementMethod(LinkMovementMethod.getInstance());
        forgotPasswordTxt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetPassword();
			}
		});
    }

    protected void resetPassword() {
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.title_reset_password));
		alert.setMessage(getString(R.string.message_reset_password));
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton(getString(R.string.ok), 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String userJid = input.getText().toString();
				AccountModel.getInstance().resetPassword(getApplicationContext(), 
						userJid, new ModelCallback<JSONObject>() {
							@Override
							public void success(JSONObject response) {
								Toast.makeText(getApplicationContext(), 
										getString(R.string.message_password_successfully_reset), 
										Toast.LENGTH_LONG).show();
							}

							@Override
							public void error(Throwable throwable) {
								Toast.makeText(getApplicationContext(), 
										getString(R.string.message_password_reset_failed), 
										Toast.LENGTH_LONG).show();
							}
				});
			}
		});

		alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		alert.show();
    }

	private void showLoginError(int stringId) {
    	Toast.makeText(getApplicationContext(), getString(stringId), 
    			Toast.LENGTH_LONG).show();
    	clearAPIAddress();
    	hideProgress();
	}

	private void clearAPIAddress() {
		Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, null);
	}

	private void hideProgress() {
		final RelativeLayout postBtn = (RelativeLayout) findViewById(R.id.loginBtn);
        final View progressBar = findViewById(R.id.progressBar);
        postBtn.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == CreateAccountActivity.REQUEST_CODE && 
    			resultCode == CreateAccountActivity.ACCOUNT_CREATED_RESULT) {
    		setResult(RESULT_CODE_OK);
    		finish();
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
	protected void login(final RelativeLayout loginBtn,
			final EditText myChannelTxt, final EditText passwordTxt,
			final View progressBar) {
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
		
		// Resolve the API server through DNS lookup
		DNSUtils.resolveAPISRV(new ModelCallback<String>() {
			
			@Override
			public void success(String apiAddress) {
				Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, apiAddress);
				SSLUtils.checkSSL(LoginActivity.this, apiAddress, new ModelCallback<Void>() {
					@Override
					public void success(Void response) {
						checkCredentials();
					}
					@Override
					public void error(Throwable throwable) {
						
						// Do nothing, SSL error not tolerable
						clearAPIAddress();
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
						setResult(RESULT_CODE_OK);
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
}
