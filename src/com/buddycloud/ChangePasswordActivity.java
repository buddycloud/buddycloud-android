package com.buddycloud;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.customviews.TooltipErrorView;
import com.buddycloud.model.AccountModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.InputUtils;
import com.buddycloud.utils.TextUtils;

public class ChangePasswordActivity extends SherlockActivity {

	private TooltipErrorView mNewPasswordErrorTooltip;
	private TooltipErrorView mRepeatNewPasswordErrorTooltip;
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		
		ActionbarUtil.showActionBarwithBack(this, getString(R.string.acct_change_password_title));
		
		final EditText changePasswordTxt = (EditText)findViewById(R.id.changePasswordTxt);
		changePasswordTxt.setEnabled(false);
		
		String passwordPref = Preferences.getPreference(getApplicationContext(), Preferences.PASSWORD);
		changePasswordTxt.setText(passwordPref);
		
		final EditText newPasswordTxt = (EditText)findViewById(R.id.newPasswordTxt);
		newPasswordTxt.addTextChangedListener(mNewPasswordTxtWatcher);
		mNewPasswordErrorTooltip = (TooltipErrorView)findViewById(R.id.newPasswordErrorTooltip);
		
		final EditText repeatNewPasswordTxt = (EditText)findViewById(R.id.repeatNewPasswordTxt);
		repeatNewPasswordTxt.addTextChangedListener(mRepeatNewPasswordTxtWatcher);
		mRepeatNewPasswordErrorTooltip = (TooltipErrorView)findViewById(R.id.repeatNewPasswordErrorTooltip);
		
		repeatNewPasswordTxt.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					
					final String currentPassword = changePasswordTxt.getText().toString();
					final String newPassword = newPasswordTxt.getText().toString();
					final String repeatNewPassword = repeatNewPasswordTxt.getText().toString();
					
					// change password
					changePassword(currentPassword, newPassword, repeatNewPassword);
				}
				return false;
			}
		});
		
		final Button changePasswordBtn = (Button)findViewById(R.id.changePasswordBtn);
		changePasswordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final String currentPassword = changePasswordTxt.getText().toString();
				final String newPassword = newPasswordTxt.getText().toString();
				final String repeatNewPassword = repeatNewPasswordTxt.getText().toString();
				
				// change password
				changePassword(currentPassword, newPassword, repeatNewPassword);
			}
		});
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.acct_message_changing_password));
		mProgressDialog.setCancelable(false);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(ChangePasswordActivity.this);
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}

	private void changePassword(final String oldPassword, final String newPassword, 
			final String repeatNewPassword) {
		
		if (TextUtils.isEmpty(newPassword)) {
			showErrorToolTip(mNewPasswordErrorTooltip,
					getString(R.string.acct_message_new_password_mandatory));
			return;
		}

		if (TextUtils.isEmpty(repeatNewPassword)) {
			showErrorToolTip(mRepeatNewPasswordErrorTooltip,
					getString(R.string.acct_message_repeat_new_password_mandatory));
			return;
		}
		
		if (isValidPassword(oldPassword, newPassword, repeatNewPassword)) {
			
			// remove all error tooltips
			hideAllErrorTooltips();

			//hide keyboard
			InputUtils.hideKeyboard(ChangePasswordActivity.this);
			
			// show progress dialog
			mProgressDialog.show();
			
			AccountModel.getInstance().changePassword(getApplicationContext(), 
					oldPassword, newPassword, 
			new ModelCallback<Void>() {
				@Override
				public void success(Void response) {
					mProgressDialog.dismiss();
					Preferences.setPreference(getApplicationContext(), 
							Preferences.PASSWORD, newPassword);
					Toast.makeText(getApplicationContext(), getString(
							R.string.acct_message_change_password_success), 
							Toast.LENGTH_SHORT).show();
					finish();
				}

				@Override
				public void error(Throwable throwable) {
					mProgressDialog.dismiss();
					Toast.makeText(getApplicationContext(), getString(
							R.string.acct_message_change_password_failed), 
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	private void showErrorToolTip(TooltipErrorView errorView, String errorMsg) {

		if (errorView != null && !TextUtils.isEmpty(errorMsg)) {
			errorView.setText(errorMsg);
			errorView.setVisibility(View.VISIBLE);
		}
	}

	private void hideAllErrorTooltips() {

		if (mNewPasswordErrorTooltip != null && 
				mRepeatNewPasswordErrorTooltip != null) {

			mNewPasswordErrorTooltip.setVisibility(View.GONE);
			mRepeatNewPasswordErrorTooltip.setVisibility(View.GONE);
		}
	}
	
	private boolean isValidPassword(final String oldPassword, final String newPassword, 
			final String repeatNewPassword) {
		
		if (oldPassword != null && oldPassword.equalsIgnoreCase(newPassword)) {
			showErrorToolTip(mNewPasswordErrorTooltip,
					getString(R.string.acct_message_new_and_current_password_not_match));
			return false;
		}
		
		if (!newPassword.equalsIgnoreCase(repeatNewPassword)) {
			showErrorToolTip(mRepeatNewPasswordErrorTooltip,
					getString(R.string.acct_message_new_and_repeat_password_not_match));
			return false;
		}
		
		return true;
	}
	
	private final TextWatcher mNewPasswordTxtWatcher = new TextWatcher() {

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
			final EditText currentPasswordTxt = (EditText)findViewById(R.id.changePasswordTxt);
			final String currentPassword = currentPasswordTxt.getText().toString();
					
			if (s.length() == 0) {
				showErrorToolTip(mNewPasswordErrorTooltip,
						getString(R.string.acct_message_new_password_mandatory));
			} else if (s.length() < 6) {
				showErrorToolTip(mNewPasswordErrorTooltip,
						getString(R.string.message_account_password_short_length));
			} else if (!TextUtils.isEmpty(currentPassword) && currentPassword.equalsIgnoreCase(s.toString())) {
				showErrorToolTip(mNewPasswordErrorTooltip,
						getString(R.string.acct_message_new_and_current_password_not_match));
			} 
			else {
				mNewPasswordErrorTooltip.setVisibility(View.GONE);
			}
		}
	};
	
	private final TextWatcher mRepeatNewPasswordTxtWatcher = new TextWatcher() {

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
			final EditText newPasswordTxt = (EditText)findViewById(R.id.newPasswordTxt);
			final String newPassword = newPasswordTxt.getText().toString();
			
			if (s.length() == 0) {
				showErrorToolTip(mRepeatNewPasswordErrorTooltip,
						getString(R.string.acct_message_repeat_new_password_mandatory));
			} else if (s.length() < 6) {
				showErrorToolTip(mRepeatNewPasswordErrorTooltip,
						getString(R.string.message_account_password_short_length));
			} else if (!TextUtils.isEmpty(newPassword) && !newPassword.equalsIgnoreCase(s.toString())) {
				showErrorToolTip(mRepeatNewPasswordErrorTooltip,
						getString(R.string.acct_message_new_and_repeat_password_not_match));
			} else {
				mRepeatNewPasswordErrorTooltip.setVisibility(View.GONE);
			}
		}
	};
}
