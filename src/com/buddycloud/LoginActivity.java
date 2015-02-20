package com.buddycloud;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.customviews.TooltipErrorView;
import com.buddycloud.http.SSLUtils;
import com.buddycloud.model.LoginModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.DNSUtils;
import com.buddycloud.utils.InputUtils;

/**
 * This activity used to show the login screen 
 * and handle all the actions.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class LoginActivity extends SherlockActivity {

	private static final String TAG = LoginActivity.class.getSimpleName();
	
	public static final int REQUEST_CODE = 104;
	public static final int LOGGED_IN_RESULT = 204;
	
	private EditText mUsernameTxt;
	private EditText mPasswordTxt;

	private TooltipErrorView mUsernameErrorTooltip;
	private TooltipErrorView mPasswordErrorTooltip;
	private ProgressDialog mProgressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionbarUtil.showActionBarwithBack(this, getString(R.string.login_title));
        
		mUsernameErrorTooltip = (TooltipErrorView) findViewById(R.id.usernameErrorTooltip);
		mUsernameTxt = (EditText) findViewById(R.id.usernameTxt);
		mUsernameTxt.addTextChangedListener(mUserNameTxtWatcher);

		mPasswordErrorTooltip = (TooltipErrorView) findViewById(R.id.passwordErrorTooltip);
		mPasswordTxt = (EditText) findViewById(R.id.passwordTxt);
		mPasswordTxt.addTextChangedListener(mPasswordTxtWatcher);
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.message_logging_in));
		mProgressDialog.setCancelable(false);
		
        String myChannelPref = Preferences.getPreference(this, Preferences.MY_CHANNEL_JID);
        if (myChannelPref != null) {
        	mUsernameTxt.setText(myChannelPref);
        }
        
        String passPref = Preferences.getPreference(this, Preferences.PASSWORD);
        if (passPref != null) {
        	mPasswordTxt.setText(passPref);
        }
        mPasswordTxt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
					final String myChannelJid = getValue(R.id.usernameTxt);
					final String passwordTxt = getValue(R.id.passwordTxt);
					
					login(myChannelJid, passwordTxt);
				}
				return false;
			}
		});
        
		final Button loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				final String myChannelJid = getValue(R.id.usernameTxt);
				final String passwordTxt = getValue(R.id.passwordTxt);
				
				login(myChannelJid, passwordTxt);
			}
		});

        final TextView forgotPasswordLink = (TextView) findViewById(R.id.forgotPasswordLink);
        forgotPasswordLink.setMovementMethod(LinkMovementMethod.getInstance());
        forgotPasswordLink.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				resetPassword();
			}
		});
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.login_screen_options, menu);
		
        MenuItem item = menu.findItem(R.id.menu_signup);
        if (item != null) {
        	item.setTitle(getString(R.string.signup_button).toUpperCase());
        }
        
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(LoginActivity.this);
        		finish();
        		return true;
           	case R.id.menu_signup:
           		InputUtils.hideKeyboard(LoginActivity.this);
           		createAccount();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CreateAccountActivity.REQUEST_CODE && 
    			resultCode == CreateAccountActivity.ACCOUNT_CREATED_RESULT) {
			
    		setResult(CreateAccountActivity.ACCOUNT_CREATED_RESULT);
    		finish();
    	}
		
    	super.onActivityResult(requestCode, resultCode, data);
    }
	
	/**
	 * Show the account signup screen
	 */
	private void createAccount() {
	
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), CreateAccountActivity.class);
		startActivityForResult(intent, CreateAccountActivity.REQUEST_CODE);
	}
	
	/**
	 * Reset the password
	 * 
	 */
    private void resetPassword() {
    	
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), RecoverPasswordActivity.class);
		startActivity(intent);
    }

	private void clearAPIAddress() {
		Preferences.setPreference(LoginActivity.this, Preferences.API_ADDRESS, null);
	}

	private void showErrorToolTip(TooltipErrorView errorView, String errorMsg) {

		if (errorView != null && !isEmpty(errorMsg)) {
			errorView.setText(errorMsg);
			errorView.setVisibility(View.VISIBLE);
		}
	}

	private void hideAllErrorTooltips() {

		if (mUsernameErrorTooltip != null && mPasswordErrorTooltip != null) {

			mUsernameErrorTooltip.setVisibility(View.GONE);
			mPasswordErrorTooltip.setVisibility(View.GONE);
		}
	}
	
	private static boolean isEmpty(String string) {
		return string.length() == 0;
	}
	
	private String getValue(int resId) {
		return ((EditText) findViewById(resId)).getText().toString();
	}
	
	private void login(final String myChannelJid, final String passwordTxt) {
	
		if (isEmpty(myChannelJid)) {
			showErrorToolTip(mUsernameErrorTooltip,
					getString(R.string.message_account_username_mandatory));
			return;
		}

		if (isEmpty(passwordTxt)) {
			showErrorToolTip(mPasswordErrorTooltip,
					getString(R.string.message_account_password_mandatory));
			return;
		}
		
		String[] myChannelJidSplit = myChannelJid.split("@");
		if (myChannelJidSplit.length < 2) {
			showErrorToolTip(mUsernameErrorTooltip, getString(R.string.login_error_bad_channel_format));
			return;
		}
				
		Preferences.setPreference(LoginActivity.this, Preferences.MY_CHANNEL_JID, myChannelJid);
		Preferences.setPreference(LoginActivity.this, Preferences.PASSWORD, passwordTxt);
		
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
						mProgressDialog.dismiss();
					}
				});
			}

			@Override
			public void error(Throwable throwable) {
				mProgressDialog.dismiss();
				Toast.makeText(
						getApplicationContext(),
						getString(R.string.login_error_wrong_credentials),
						Toast.LENGTH_LONG).show();
			}
			
			private void checkCredentials() {
				LoginModel.getInstance().getFromServer(LoginActivity.this, new ModelCallback<Void>() {
					@Override
					public void success(Void response) {
						mProgressDialog.dismiss();
						setResult(LOGGED_IN_RESULT);
						finish();
					}
					
					@Override
					public void error(Throwable throwable) {
						mProgressDialog.dismiss();
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.login_error_wrong_credentials),
								Toast.LENGTH_LONG).show();
					}
				});
			}
			
		}, myChannelJidSplit[1]);
		
		// remove all error tool tips
		hideAllErrorTooltips();

		//hide keyboard
		InputUtils.hideKeyboard(LoginActivity.this);
		
		// show progress dialog
		mProgressDialog.show();
	}

	private final TextWatcher mUserNameTxtWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == 0) {
				showErrorToolTip(mUsernameErrorTooltip,
						getString(R.string.message_account_username_mandatory));
			} else {
				mUsernameErrorTooltip.setVisibility(View.GONE);
			}
		}
	};

	private final TextWatcher mPasswordTxtWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == 0) {
				showErrorToolTip(mPasswordErrorTooltip,
						getString(R.string.message_account_password_mandatory));
			} else if (s.length() < 6) {
				showErrorToolTip(mPasswordErrorTooltip,
						getString(R.string.message_account_password_short_length));
			} else {
				mPasswordErrorTooltip.setVisibility(View.GONE);
			}
		}
	};
}
