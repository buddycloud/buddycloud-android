package com.buddycloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.customviews.TooltipErrorView;
import com.buddycloud.http.SSLUtils;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.DNSUtils;
import com.buddycloud.utils.InputUtils;

/**
 * This activity used to show the create account screen 
 * and handle all the actions.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class CreateAccountActivity extends SherlockActivity {

	private static final String TAG = CreateAccountActivity.class.getSimpleName();

	private static final String DOMAIN_SUGGESTION_URL = "http://buddycloud.com/registration-helper/sign-up-domains.json";
	private static final String BUDDYCLOUD_DOMAIN = "buddycloud.org";

	public static final int REQUEST_CODE = 105;
	public static final int ACCOUNT_CREATED_RESULT = 205;

	private EditText mUsernameTxt;
	private EditText mPasswordTxt;
	private EditText mEmailAddressTxt;
	private TextView mCreateAccountCaption;

	private TooltipErrorView mUsernameErrorTooltip;
	private TooltipErrorView mPasswordErrorTooltip;
	private TooltipErrorView mEmailErrorTooltip;
	private ProgressDialog mProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.create_account_title));

		mUsernameErrorTooltip = (TooltipErrorView) findViewById(R.id.usernameErrorTooltip);
		mUsernameTxt = (EditText) findViewById(R.id.usernameTxt);
		mUsernameTxt.addTextChangedListener(mUserNameTxtWatcher);

		mPasswordErrorTooltip = (TooltipErrorView) findViewById(R.id.passwordErrorTooltip);
		mPasswordTxt = (EditText) findViewById(R.id.passwordTxt);
		mPasswordTxt.addTextChangedListener(mPasswordTxtWatcher);
		
		mEmailErrorTooltip = (TooltipErrorView) findViewById(R.id.emailErrorTooltip);
		mEmailAddressTxt = (EditText) findViewById(R.id.emailAddressTxt);
		mEmailAddressTxt.addTextChangedListener(mEmailAddressTxtWatcher);
		mEmailAddressTxt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
					final String accountNameTxt = getValue(R.id.usernameTxt);
					final String emailAddressTxt = getValue(R.id.emailAddressTxt);
					final String passwordTxt = getValue(R.id.passwordTxt);
					
					// resolve API service and create account.
					resolveAPISRVAndCreateAccount(accountNameTxt, emailAddressTxt, passwordTxt);
				}
				return false;
			}
		});
        
		mCreateAccountCaption = (TextView) findViewById(R.id.createAccountCaption);
		SpannableString hyperLinkSpan = new SpannableString(mCreateAccountCaption.getText().toString());
		hyperLinkSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.bc_green_blue)), 162, hyperLinkSpan.length() - 1, 0);
		hyperLinkSpan.setSpan(new UnderlineSpan(), 162, hyperLinkSpan.length() - 1, 0);
		hyperLinkSpan.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				String url = getString(R.string.buddycloud_website);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				startActivity(intent);
			}
		}, 162, hyperLinkSpan.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		mCreateAccountCaption.setText(hyperLinkSpan);
		mCreateAccountCaption.setMovementMethod(LinkMovementMethod.getInstance());
	    
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.message_account_creation));
		mProgressDialog.setCancelable(false);

		final Button createAccountBtn = (Button) findViewById(R.id.createAccountBtn);
		createAccountBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final String accountNameTxt = getValue(R.id.usernameTxt);
				final String emailAddressTxt = getValue(R.id.emailAddressTxt);
				final String passwordTxt = getValue(R.id.passwordTxt);

				// resolve API service and create account.
				resolveAPISRVAndCreateAccount(accountNameTxt, emailAddressTxt, passwordTxt);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(CreateAccountActivity.this);
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}

	@Override
	public void onAttachedToWindow() {
		// fillSuggestions();
	}

	/*
	private void fillSuggestions() {
		BuddycloudHTTPHelper.reqArrayNoSSL(DOMAIN_SUGGESTION_URL, this,
				new ModelCallback<JSONArray>() {

					@Override
					public void success(JSONArray response) {
						List<Domain> domains = new ArrayList<Domain>();
						for (int i = 0; i < response.length(); i++) {
							JSONObject domainJson = response.optJSONObject(i);
							domains.add(new Domain(domainJson));
						}

						 final DomainAdapter adapter = new DomainAdapter(
						 getApplicationContext(),
						 R.layout.domain_spinner_item,
						 domains);
						
						 AlertDialog.Builder builder = new
						 AlertDialog.Builder(CreateAccountActivity.this);
						 DialogInterface.OnClickListener dialogListener = new
						 DialogInterface.OnClickListener() {
						 public void onClick(DialogInterface dialog, int
						 which) {
						 Domain domain = adapter.getItem(which);
						 TextView domainTextView = (TextView)
						 findViewById(R.id.domainTxt);
						 domainTextView.setText(domain.toString());
						 dialog.dismiss();
						 }
						 };
						
						 builder.setTitle(getString(R.string.create_account_domain_hint));
						 builder.setAdapter(adapter, dialogListener);
						 builder.create().show();
					}

					@Override
					public void error(Throwable throwable) {
						// TODO Auto-generated method stub
					}
				});
	} */

	/**
	 * Resolve API Service and create new account
	 * 
	 */
	private void resolveAPISRVAndCreateAccount(final String accountNameTxt, final String passwordTxt,
			final String emailAddressTxt) {
		
		if (isEmpty(accountNameTxt)) {
			showErrorToolTip(mUsernameErrorTooltip,
					getString(R.string.message_account_username_mandatory));
			return;
		}

		if (isEmpty(passwordTxt)) {
			showErrorToolTip(mPasswordErrorTooltip,
					getString(R.string.message_account_password_mandatory));
			return;
		}

		if (!isValidEmail(emailAddressTxt)) {
			showErrorToolTip(mEmailErrorTooltip,
					getString(R.string.message_account_email_invalid));
			return;
		}

		final String bareJid = accountNameTxt + "@" + BUDDYCLOUD_DOMAIN;
		if (!isBareJid(bareJid)) {

			Toast.makeText(getApplicationContext(),
					getString(R.string.message_account_invalid_domain),
					Toast.LENGTH_LONG).show();
			return;
		}

		// resolve API service
		DNSUtils.resolveAPISRV(new ModelCallback<String>() {
			
			@Override
			public void success(final String apiAddress) {
				SSLUtils.checkSSL(getApplicationContext(), apiAddress,
						new ModelCallback<Void>() {
							@Override
							public void success(Void response) {
								createAccount(emailAddressTxt,
										passwordTxt, bareJid,
										apiAddress);
							}

							@Override
							public void error(Throwable throwable) {
								// Do nothing, SSL error not tolerable
								mProgressDialog.hide();
							}
						});
			}

			@Override
			public void error(Throwable throwable) {
				mProgressDialog.hide();
				Toast.makeText(
						getApplicationContext(),
						getString(R.string.message_api_discovery_failed),
						Toast.LENGTH_LONG).show();
				return;
			}
		}, BUDDYCLOUD_DOMAIN);

		// remove all error tooltips
		hideAllErrorTooltips();

		//hide keyboard
		InputUtils.hideKeyboard(CreateAccountActivity.this);
		
		// show progress dialog
		mProgressDialog.show();
	}
	
	/**
	 * Create new account
	 * 
	 * @param emailAddressTxt
	 * @param passwordTxt
	 * @param bareJid
	 * @param apiAddress
	 */
	private void createAccount(final String emailAddressTxt,
			final String passwordTxt, final String bareJid,
			final String apiAddress) {
		Map<String, String> accountInfo = new HashMap<String, String>();
		accountInfo.put("username", bareJid);
		accountInfo.put("password", passwordTxt);
		accountInfo.put("email", emailAddressTxt);
		AccountModel.getInstance().save(getApplicationContext(),
				new JSONObject(accountInfo), new ModelCallback<JSONObject>() {

					@Override
					public void success(JSONObject response) {
						mProgressDialog.hide();
						Preferences.setPreference(getApplicationContext(),
								Preferences.MY_CHANNEL_JID, bareJid);
						Preferences.setPreference(getApplicationContext(),
								Preferences.PASSWORD, passwordTxt);
						Preferences.setPreference(getApplicationContext(),
								Preferences.API_ADDRESS, apiAddress);
						setResult(ACCOUNT_CREATED_RESULT);
						finish();
					}

					@Override
					public void error(Throwable throwable) {
						mProgressDialog.hide();
						showErrorToolTip(mUsernameErrorTooltip,
								getString(R.string.message_account_creation_failed));

						Toast.makeText(
								getApplicationContext(),
								getString(R.string.message_account_creation_failed),
								Toast.LENGTH_LONG).show();
					}
				}, apiAddress);
	}
	
	private void showErrorToolTip(TooltipErrorView errorView, String errorMsg) {

		if (errorView != null && !isEmpty(errorMsg)) {
			errorView.setText(errorMsg);
			errorView.setVisibility(View.VISIBLE);
		}
	}

	private void hideAllErrorTooltips() {

		if (mUsernameErrorTooltip != null && mPasswordErrorTooltip != null
				&& mEmailErrorTooltip != null) {

			mUsernameErrorTooltip.setVisibility(View.GONE);
			mPasswordErrorTooltip.setVisibility(View.GONE);
			mEmailErrorTooltip.setVisibility(View.GONE);
		}
	}

	private static boolean isEmpty(String string) {
		return string.length() == 0;
	}

	private static boolean isValidEmail(String email) {
		
		final String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
		return (!isEmpty(email) && email.matches(EMAIL_REGEX));
	}
	
	public static boolean isBareJid(CharSequence target) {
		return Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}

	private String getValue(int resId) {
		return ((EditText) findViewById(resId)).getText().toString();
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

		public DomainAdapter(Context context, int textViewResourceId,
				List<Domain> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getCustomView(position, convertView, parent);
		}

		public View getCustomView(int position, View convertView,
				ViewGroup parent) {

			Domain domain = getItem(position);

			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.domain_spinner_item, parent,
					false);
			TextView label = (TextView) row.findViewById(R.id.textHeader);
			label.setText(domain.getJSON().optString("domain"));

			TextView sub = (TextView) row.findViewById(R.id.textSub);
			sub.setText(domain.getJSON().optString("name"));

			return row;
		}
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

	private final TextWatcher mEmailAddressTxtWatcher = new TextWatcher() {

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
				showErrorToolTip(mEmailErrorTooltip,
						getString(R.string.message_account_email_invalid));
			} else {
				mEmailErrorTooltip.setVisibility(View.GONE);
			}
		}
	};
}
