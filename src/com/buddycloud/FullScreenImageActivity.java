package com.buddycloud;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.buddycloud.customviews.TouchImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FullScreenImageActivity extends SherlockFragmentActivity {

	public static final int REQUEST_CODE = 121;
	public static final String IMAGE_URL = "com.buddycloud.IMAGE_URL";
	public static final String IMAGE_URL_HIGH_RES = "com.buddycloud.IMAGE_URL_HIGH_RES";
	private TouchImageView imageView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.imageView = new TouchImageView(getApplicationContext());
        imageView.setMaxZoom(4f);
        setContentView(imageView);
        loadToTarget(IMAGE_URL);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		ImageLoader.getInstance().resume();
		loadToTarget(IMAGE_URL_HIGH_RES);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		ImageLoader.getInstance().stop();
	}

	private void loadToTarget(String key) {
		String imageURL = getIntent().getStringExtra(key);
		ImageLoader.getInstance().displayImage(imageURL, imageView);
	}
}
