package com.julo.android.alienviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.julo.android.alienviewer.R;
import com.julo.android.alienviewer.fragments.PostListFragment;
import com.julo.android.alienviewer.fragments.SubredditListFragment;

/**
 * Created by julianlo on 12/15/15.
 */
public class MainListActivity extends SingleFragmentActivity
        implements SubredditListFragment.Callbacks, PostListFragment.Callbacks {

    private static final String EXTRA_SUBREDDIT = "com.julo.android.alienviewer.subreddit";

    public static Intent newIntent(Context context) {
        return newIntent(context, null);
    }

    public static Intent newIntent(Context context, String subreddit) {
        Intent intent = new Intent(context, MainListActivity.class);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        // This only gets called on initial create since all the fragments we use here are retained.
        if (getIntent().getStringExtra(EXTRA_SUBREDDIT) != null) {
            return PostListFragment.newInstance(getIntent().getStringExtra(EXTRA_SUBREDDIT));
        } else {
            return PostListFragment.newInstance();
        }
    }

    @Override
    public void onSwitchToPostsSelected() {
        replaceFragment(PostListFragment.newInstance());
    }

    @Override
    public void onSwitchToSubredditsSelected() {
        replaceFragment(SubredditListFragment.newInstance());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
