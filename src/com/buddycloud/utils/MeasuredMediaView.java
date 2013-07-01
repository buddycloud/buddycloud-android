package com.buddycloud.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MeasuredMediaView extends ImageView {

	private MeasureListener measureListener;
	
	public MeasuredMediaView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setMeasureListener(MeasureListener measureListener) {
		this.measureListener = measureListener;
		if (getWidth() > 0 || getHeight() > 0) {
			measureListener.measure(getWidth(), getHeight());
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (measureListener != null) {
			measureListener.measure(MeasureSpec.getSize(widthMeasureSpec), 
					MeasureSpec.getSize(heightMeasureSpec));
		}
	}
	
	public interface MeasureListener {
		void measure(int widthMeasureSpec, int heightMeasureSpec);
	}
}
