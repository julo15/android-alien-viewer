package com.julo.android.redditpix;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by julianlo on 1/14/16.
 */
public class ToggleTextView extends TextView {

    private int mNormalColor;
    private int mEmphasizedColor;
    private int mTransitionColor;
    private ToggleState mToggleState = ToggleState.NORMAL;

    public enum ToggleState {
        NORMAL,
        EMPHASIZED,
        TRANSITION,
    }

    public ToggleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ToggleTextView, 0, 0);
        try {
            int defaultTextColor = getCurrentTextColor();
            setNormalColor(typedArray.getColor(R.styleable.ToggleTextView_normalTextColor, defaultTextColor));
            setEmphasizedColor(typedArray.getColor(R.styleable.ToggleTextView_emphasizedTextColor, defaultTextColor));
            setTransitionColor(typedArray.getColor(R.styleable.ToggleTextView_transitionTextColor, defaultTextColor));
        } finally {
            typedArray.recycle();
        }

        updateTextColor();
    }

    public int getNormalColor() {
        return mNormalColor;
    }

    public void setNormalColor(int normalColor) {
        mNormalColor = normalColor;
        updateTextColor();
    }

    public int getEmphasizedColor() {
        return mEmphasizedColor;
    }

    public void setEmphasizedColor(int emphasizedColor) {
        mEmphasizedColor = emphasizedColor;
        updateTextColor();
    }

    public int getTransitionColor() {
        return mTransitionColor;
    }

    public void setTransitionColor(int transitionColor) {
        mTransitionColor = transitionColor;
        updateTextColor();
    }

    public ToggleState getToggleState() {
        return mToggleState;
    }

    public void setToggleState(@NonNull ToggleState toggleState) {
        mToggleState = toggleState;
        updateTextColor();
    }

    private void updateTextColor() {
        int color;
        if (mToggleState == ToggleState.EMPHASIZED) {
            color = mEmphasizedColor;
        } else if (mToggleState == ToggleState.TRANSITION) {
            color = mTransitionColor;
        } else {
            color = mNormalColor;
        }
        setTextColor(color);
    }
}
