package com.julo.android.redditpix.activities;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.Session;
import com.julo.android.redditpix.fragments.BaseImageFragment;
import com.julo.android.redditpix.imgur.Album;
import com.julo.android.redditpix.imgur.Imgur;
import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.reddit.Reddit;
import com.julo.android.redditpix.util.Util;
import com.julo.android.redditpix.views.ToggleTextView;
import com.julo.android.redditpix.views.ViewPagerIndicator;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/15/15.
 */
public class ImagePagerActivity extends BaseImagePagerActivity {
    private static final String TAG = "ImagePagerActivity";

    public static final String EXTRA_POST = "com.julo.android.redditpix.post"; // used for input and results

    private static final int PAGER_INDICATOR_ANIMATION_START_DELAY_MILLIS = 1500;
    private static final int PAGER_INDICATOR_ANIMATION_DURATION_MILLIS = 700;

    private Post mPost;
    private ViewPagerIndicator mViewPagerIndicator;
    private TextView mCommentsCountTextView;
    private TextView mTitleTextView;
    private TextView mSubredditTextView;
    private TextView mFreshnessTextView;
    private ToggleTextView mKarmaTextView;
    private ToggleTextView mUpVoteButton;
    private ToggleTextView mDownVoteButton;
    private View mLinkButton;
    private VoteTask mVoteTask;
    private ObjectAnimator mViewPagerIndicatorAnimator;

    public static Intent newIntent(Context context, Post post, String imageUrl) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        BaseImagePagerActivity.putBaseExtras(intent, imageUrl);
        intent.putExtra(EXTRA_POST, Parcels.wrap(post));
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
        mPost = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_POST));

        mCommentsCountTextView = Util.findView(this, R.id.activity_image_pager_comments_count_text_view);
        mCommentsCountTextView.setText(getResources().getString(R.string.comments_format_link, mPost.getCommentCount()));
        mCommentsCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new WebViewActivity.IntentBuilder(ImagePagerActivity.this)
                        .url(Util.ensureUrlHasRedditPrefix(mPost.getCommentsUrl()))
                        .build();
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

        View.OnClickListener toggleTextViewClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the clicked toggle text view to the transition colour.
                // VoteTask will be responsible for setting it back to normal or emphasized.
                ToggleTextView toggleTextView = Util.cast(v);
                toggleTextView.setToggleState(ToggleTextView.ToggleState.TRANSITION);

                boolean upClicked = (toggleTextView == mUpVoteButton);
                boolean unVote = (mPost.isLiked() != null) && (upClicked == mPost.isLiked());
                int vote;
                if (unVote) {
                    vote = Reddit.VOTE_UNVOTE;
                } else if (upClicked) {
                    vote = Reddit.VOTE_UP;
                } else {
                    vote = Reddit.VOTE_DOWN;
                }

                sendVote(vote);
            }
        };

        mUpVoteButton = Util.findView(this, R.id.activity_image_pager_up_vote_button);
        mUpVoteButton.setOnClickListener(toggleTextViewClickListener);

        mDownVoteButton = Util.findView(this, R.id.activity_image_pager_down_vote_button);
        mDownVoteButton.setOnClickListener(toggleTextViewClickListener);

        updateVoteButtonToggleStates();

        mLinkButton = Util.findView(this, R.id.activity_image_pager_link_button);
        mLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new WebViewActivity.IntentBuilder(ImagePagerActivity.this)
                        .url(mPost.getUrl())
                        .showAddress(true)
                        .build();
                startActivity(intent);
            }
        });

        mViewPager.setAdapter(new ImageFragmentPagerAdapter(getSupportFragmentManager()));
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showPagerIndicatorBriefly();
                return false;
            }
        });

        mViewPagerIndicator = Util.findView(this, R.id.activity_image_pager_indicator);
        mViewPagerIndicator.attach(mViewPager);

        showPagerIndicatorBriefly();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVoteTask != null) {
            mVoteTask.cancel(false);
        }
    }

    @Override
    protected void onTransitionComplete() {
        String albumId = Imgur.extractAlbumIdFromUrl(mPost.getUrl());
        if (albumId != null) {
            new FetchImageUrlsTask().execute(albumId);
        }
    }

    private void showPagerIndicatorBriefly() {
        if (mViewPagerIndicatorAnimator != null) {
            mViewPagerIndicatorAnimator.cancel();
        }
        mViewPagerIndicator.setAlpha(1f);
        mViewPagerIndicatorAnimator = ObjectAnimator.ofFloat(mViewPagerIndicator, "alpha", 1f, 0f)
                .setDuration(PAGER_INDICATOR_ANIMATION_DURATION_MILLIS);

        mViewPagerIndicatorAnimator.setStartDelay(PAGER_INDICATOR_ANIMATION_START_DELAY_MILLIS);
        mViewPagerIndicatorAnimator.start();
    }

    private void updateVoteButtonToggleStates() {
        Boolean isLiked = mPost.isLiked();
        if (isLiked != null && isLiked) {
            mUpVoteButton.setToggleState(ToggleTextView.ToggleState.EMPHASIZED);
            mKarmaTextView.setToggleState(ToggleTextView.ToggleState.EMPHASIZED);
        } else {
            mUpVoteButton.setToggleState(ToggleTextView.ToggleState.NORMAL);
            mKarmaTextView.setToggleState(ToggleTextView.ToggleState.NORMAL);
        }

        mDownVoteButton.setToggleState((isLiked != null && !isLiked) ?
                ToggleTextView.ToggleState.EMPHASIZED : ToggleTextView.ToggleState.NORMAL);
    }

    private void sendVote(int vote) {
        if (mVoteTask != null) {
            mVoteTask.cancel(false);
        }
        mVoteTask = new VoteTask();
        mVoteTask.execute(vote);
    }

    private void onImageUrlsLoaded(List<String> imageUrls) {
        ((ImageFragmentPagerAdapter)mViewPager.getAdapter()).setImageUrls(imageUrls);
        showTransitionImage(false);
    }

    public static class ImageFragment extends BaseImageFragment {
        private String mImageUrl;

        public ImageFragment() {
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            // TODO: Save url
        }

        public ImageFragment setImageUrl(String url) {
            mImageUrl = url;
            return this;
        }

        @Override
        protected String getImageUrl() {
            return mImageUrl;
        }
    }

    private static class ImageFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private List<String> mImageUrls = new ArrayList<>();

        public ImageFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public void setImageUrls(List<String> imageUrls) {
            mImageUrls = imageUrls;
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(final int position) {
            return new ImageFragment()
                    .setImageUrl(mImageUrls.get(position));
        }

        @Override
        public int getCount() {
            return mImageUrls.size();
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
            onImageUrlsLoaded(strings);
        }
    }

    private class VoteTask extends AsyncTask<Integer,Void,Integer> {
        private String mId;
        private Exception mException;

        @Override
        protected void onPreExecute() {
            // Just to be safe, let's keep it so mPost is only accessed on the main thread
            mId = mPost.getId();
        }

        @Override
        protected Integer doInBackground(Integer... params) {

            try {
                Session.getInstance().getReddit().vote(mId, params[0]);
                return params[0];
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to vote", ioe);
                mException = ioe;
            } catch (Reddit.AuthenticationException ae) {
                Log.v(TAG, "Failed to authenticate when trying to vote", ae);
                mException = ae;
                startActivity(AuthorizeActivity.newIntent(ImagePagerActivity.this));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer vote) {
            if (mException != null) {
                if (!(mException instanceof Reddit.AuthenticationException)) {
                    Toast.makeText(ImagePagerActivity.this, R.string.failed_to_send_vote, Toast.LENGTH_SHORT)
                            .show();
                }
            } else {
                mPost.setIsLiked(Util.convertVoteToLike(vote));
                Intent data = new Intent();
                data.putExtra(EXTRA_POST, Parcels.wrap(mPost));
                setResult(RESULT_OK, data);
            }
            updateVoteButtonToggleStates();
        }

        @Override
        protected void onCancelled() {
            updateVoteButtonToggleStates();
        }
    }
}
