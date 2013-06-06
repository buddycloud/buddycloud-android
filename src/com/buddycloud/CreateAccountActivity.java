package com.buddycloud;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class CreateAccountActivity extends Activity {

	public static final int REQUEST_CODE = 105;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_share, menu);
        return true;
    }
}
