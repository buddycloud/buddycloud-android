package com.buddycloud;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TouchImageView;
import com.squareup.picasso.Target;

public class FullScreenImageActivity extends SherlockFragmentActivity {

	public static final int REQUEST_CODE = 121;
	public static final String IMAGE_URL = "com.buddycloud.IMAGE_URL";
	private TouchImageView imageView;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.imageView = new TouchImageView(getApplicationContext());
        imageView.setMaxZoom(4f);
        setContentView(imageView);
    }
	
	@Override
	public void onAttachedToWindow() {
		String imageURL = getIntent().getStringExtra(IMAGE_URL);
		ImageHelper.picasso(this).load(imageURL).fetch(new Target() {
			@Override
			public void onSuccess(Bitmap arg0) {
				imageView.setImageBitmap(arg0);
			}
			
			@Override
			public void onError() {}
		});
	}
}
