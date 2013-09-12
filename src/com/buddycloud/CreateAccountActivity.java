package com.buddycloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buddycloud.http.BuddycloudHTTPHelper;
import com.buddycloud.http.SSLUtils;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;

public class CreateAccountActivity extends Activity {

	private static final String DOMAIN_SUGGESTION_URL = "http://buddycloud.com/registration-helper/sign-up-domains.json";
	public static final int REQUEST_CODE = 105;
	public static final int ACCOUNT_CREATED_RESULT = 205;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        
        final AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.domainTxt);
        
        actv.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					actv.showDropDown();
				}
			}
		});
        
        fillSuggestions(actv);
        
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
							getString(R.string.message_account_fields_mandatory),
							Toast.LENGTH_LONG).show();
					return;
				}
				
				if (!passwordTxt.equals(repeatPasswordTxt)) {
					Toast.makeText(getApplicationContext(), 
							getString(R.string.message_account_passwords_donot_match),
							Toast.LENGTH_LONG).show();
					return;
				}
				
				final String bareJid = accountNameTxt + "@" + domainTxt;
				if (!isBareJid(bareJid)) {
					Toast.makeText(getApplicationContext(), 
							getString(R.string.message_account_invalid_domain),
							Toast.LENGTH_LONG).show();
					return;
				}
				
				DNSUtils.resolveAPISRV(new ModelCallback<String>() {
					@Override
					public void success(final String apiAddress) {
						SSLUtils.checkSSL(getApplicationContext(), apiAddress, 
								new ModelCallback<Void>() {
									@Override
									public void success(Void response) {
										createAccount(emailAddressTxt, passwordTxt, 
												bareJid, apiAddress);
									}

									@Override
									public void error(Throwable throwable) {
										// Do nothing, SSL error not tolerable
									}
						});
					}
					
					@Override
					public void error(Throwable throwable) {
						Toast.makeText(getApplicationContext(), 
								getString(R.string.message_api_discovery_failed),
								Toast.LENGTH_LONG).show();
						return;
					}
				}, domainTxt);
			}

		});
        
    }

	private void fillSuggestions(final AutoCompleteTextView actv) {
		BuddycloudHTTPHelper.getArray(
        		DOMAIN_SUGGESTION_URL, false, true, this, 
        		new ModelCallback<JSONArray>() {

					@Override
					public void success(JSONArray response) {
						List<Domain> domains = new ArrayList<Domain>();
						for (int i = 0; i < response.length(); i++) {
							JSONObject domainJson = response.optJSONObject(i);
							domains.add(new Domain(domainJson));
						}
						
						DomainAdapter adapter = new DomainAdapter(
								getApplicationContext(), 
								R.layout.domain_spinner_item, 
								domains);
						
						actv.setAdapter(adapter);
						actv.setThreshold(256);
						actv.showDropDown();
					}

					@Override
					public void error(Throwable throwable) {
						// TODO Auto-generated method stub
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
								getString(R.string.message_account_creation_failed),
								Toast.LENGTH_LONG).show();
					}
				}, apiAddress);
	}
	
	private static class Domain {
		
		private JSONObject domainObject;

		public Domain(JSONObject domainObject) {
			this.domainObject = domainObject;
		}
		
		JSONObject getJSON() {
			return domainObject;
		}
		
		@Override
		public String toString() {
			return domainObject.optString("domain");
		}
	}
	
	private class DomainAdapter extends ArrayAdapter<Domain> {
		 
        public DomainAdapter(Context context, int textViewResourceId, List<Domain> objects) {
            super(context, textViewResourceId, objects);
        }
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	return getCustomView(position, convertView, parent);
        }
        
        public View getCustomView(int position, View convertView, ViewGroup parent) {
 
        	Domain domain = getItem(position);
        	
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.domain_spinner_item, parent, false);
			TextView label = (TextView) row.findViewById(R.id.textHeader);
			label.setText(domain.getJSON().optString("domain"));

			TextView sub = (TextView) row.findViewById(R.id.textSub);
			sub.setText(domain.getJSON().optString("name"));

			return row;
        }
   }
}
