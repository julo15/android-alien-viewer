package com.julo.android.alienviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.julo.android.alienviewer.fragments.PostListFragment;
import com.julo.android.alienviewer.fragments.SubredditListFragment;

/**
 * Created by julianlo on 12/15/15.
 */
public class MainListActivity extends SelectSingleFragmentActivity
        implements SubredditListFragment.Callbacks, PostListFragment.Callbacks {

    private static final int FRAGMENT_SUBREDDITS = 1;
    private static final int FRAGMENT_POSTS = 0;

    public static Intent newIntent(Context context) {
        return new Intent(context, MainListActivity.class);
    }

    @Override
    protected Fragment createFragment(int i) {
        switch (i) {
            case FRAGMENT_SUBREDDITS:
                return SubredditListFragment.newInstance();
            case FRAGMENT_POSTS:
                return PostListFragment.newInstance();
            default:
                throw new ArrayIndexOutOfBoundsException("Bad fragment index");
        }
    }

    @Override
    public void onSwitchToPostsSelected() {
        setCurrentFragment(FRAGMENT_POSTS);
    }

    @Override
    public void onSwitchToSubredditsSelected() {
        setCurrentFragment(FRAGMENT_SUBREDDITS);
    }
}
