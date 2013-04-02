package com.buddycloud.image;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class WebImage implements SmartImage {
    
    private static WebImageCache webImageCache;

    private String url;

    public WebImage(String url) {
        this.url = url;
    }
    
    public String getUrl() {
		return url;
	}
    
    public WebImageCache getCache(Context context) {
    	if(webImageCache == null) {
            webImageCache = new WebImageCache(context);
        }
    	return webImageCache;
	}

    public Bitmap getBitmap(Context context) {
        // Don't leak context
        webImageCache = getCache(context);

        // Try getting bitmap from cache first
        Bitmap bitmap = null;
        if(url != null) {
            bitmap = webImageCache.get(url);
            if(bitmap == null) {
            	if (webImageCache.isNotFound(url)) {
            		return null;
            	}
                bitmap = getBitmapFromUrl(url);
                if(bitmap != null){
                    webImageCache.put(url, bitmap);
                }
            }
        }

        return bitmap;
    }

    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;

        try {
            HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse responseGet = client.execute(get);
            
			if (responseGet.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				webImageCache.putNotFound(url);
				return null;
			}
			
			bitmap = BitmapFactory.decodeStream(responseGet.getEntity().getContent());
        } catch(Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static void removeFromCache(String url) {
        if(webImageCache != null) {
            webImageCache.remove(url);
        }
    }
}
