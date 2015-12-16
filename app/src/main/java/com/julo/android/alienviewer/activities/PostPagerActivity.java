package com.julo.android.alienviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.julo.android.alienviewer.fragments.PostFragment;
import com.julo.android.alienviewer.reddit.Post;
import com.julo.android.alienviewer.reddit.Reddit;
import com.julo.android.alienviewer.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/12/15.
 */
public class PostPagerActivity extends BaseImagePagerActivity
        implements PostFragment.Callbacks {
    private static final String TAG = "PostPagerActivity";

    private static final String EXTRA_SUBREDDIT = "com.julo.android.alienviewer.subreddit";

    private List<Post> mPosts = new ArrayList<>();

    public static Intent newIntent(Context context, String subreddit, String imageUrl) {
        Intent intent = new Intent(context, PostPagerActivity.class);
        BaseImagePagerActivity.putBaseExtras(intent, imageUrl);
        intent.putExtra(EXTRA_SUBREDDIT, subreddit);
        return intent;
    }

    @Override
    protected Integer getImageViewScaleType() {
        return ImageView.ScaleType.CENTER_CROP.ordinal();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setSubtitle(getIntent().getStringExtra(EXTRA_SUBREDDIT));

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Post post = mPosts.get(position);
                return PostFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return mPosts.size();
            }
        });

        new FetchPostsTask().execute(getIntent().getStringExtra(EXTRA_SUBREDDIT));
    }

    @Override
    public Post onPostNeeded(int index) {
        return mPosts.get(index);
    }

    private class FetchPostsTask extends AsyncTask<String,Void,List<Post>> {
        @Override
        protected List<Post> doInBackground(String... params) {
            try {
                List<Post> posts = new Reddit(null).fetchPosts(params[0], Util.IMAGE_POST_FILTERER);
                return posts;
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch posts", ioe);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse posts JSON", je);
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            mPosts = posts;
            mViewPager.getAdapter().notifyDataSetChanged();
            mTransitionImageView.setVisibility(View.GONE);
        }
    }
}
