package com.buddycloud.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.utils.TypefacesUtil;

/**
 * Create a textview with custom typeface.
 * 
 * <code>
 * 		<com.buddycloud.customviews.TypefacedTextView
 *       	android:id="@+id/createAccountBtn"
 *       	android:text="@string/create_account_button"
 *       	buddycloud:typeface="Roboto-Regular.ttf" />
 * </code>
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class TypefacedTextView extends TextView {

	private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

	private static final String FONTS_PATH = "fonts/";
	
	public TypefacedTextView(Context context) {
		super(context);
	}

	public TypefacedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		applyFonts(context, attrs);
	}

	public TypefacedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		applyFonts(context, attrs);
	}

	private void applyFonts(Context context, AttributeSet attrs) {

		// Get our custom attributes
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.Typefaced, 0, 0);

		try {
			String fontName = a.getString(R.styleable.Typefaced_typeface);
			if (!isInEditMode() && !TextUtils.isEmpty(fontName)) {
				Typeface typeface = sTypefaceCache.get(fontName);

				if (typeface == null) {
					typeface = TypefacesUtil.get(context, FONTS_PATH + fontName);

					// Cache the Typeface object
					sTypefaceCache.put(fontName, typeface);
				}
				setTypeface(typeface);

				// Note: This flag is required for proper typeface rendering
				setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
			}
		} finally {
			a.recycle();
		}
	}

}
