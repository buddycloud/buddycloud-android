package com.buddycloud;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TouchImageView;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

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
		loadToTarget(IMAGE_URL_HIGH_RES);
	}

	private void loadToTarget(String key) {
		String imageURL = getIntent().getStringExtra(key);
		ImageHelper.picasso(getApplicationContext()).load(imageURL).into(new Target() {
			@Override
			public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
				imageView.setImageBitmap(arg0);
				imageView.forceLayout();
			}

			@Override
			public void onBitmapFailed(Drawable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPrepareLoad(Drawable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
}
