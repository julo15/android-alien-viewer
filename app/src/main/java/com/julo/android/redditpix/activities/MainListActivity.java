package com.julo.android.redditpix.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.fragments.PostListFragment;
import com.julo.android.redditpix.fragments.SubredditListFragment;

/**
 * Created by julianlo on 12/15/15.
 */
public class MainListActivity extends SingleFragmentActivity
        implements SubredditListFragment.Callbacks, PostListFragment.Callbacks {

    private static final String EXTRA_SUBREDDIT = "com.julo.android.redditpix.subreddit";

    public static Intent newIntent(Context context) {
        return newIntent(context, null);
    }

    public static Intent newIntent(Context context, String subreddit) {
        Intent intent = new Intent(context, MainListActivity.class);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_redditgram);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The request to open an image is done with Activity.startActivityForResult, so we do not get the benefit
        // of the result being automatically forwarded by FragmentActivity to the PostListFragment.
        // (We use Activity.startActivityForResult because it supports transitions, and there is no Fragment
        // equivalent of startActivityForResult that supports transitions.)
        // So we manually forward the request to the fragment here.
        if (requestCode == PostListFragment.REQUEST_SHOW_POST) {
            getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
        }
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
