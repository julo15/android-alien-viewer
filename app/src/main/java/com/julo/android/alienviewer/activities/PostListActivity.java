package com.julo.android.alienviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.julo.android.alienviewer.fragments.PostListFragment;

/**
 * Created by julianlo on 12/15/15.
 */
public class PostListActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, PostListActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return PostListFragment.newInstance();
    }
}
