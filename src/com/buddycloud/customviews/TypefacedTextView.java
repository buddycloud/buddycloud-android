package com.buddycloud.customviews;

import android.content.Context;
import android.content.res.TypedArray;
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

	private static final String FONTS_PATH = "fonts/";
	
    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Typeface.createFromAsset doesn't work in the layout editor. Skipping...
        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.Typefaced);
        String fontName = styledAttrs.getString(R.styleable.Typefaced_typeface);
        styledAttrs.recycle();

        if (fontName != null) {
        	// use typefaces cache to resolve memory leak issue
            setTypeface(TypefacesUtil.get(context, FONTS_PATH + fontName));
        }
    }

}
