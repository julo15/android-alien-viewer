package com.julo.android.redditpix.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.julo.android.redditpix.R;

/**
 * Created by julianlo on 11/13/16.
 */

public class ViewPagerIndicator extends LinearLayout {

    private static final int DEFAULT_DOT_SIZE_PX = 10;
    private static final int DEFAULT_DOT_MARGIN_PX = 5;
    private static final int DEFAULT_SELECTED_COLOR = 0xFFFF0000;
    private static final int DEFAULT_UNSELECTED_COLOR = 0xFF00FF00;

    private ViewPager mViewPager;
    private int mSelectedPosition;
    private Drawable mUnselectedDrawable;
    private Drawable mSelectedDrawable;
    private int mDotMargin;

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            onViewPagerPageSelected(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    /**
     * Exists to handle the initial image URL list retrieval.
     */
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            onViewPagerPageSelected(0);
        }
    };

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator, 0, 0);
        try {
            final int dotSize = typedArray.getDimensionPixelSize(R.styleable.ViewPagerIndicator_dotSize, DEFAULT_DOT_SIZE_PX);
            final int selectedColor = typedArray.getColor(R.styleable.ViewPagerIndicator_selectedColor, DEFAULT_SELECTED_COLOR);
            final int unselectedColor = typedArray.getColor(R.styleable.ViewPagerIndicator_unselectedColor, DEFAULT_UNSELECTED_COLOR);

            mSelectedDrawable = createDotDrawable(selectedColor, dotSize);
            mUnselectedDrawable = createDotDrawable(unselectedColor, dotSize);

            mDotMargin = typedArray.getDimensionPixelSize(R.styleable.ViewPagerIndicator_dotMargin, DEFAULT_DOT_MARGIN_PX);

        } finally {
            typedArray.recycle();
        }
    }

    public void attach(ViewPager viewPager) {
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(mOnPageChangeListener);
        mViewPager.getAdapter().registerDataSetObserver(mDataSetObserver);
        onViewPagerPageSelected(mViewPager.getCurrentItem());
    }

    public void detach() {
        mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        mViewPager.getAdapter().unregisterDataSetObserver(mDataSetObserver);
        mViewPager = null;
        removeAllViews();
    }

    private void onViewPagerPageSelected(int position) {
        if (mViewPager.getAdapter().getCount() != getChildCount()) {
            initSize(position);
        } else if (mViewPager.getAdapter().getCount() > 0){
            updatePosition(position);
        }
    }

    private void initSize(int position) {
        removeAllViews();

        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        if (position >= count || position < 0) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < count; i++) {
            ImageView dotImageView = new ImageView(getContext());
            dotImageView.setImageDrawable(position == i ? mSelectedDrawable : mUnselectedDrawable);
            dotImageView.setPadding(0, 0, mDotMargin, 0);
            addView(dotImageView);
        }

        mSelectedPosition = position;
    }

    private void updatePosition(int position) {
        ((ImageView)getChildAt(mSelectedPosition)).setImageDrawable(mUnselectedDrawable);
        ((ImageView)getChildAt(position)).setImageDrawable(mSelectedDrawable);
        mSelectedPosition = position;
    }

    private static Drawable createDotDrawable(@ColorInt int color, int size) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setSize(size, size);
        drawable.setColor(color);
        return drawable;
    }
}
