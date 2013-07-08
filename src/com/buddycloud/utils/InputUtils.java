package com.buddycloud.utils;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class InputUtils {

	public static void hideKeyboard(final Context context, final EditText replyTxt) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(replyTxt.getWindowToken(), 0);
	}

}
