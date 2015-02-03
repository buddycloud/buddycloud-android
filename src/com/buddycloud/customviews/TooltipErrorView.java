package com.buddycloud.customviews;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.utils.TypefacesUtil;

/**
 * Create custom error tooltip view. It offers different
 * set of attributes that can help to customize the tooltip 
 * view.
 * 
 * <code>
 *       <com.buddycloud.customviews.TooltipErrorView
 *           android:id="@+id/passwordErrorTooltip"
 *           android:layout_width="fill_parent"
 *           android:layout_height="wrap_content"
 *           tooltipErrorView:bgColor="#f6f6f6"
 *           tooltipErrorView:borderColor="#d5d5d5"
 *           tooltipErrorView:fontTypeface="Roboto-Light.ttf"
 *           tooltipErrorView:textColor="#768595" />
 * </code>
 * 
 * @author Adnan Urooj (Deminem)
 * 
 */
public class TooltipErrorView extends LinearLayout implements
		ViewTreeObserver.OnPreDrawListener {

	private static final String FONTS_PATH = "fonts/";

	private static final int POINTER_HEIGHT = 6;

	private static final int POINTER_WIDE_HEIGHT = 12;

	private static final int POINTER_START = 35;

	private ViewGroup mContentHolder;
	private TextView mToolTipTV;

	private CharSequence mText;
	private int mColor;
	private int mBorderColor;
	private int mTextColor;
	private int mTextSize;
	private Typeface mTypeface;
	
	private int mPointHeightPx;

	public TooltipErrorView(final Context context) {
		super(context);
		init(context, null, 0);
	}

	public TooltipErrorView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	private void init(final Context context, final AttributeSet attrs,
			final int defStyle) {

		setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		setOrientation(VERTICAL);

		LayoutInflater.from(getContext()).inflate(R.layout.tooltip, this, true);
		mContentHolder = (ViewGroup) findViewById(R.id.tooltip_contentholder);
		mToolTipTV = (TextView) findViewById(R.id.tooltip_contenttv);

		Resources r = getContext().getResources();
		mPointHeightPx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, POINTER_HEIGHT,
				r.getDisplayMetrics());
		
		
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.TooltipErrorView, 0, 0);
		try {

			mText = a.getString(R.styleable.TooltipErrorView_text);
			mTypeface = getTypeface(context,
					a.getString(R.styleable.TooltipErrorView_fontTypeface));
			mTextSize = a.getInt(R.styleable.TooltipErrorView_textSize, 18);
			mTextColor = a.getColor(R.styleable.TooltipErrorView_textColor,
					Color.parseColor("#72828C"));
			mColor = a.getColor(R.styleable.TooltipErrorView_bgColor,
					Color.parseColor("#f6f6f6"));
			mBorderColor = a.getColor(R.styleable.TooltipErrorView_borderColor,
					Color.parseColor("#d4d4d4"));
		} finally {
			a.recycle();
		}

		// setup the config for tooltip
		setupToolTip();
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public boolean onPreDraw() {

		mContentHolder.measure(getMeasuredWidth(), getTextHeight() + mPointHeightPx);

		Shape shape = getTooltipShape();
		ShapeDrawable d = new ShapeDrawable(shape);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mContentHolder.setBackground(d);
		} else {
			mContentHolder.setBackgroundDrawable(d);
		}

		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int w = getMeasuredWidth();
		int h = getMeasuredHeight();

		w -= getPaddingLeft() - getPaddingRight();
		h = getTextHeight() + getPaddingTop() + getPaddingBottom()
				+ mPointHeightPx;

		setMeasuredDimension(w, h);
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);

		mContentHolder.setVisibility(visibility);
		mToolTipTV.setVisibility(visibility);

		invalidate();
	}

	/**
	 * Setup the error tooltip
	 * 
	 * @param text
	 */
	private void setupToolTip() {

		if (!TextUtils.isEmpty(mText)) {
			mToolTipTV.setText(mText);
		}

		mToolTipTV.setTypeface(mTypeface);
		mToolTipTV.setTextColor(mTextColor);
		mToolTipTV.setTextSize(mTextSize);

		getViewTreeObserver().addOnPreDrawListener(this);
	}

	/**
	 * Get the typeface for given font
	 * 
	 * @param context
	 * @param fontName
	 * @return
	 */
	private Typeface getTypeface(Context context, String fontName) {

		if (!TextUtils.isEmpty(fontName)) {
			return TypefacesUtil.get(context, FONTS_PATH + fontName);
		}

		return Typeface.SANS_SERIF;
	}

	private int getTextHeight() {

		mToolTipTV.setText(mText);
		mToolTipTV.measure(MeasureSpec.makeMeasureSpec(
				mContentHolder.getWidth(), MeasureSpec.EXACTLY), MeasureSpec
				.makeMeasureSpec(LayoutParams.WRAP_CONTENT,
						MeasureSpec.UNSPECIFIED));

		return mToolTipTV.getMeasuredHeight();
	}

	/**
	 * Set the tooltip text
	 * 
	 * @param msg
	 */
	public void setText(CharSequence msg) {

		if (!TextUtils.isEmpty(msg) && mText != msg) {
			mText = msg;
			mToolTipTV.setText(mText);

			requestLayout();
			invalidate();
		}
	}

	/**
	 * Set the tooltip text color
	 * 
	 * @param color
	 */
	public void setTextColor(int color) {

		if (mTextColor != color) {
			mTextColor = color;
			mToolTipTV.setTextColor(color);

			invalidate();
		}
	}

	/**
	 * Set the tooltip border color
	 * 
	 * @param color
	 */
	public void setBorderColor(int color) {

		if (mBorderColor != color) {
			mBorderColor = color;

			invalidate();
		}
	}

	/**
	 * Set the tooltip background color
	 * 
	 * @param color
	 */
	public void setBackgroundColor(int color) {

		if (mColor != color) {
			mColor = color;

			invalidate();
		}
	}

	/**
	 * Set the tooltip typeface
	 * 
	 * @param color
	 */
	public void setTypeface(Typeface typeface) {

		if (mTypeface != typeface) {
			mTypeface = typeface;

			invalidate();
		}
	}

	/**
	 * Get the error tooltip text
	 * 
	 * @return
	 */
	public CharSequence getText() {
		return mText;
	}

	/**
	 * Get the tooltip background color
	 * 
	 * @return
	 */
	public int getColor() {
		return mColor;
	}

	/**
	 * Get the text color
	 * 
	 * @return
	 */
	public int getTextColor() {
		return mTextColor;
	}

	/**
	 * Get the typeface
	 * 
	 * @return
	 */
	public Typeface getTypeface() {
		return mTypeface;
	}

	/**
	 * Get the tooltip shape
	 * 
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private Shape getTooltipShape() {

		Resources r = this.getContext().getResources();
		final int pointHeightPx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, POINTER_HEIGHT,
				r.getDisplayMetrics());
		final int pointedHeightPx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, POINTER_WIDE_HEIGHT,
				r.getDisplayMetrics());
		final int pointStartPx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, POINTER_START,
				r.getDisplayMetrics());

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContentHolder
				.getLayoutParams();
		mToolTipTV.setY(pointHeightPx);
		params.height = mToolTipTV.getHeight() + pointHeightPx;

		final Path rectPath = new Path();
		final Path rectBorderPath = new Path();

		// Create the rectangular shape
		Shape shape = new RectShape() {

			@Override
			protected void onResize(float w, float h) {
				createShapePath(rectPath, rectBorderPath, w, h, pointHeightPx,
						pointedHeightPx, pointStartPx);
			}

			@Override
			public void draw(Canvas canvas, Paint paint) {
				drawOnCanvas(canvas, paint, rectPath, rectBorderPath);
			}
		};
		return shape;
	}

	/**
	 * Create the shape path
	 * 
	 * @param rectPath
	 * @param rectBorderPath
	 * @param width
	 * @param height
	 * @param pointHeightPx
	 * @param pointedHeightPx
	 * @param pointStartPx
	 */
	private void createShapePath(Path rectPath, Path rectBorderPath,
			float width, float height, int pointHeightPx, int pointedHeightPx,
			int pointStartPx) {

		int w = (int) width;
		int h = (int) height;

		Point a = new Point(0, h);
		Point b = new Point(w, h);
		Point c = new Point(w, pointHeightPx);
		Point d = new Point((w - (w - pointStartPx)) + (pointedHeightPx / 2),
				pointHeightPx);
		Point e = new Point((w - (w - pointStartPx)), 0); // this is the sharp
															// point of the
															// triangle
		Point f = new Point((w - (w - pointStartPx)) - (pointedHeightPx / 2),
				pointHeightPx);
		Point g = new Point(0, pointHeightPx);

		rectPath.reset();
		rectPath.moveTo(a.x, a.y);
		rectPath.lineTo(b.x, b.y);
		rectPath.lineTo(c.x, c.y);
		rectPath.lineTo(d.x, d.y);
		rectPath.lineTo(e.x, e.y);
		rectPath.lineTo(f.x, f.y);
		rectPath.lineTo(g.x, g.y);
		rectPath.close();

		rectBorderPath.reset();
		rectBorderPath.moveTo(a.x, a.y);
		rectBorderPath.lineTo(b.x, b.y);
		rectBorderPath.lineTo(c.x, c.y);
		rectBorderPath.lineTo(d.x, d.y);
		rectBorderPath.lineTo(e.x, e.y);
		rectBorderPath.lineTo(f.x, f.y);
		rectBorderPath.lineTo(g.x, g.y);
		rectBorderPath.close();
	}

	/**
	 * Draw the shape on canvas
	 * 
	 * @param c
	 * @param p
	 * @param rectPath
	 * @param rectBorderPath
	 */
	private void drawOnCanvas(Canvas c, Paint p, Path rectPath,
			Path rectBorderPath) {

		// set background color
		if (rectPath != null) {
			p.setColor(mColor);
			c.drawPath(rectPath, p);
		}

		// set border
		if (rectBorderPath != null) {
			p.setColor(mBorderColor);
			p.setStyle(Style.STROKE);
			p.setStrokeWidth(3);
			c.drawPath(rectBorderPath, p);
		}
	}
}
