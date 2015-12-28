package com.julo.android.redditpix.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.fragments.ImageFragment;
import com.julo.android.redditpix.imgur.Album;
import com.julo.android.redditpix.imgur.Imgur;
import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/15/15.
 */
public class ImagePagerActivity extends BaseImagePagerActivity {
    private static final String TAG = "ImagePagerActivity";

    public static final String EXTRA_POST = "com.julo.android.redditpix.post";

    private Post mPost;
    private List<String> mImageUrls = new ArrayList<>();
    private TextView mCommentsCountTextView;
    private TextView mTitleTextView;
    private TextView mSubredditTextView;
    private TextView mFreshnessTextView;
    private TextView mKarmaTextView;
    private View mLinkButton;

    public static Intent newIntent(Context context, Post post, String imageUrl) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        BaseImagePagerActivity.putBaseExtras(intent, imageUrl);
        intent.putExtra(EXTRA_POST, post);
        return intent;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_image_pager;
    }

    @Override
    protected int getViewPagerId() {
        return R.id.activity_image_pager_view_pager;
    }

    @Override
    protected int getTransitionImageViewId() {
        return R.id.activity_image_pager_transition_image_view;
    }

    @Override
    protected int getTransitionImageViewParentId() {
        return R.id.activity_image_pager_transition_parent_view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPost = Util.cast(getIntent().getParcelableExtra(EXTRA_POST));

        mCommentsCountTextView = Util.findView(this, R.id.activity_image_pager_comments_count_text_view);
        mCommentsCountTextView.setText(getResources().getString(R.string.comments_format_link, mPost.getCommentCount()));
        mCommentsCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = WebViewActivity.newIntent(ImagePagerActivity.this, Util.ensureUrlHasRedditPrefix(mPost.getCommentsUrl()));
                startActivity(intent);
            }
        });

        mTitleTextView = Util.findView(this, R.id.activity_image_pager_title_text_view);
        mTitleTextView.setText(mPost.getTitle());

        mSubredditTextView = Util.findView(this, R.id.activity_image_pager_subreddit_text_view);
        mSubredditTextView.setText(getResources().getString(R.string.subreddit_format, mPost.getSubredditName()));
        mSubredditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MainListActivity.newIntent(ImagePagerActivity.this, mPost.getSubredditName());
                startActivity(intent);
            }
        });

        mFreshnessTextView = Util.findView(this, R.id.activity_image_pager_freshness_text_view);
        mFreshnessTextView.setText(Util.getRelativeTimeString(this, mPost.getCreatedUtc()));

        mKarmaTextView = Util.findView(this, R.id.activity_image_pager_karma_text_view);
        mKarmaTextView.setText(String.valueOf(mPost.getKarmaCount()));
        findViewById(R.id.activity_image_pager_karma_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ImagePagerActivity.this, R.string.voting_not_supported_toast, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        mLinkButton = Util.findView(this, R.id.activity_image_pager_link_button);
        mLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = WebViewActivity.newIntent(ImagePagerActivity.this, mPost.getUrl());
                startActivity(intent);
            }
        });

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(final int position) {
                return new ImageFragment() {
                    @Override
                    protected String getImageUrl() {
                        return mImageUrls.get(position);
                    }
                };
            }

            @Override
            public int getCount() {
                return mImageUrls.size();
            }
        });
    }

    @Override
    protected void onTransitionComplete() {
        String albumId = Imgur.extractAlbumIdFromUrl(mPost.getUrl());
        if (albumId != null) {
            new FetchImageUrlsTask().execute(albumId);
        }
    }

    private class FetchImageUrlsTask extends AsyncTask<String,Void,List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            try {
                Album album = new Imgur().fetchAlbum(params[0]);
                return album.getImageUrls();
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to fetch album", ioe);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse album", je);
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            mImageUrls = strings;
            mViewPager.getAdapter().notifyDataSetChanged();
            showTransitionImage(false);
        }
    }
}
