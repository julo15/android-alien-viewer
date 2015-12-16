package com.julo.android.alienviewer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.julo.android.alienviewer.R;

/**
 * Created by julianlo on 12/15/15.
 */
public abstract class SelectSingleFragmentActivity extends SingleFragmentActivity {

    private static final String STATE_CURRENT_FRAGMENT_INDEX = "current_fragment_index";

    private int mCurrentFragmentIndex;
    protected abstract Fragment createFragment(int i);

    @Override
    protected final Fragment createFragment() {
        return createFragment(mCurrentFragmentIndex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentFragmentIndex = savedInstanceState.getInt(STATE_CURRENT_FRAGMENT_INDEX, 0);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_FRAGMENT_INDEX, mCurrentFragmentIndex);
    }

    public void setCurrentFragment(int i) {
        mCurrentFragmentIndex = i;
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, createFragment())
                .commit();
    }
}
