package com.buddycloud;

import org.json.JSONObject;

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
import com.buddycloud.utils.ActionbarUtil;
import com.buddycloud.utils.InputUtils;

/**
 * This activity used to show the recover forgot password screen 
 * and handle all the actions.
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class RecoverPasswordActivity extends SherlockActivity {

	private static final String TAG = RecoverPasswordActivity.class.getSimpleName();
	
	private EditText mUsernameTxt;

	private TooltipErrorView mUsernameErrorTooltip;
	private ProgressDialog mProgressDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        ActionbarUtil.showActionBarwithBack(this, getString(R.string.forgot_password_title));

		mUsernameErrorTooltip = (TooltipErrorView) findViewById(R.id.usernameErrorTooltip);
		mUsernameTxt = (EditText) findViewById(R.id.usernameTxt);
		mUsernameTxt.addTextChangedListener(mUsernameTxtWatcher);
		
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setMessage(getString(R.string.message_reseting_password));
		mProgressDialog.setCancelable(false);

		mUsernameTxt.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    final String usernameTxt = getValue(R.id.usernameTxt);
                    recoverPassword(usernameTxt);
                }
                return false;
            }
        });
        
		final Button forgotPasswordBtn = (Button) findViewById(R.id.forgotPasswordBtn);
		forgotPasswordBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

                final String usernameTxt = getValue(R.id.usernameTxt);
                recoverPassword(usernameTxt);
			}
		});
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
	    switch (item.getItemId()) {
        	case android.R.id.home:
        		InputUtils.hideKeyboard(RecoverPasswordActivity.this);
        		finish();
        		return true;
        	default:
        		return super.onOptionsItemSelected(item);
        }
	}

	private void showErrorToolTip(TooltipErrorView errorView, String errorMsg) {

		if (errorView != null && !isEmpty(errorMsg)) {
			errorView.setText(errorMsg);
			errorView.setVisibility(View.VISIBLE);
		}
	}

	private void hideAllErrorTooltips() {

		if (mUsernameErrorTooltip != null) {
			mUsernameErrorTooltip.setVisibility(View.GONE);
		}
	}
	
	private static boolean isEmpty(String string) {
		return string.length() == 0;
	}

	private String getValue(int resId) {
		return ((EditText) findViewById(resId)).getText().toString();
	}
	
	/**
	 * Recover the password for the given username
	 * 
	 * @param usernameTxt
	 */
	private void recoverPassword(final String usernameTxt) {

        if (isEmpty(usernameTxt)) {
            showErrorToolTip(mUsernameErrorTooltip,
                    getString(R.string.message_account_username_mandatory));
            return;
        }

        String[] myUsernameSplit = usernameTxt.split("@");
        if (myUsernameSplit.length < 2) {
            showErrorToolTip(mUsernameErrorTooltip,
                    getString(R.string.forgot_password_error_bad_username_format));
            return;
        }
		
		// remove all error tool tips
		hideAllErrorTooltips();
		
		// show progress dialog
		mProgressDialog.show();
		
		// Recover the password for given user@example.com
		AccountModel.getInstance().resetPassword(getApplicationContext(),
                usernameTxt, new ModelCallback<JSONObject>() {
					@Override
					public void success(JSONObject response) {
						mProgressDialog.dismiss();
						Toast.makeText(getApplicationContext(), 
								getString(R.string.message_password_successfully_reset), 
								Toast.LENGTH_LONG).show();
						
						//hide keyboard
						InputUtils.hideKeyboard(RecoverPasswordActivity.this);
						
						//sent back to login screen
						finish();
					}

					@Override
					public void error(Throwable throwable) {
						mProgressDialog.dismiss();
						Toast.makeText(getApplicationContext(), 
								getString(R.string.message_password_reset_failed), 
								Toast.LENGTH_LONG).show();
					}
		});
	}

	private final TextWatcher mUsernameTxtWatcher = new TextWatcher() {

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
}
