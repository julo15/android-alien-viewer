package com.julo.android.redditpix.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.julo.android.redditpix.fragments.SubredditListFragment;

public class SubredditListActivity extends SingleFragmentActivity
        implements SubredditListFragment.Callbacks {

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SubredditListActivity.class);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        return SubredditListFragment.newInstance();
    }

    @Override
    public void onSwitchToPostsSelected() {
        Intent intent = PostListActivity.newIntent(this);
        startActivity(intent);
        finish();
    }
}
