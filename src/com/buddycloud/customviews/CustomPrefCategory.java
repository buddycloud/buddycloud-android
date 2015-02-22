package com.buddycloud.customviews;

import com.buddycloud.R;
import com.buddycloud.utils.TypefacesUtil;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class CustomPrefCategory extends PreferenceCategory {

	private static final String FONTS_PATH = "fonts/";
	private static final String FONT_NAME = "Roboto-Bold.ttf";
	
	public CustomPrefCategory(Context context) {
		super(context);
	}

	public CustomPrefCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomPrefCategory(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		final Context context = getContext(); 
		TextView titleView = (TextView) view.findViewById(android.R.id.title);
		titleView.setTextColor(context.getResources().getColor(R.color.bc_green_blue_color));
		
		int fontSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 18, context.getResources().getDisplayMetrics());
		titleView.setTextSize(fontSize);
		
		Typeface font = TypefacesUtil.get(context, FONTS_PATH + FONT_NAME);
		if (font != null) {
			titleView.setTypeface(font);
		}
	}
}
