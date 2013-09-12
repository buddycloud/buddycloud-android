package com.buddycloud.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.buddycloud.R;

public class TypefacedTextView extends TextView {

	private static final String FONTS_PATH = "fonts/";
	
    public TypefacedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //Typeface.createFromAsset doesn't work in the layout editor. Skipping...
        if (isInEditMode()) {
            return;
        }

        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
        String fontName = styledAttrs.getString(R.styleable.TypefacedTextView_typeface);
        styledAttrs.recycle();

        if (fontName != null) {
        	// use typefaces cache to resolve memory leak issue
            setTypeface(Typefaces.get(context, FONTS_PATH + fontName));
        }
    }

}
