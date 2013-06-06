package com.buddycloud;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;

public class CreateAccountActivity extends Activity {

	public static final int REQUEST_CODE = 105;
	public static final int ACCOUNT_CREATED_RESULT = 205;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        
        final RelativeLayout createAccountBtn = (RelativeLayout) findViewById(R.id.createAccountBtn);
        createAccountBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String accountNameTxt = getValue(R.id.loginTxt);
				final String domainTxt = getValue(R.id.domainTxt);
				final String emailAddressTxt = getValue(R.id.emailAddressTxt);
				final String repeatPasswordTxt = getValue(R.id.repeatPasswordTxt);
				final String passwordTxt = getValue(R.id.passwordTxt);
				
				if (isEmpty(accountNameTxt) || isEmpty(domainTxt) || 
						isEmpty(emailAddressTxt) || isEmpty(repeatPasswordTxt) || isEmpty(passwordTxt)) {
					Toast.makeText(getApplicationContext(), 
							"All fields are mandatory.", Toast.LENGTH_LONG).show();
					return;
				}
				
				if (!passwordTxt.equals(repeatPasswordTxt)) {
					Toast.makeText(getApplicationContext(), 
							"Your password and confirmation password do not match.", Toast.LENGTH_LONG).show();
					return;
				}
				
				final String bareJid = accountNameTxt + "@" + domainTxt;
				if (!isBareJid(bareJid)) {
					Toast.makeText(getApplicationContext(), 
							"Domain name is not valid.", Toast.LENGTH_LONG).show();
					return;
				}
				
				DNSUtils.resolveAPISRV(new ModelCallback<String>() {
					@Override
					public void success(String apiAddress) {
						createAccount(emailAddressTxt, passwordTxt, bareJid, apiAddress);
					}
					
					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getApplicationContext(), 
								"Could not find an API for this domain.", Toast.LENGTH_LONG).show();
						return;
					}
				}, domainTxt);
			}

		});
        
    }

    private static boolean isEmpty(String string) {
    	return string.length() == 0;
    }
    
    public static boolean isBareJid(CharSequence target) {
    	return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    
    private String getValue(int resId) {
    	return ((EditText) findViewById(resId)).getText().toString();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

	private void createAccount(final String emailAddressTxt,
			final String passwordTxt, final String bareJid, final String apiAddress) {
		Map<String, String> accountInfo = new HashMap<String, String>();
		accountInfo.put("username", bareJid);
		accountInfo.put("password", passwordTxt);
		accountInfo.put("email", emailAddressTxt);
		AccountModel.getInstance().save(getApplicationContext(), new JSONObject(accountInfo), 
				new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						Preferences.setPreference(getApplicationContext(), Preferences.MY_CHANNEL_JID, bareJid);
						Preferences.setPreference(getApplicationContext(), Preferences.PASSWORD, passwordTxt);
						Preferences.setPreference(getApplicationContext(), Preferences.API_ADDRESS, apiAddress);
						setResult(ACCOUNT_CREATED_RESULT);
						finish();
					}

					@Override
					public void error(Throwable throwable) {
						Toast.makeText(
								getApplicationContext(),
								"Could not register account. Account unavailable.",
								Toast.LENGTH_LONG).show();
					}
				}, apiAddress);
	}
}
