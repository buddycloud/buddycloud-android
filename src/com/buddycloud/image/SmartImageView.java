package com.buddycloud.image;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.buddycloud.image.SmartImageTask.OnCompleteListener;

public class SmartImageView extends ImageView {
    private static final int LOADING_THREADS = 10;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(LOADING_THREADS);

    private SmartImageTask currentTask;

    public SmartImageView(Context context) {
        super(context);
    }

    public SmartImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmartImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Helpers to set image by URL
    public void setImageUrl(String url) {
        setImage(new WebImage(url), null, null, null, null);
    }

    public void setImageUrl(String url, OnCompleteListener completeListener) {
        setImage(new WebImage(url), null, null, completeListener, null);
    }

    public void setImageUrl(String url, final Integer fallbackResource) {
        setImage(new WebImage(url), fallbackResource, fallbackResource, null, null);
    }

    public void setImageUrlWithAnimation(String url, final Integer fallbackResource, final Integer animationResource) {
        setImage(new WebImage(url), fallbackResource, fallbackResource, null, animationResource);
    }
    
    public void setImageUrlWithAnimation(String url, final Integer fallbackResource, final OnCompleteListener completeListener, final Integer animationResource) {
        setImage(new WebImage(url), fallbackResource, fallbackResource, completeListener, animationResource);
    }
    
    public void setImageUrl(String url, final Integer fallbackResource, OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, fallbackResource, completeListener, null);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource) {
        setImage(new WebImage(url), fallbackResource, loadingResource, null, null);
    }

    public void setImageUrl(String url, final Integer fallbackResource, final Integer loadingResource, OnCompleteListener completeListener) {
        setImage(new WebImage(url), fallbackResource, loadingResource, completeListener, null);
    }

    public void setImage(final WebImage image, final Integer fallbackResource, final Integer loadingResource, final OnCompleteListener completeListener, final Integer animationResource) {
        
    	Animation animation = null;
    	
    	// Set a loading resource
        if(loadingResource != null){
        	setImageResource(loadingResource);
        }
        
        if (animationResource != null) {
        	animation = AnimationUtils.loadAnimation(getContext(), animationResource);
        	startAnimation(animation);
        	animation.reset();
        	animation.start();
        }

        // Cancel any existing tasks for this image view
        if(currentTask != null) {
            currentTask.cancel();
            currentTask = null;
        }
        
        WebImageCache cache = image.getCache(getContext());
		Bitmap bitmapFromMemory = cache.getBitmapFromMemory(image.getUrl());
		if (bitmapFromMemory != null) {
			onComplete(fallbackResource, completeListener, bitmapFromMemory, animation);
			return;
		}
        
        // Set up the new task
		final Animation finalAnimation = animation;
        currentTask = new SmartImageTask(getContext(), image);
        currentTask.setOnCompleteHandler(new SmartImageTask.OnCompleteHandler() {
            @Override
            public void onComplete(final Bitmap bitmap) {
            	post(new Runnable() {
					@Override
					public void run() {
						SmartImageView.this.onComplete(fallbackResource, 
								completeListener, bitmap, finalAnimation);
					}
            	});
            }
        });

        // Run the task in a threadpool
        threadPool.execute(currentTask);
    }

	private void onComplete(Integer fallbackResource, OnCompleteListener completeListener, 
			Bitmap bitmap, Animation animation) {
		if (animation != null) {
			animation.cancel();
		}
		if (bitmap != null) {
			setImageBitmap(bitmap);
		} else {
			if (fallbackResource != null) {
				setImageResource(fallbackResource);
			}
		}
		if (completeListener != null) {
			completeListener.onComplete();
		}
	}

    public static void cancelAllTasks() {
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(LOADING_THREADS);
    }
}