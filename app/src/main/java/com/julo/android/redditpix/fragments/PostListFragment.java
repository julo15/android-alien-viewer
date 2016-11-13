package com.julo.android.redditpix.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.julo.android.redditpix.BlurTransformation;
import com.julo.android.redditpix.Preferences;
import com.julo.android.redditpix.R;
import com.julo.android.redditpix.Session;
import com.julo.android.redditpix.activities.AuthorizeActivity;
import com.julo.android.redditpix.activities.ImagePagerActivity;
import com.julo.android.redditpix.reddit.Listing;
import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.reddit.Reddit;
import com.julo.android.redditpix.util.Util;
import com.julo.android.redditpix.views.EndlessRecyclerView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONException;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.WeakHashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by julianlo on 12/15/15.
 */
public class PostListFragment extends Fragment {
    private static final String TAG = "PostListFragment";

    private static final String ARG_SUBREDDIT = "subreddit";

    private static final int REQUEST_AUTHORIZE = 1;
    public static final int REQUEST_SHOW_POST = 2;

    private static final int FETCH_TASK_STATE_NOT_RUN = 0;
    private static final int FETCH_TASK_STATE_RUNNING = 1;
    private static final int FETCH_TASK_STATE_DONE = 2;

    private static final int NUM_COLUMNS = 2;

    @Bind(R.id.fragment_post_list_recycler_view) EndlessRecyclerView mRecyclerView;
    @Bind(R.id.fragment_post_list_swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @Bind(R.id.info_bar_view_text_view) TextView mInfoBarTextView;
    @Bind(R.id.info_bar_view) View mInfoBarView;
    private WeakHashMap<Post, Point> mImageSizeMap = new WeakHashMap<>();

    private List<Post> mPosts = new ArrayList<>();
    private FetchPostsTask mFetchPostsTask;
    private int mFetchTaskState = FETCH_TASK_STATE_NOT_RUN;
    private FetchContext mFetchContext = new FetchContext();
    private String mSearchQuery;

    public static PostListFragment newInstance() {
        return new PostListFragment();
    }

    public static PostListFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        PostListFragment fragment = new PostListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks {
        void onSwitchToSubredditsSelected();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true); // so we don't need to requery reddit on rotation

        String subreddit = null;
        Bundle args = getArguments();
        if (args != null) {
            subreddit = args.getString(ARG_SUBREDDIT);
        }
        setSubredditSearch(subreddit, false); // don't put it in the search view
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);
        ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(NUM_COLUMNS, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setOnMoreItemsNeededListener(new EndlessRecyclerView.OnMoreItemsNeededListener() {
            @Override
            public boolean onMoreItemsNeeded() {
                fetchPosts(false /* load more */);
                return true;
            }
        });

        mImageSizeMap.clear();
        setupAdapter();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryLight, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPosts(true /* refresh */);
            }
        });

        mInfoBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });

        /*
        if (Preferences.getAccessToken(getActivity()) == null) {
            startAuthorizeActivityForResult();
        } else */ if (mFetchTaskState == FETCH_TASK_STATE_NOT_RUN) {
            fetchPosts(true /* refresh */);
        } else if (mFetchTaskState == FETCH_TASK_STATE_RUNNING) {
            showProgress(true);
        }

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFetchPostsTask != null) {
            mFetchPostsTask.cancel(false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_post_list, menu);

        //if (mSearchQuery != null) {
            menu.findItem(R.id.menu_item_clear_search).setVisible(true);
        //}

        menu.findItem(R.id.menu_item_subreddit_view).setVisible(false);

        if (Session.getInstance().getReddit().isLoggedIn()) {
            menu.findItem(R.id.menu_item_log_in).setVisible(false);
        } else {
            menu.findItem(R.id.menu_item_log_out).setVisible(false);
        }

        final SearchView searchView = Util.cast(menu.findItem(R.id.menu_item_search).getActionView());
        searchView.setQueryHint(getResources().getString(R.string.subreddit_search_hint));

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(mSearchQuery, false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setSubredditSearch(query, true);
                fetchPosts(true /* refresh */);
                Util.hideSearchView(searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh_list:
                fetchPosts(true /* refresh */);
                return true;

            case R.id.menu_item_log_in:
                Intent intent = AuthorizeActivity.newIntent(getActivity());
                startActivityForResult(intent, REQUEST_AUTHORIZE);
                return true;

            case R.id.menu_item_log_out:
                new LogOutTask().execute();
                return true;

            case R.id.menu_item_random_subreddit:
                // Clear out the query, then search for a random subreddit
                setSubredditSearch(null, true);
                mFetchContext.startNewRandomFetch();
                fetchPosts(true /* refresh */);
                return true;

            case R.id.menu_item_clear_search:
                setSubredditSearch(null, true);
                fetchPosts(true /* refresh */);
                return true;

            case R.id.menu_item_subreddit_view:
                ((Callbacks)getActivity()).onSwitchToSubredditsSelected();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_AUTHORIZE) {
            getActivity().invalidateOptionsMenu();
            fetchPosts(true /* refresh */);
        } else if (requestCode == REQUEST_SHOW_POST) {
            Log.v(TAG, "Got an updated post back");
            Post updatedPost = Parcels.unwrap(data.getParcelableExtra(ImagePagerActivity.EXTRA_POST));
            for (ListIterator<Post> iterator = mPosts.listIterator(); iterator.hasNext();) {
                int index = iterator.nextIndex();
                Post post = iterator.next();
                if (post.getId().equals(updatedPost.getId())) {
                    iterator.remove();
                    iterator.add(updatedPost);
                    mRecyclerView.getAdapter().notifyItemChanged(index);
                    break;
                }
            }
        }
    }

    private void setInfoBarText(String text) {
        mInfoBarTextView.setText(text);
    }

    private void showInfoBar(boolean show) {
        Util.showView(mInfoBarView, show);
    }

    private void setupAdapter() {
        // StaggeredGridLayoutManager doesn't play well when you're constantly setting a new adapter
        // (it leaves a bunch of dead whitespace at the top of the RecyclerView), so we always just
        // modify the dataset of the existing adapter.
        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(new PostAdapter(mPosts));
        } else {
            ((PostAdapter)mRecyclerView.getAdapter()).setPosts(mPosts);
        }
    }

    private void setSubredditSearch(String query, boolean isSearchQuery) {
        if (isSearchQuery) {
            mSearchQuery = query;
            getActivity().invalidateOptionsMenu();
        }

        if (query != null) {
            mFetchContext.startNewSubredditFetch(query);
        } else {
            mFetchContext.startNewFrontPageFetch();
        }

    }

    private void fetchPosts(boolean refresh) {
        if (mFetchPostsTask != null) {
            mFetchPostsTask.cancel(false);
        }
        mFetchPostsTask = new FetchPostsTask(
                refresh ?
                    mFetchContext.createParametersForRefresh() :
                    mFetchContext.createParametersForMore());
        mFetchPostsTask.execute();
    }

    private void showProgress(final boolean show) {
        // Even when called from the UI-thread, setRefreshing doesn't always
        // show the refresh UI. This issue occurs in the fetch task's onPreExecute and
        // on the fragment's onCreateView.
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    private void startAuthorizeActivityForResult() {
        Intent intent = AuthorizeActivity.newIntent(getActivity());
        startActivityForResult(intent, REQUEST_AUTHORIZE);
    }

    private class PostHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private TextView mNameTextView;
        private TextView mFreshnessTextView;
        private TextView mNsfwTextView;
        private TextView mMoreImagesTextView;
        private Post mPost;

        public PostHolder(View itemView) {
            super(itemView);
            mImageView = Util.findView(itemView, R.id.post_item_image_view);
            mNameTextView = Util.findView(itemView, R.id.post_item_name_text_view);
            mFreshnessTextView = Util.findView(itemView, R.id.post_item_freshness_text_view);
            mNsfwTextView = Util.findView(itemView, R.id.post_item_nsfw_text_view);
            mMoreImagesTextView = Util.findView(itemView, R.id.post_item_more_images_text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Start the activity for result, and receive the post and put it in the list.
                    Intent intent = ImagePagerActivity.newIntent(getActivity(), mPost, mPost.getImageUrl());
                    ImagePagerActivity.startWithTransitionForResult(getActivity(), intent, mImageView, REQUEST_SHOW_POST);
                }
            });
        }

        private void cacheImageViewSize() {
            // Cache the size of the image
            Point size = new Point(mImageView.getWidth(), mImageView.getHeight());
            if (size.x > 0 && size.y > 0) {
                mImageSizeMap.put(mPost, size);
            }
        }

        private void setImageViewSize() {
            ViewGroup.LayoutParams lp = mImageView.getLayoutParams();
            Point cachedSize = mImageSizeMap.get(mPost);
            lp.width = (cachedSize != null) ? cachedSize.x : ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = (cachedSize != null) ? cachedSize.y : ViewGroup.LayoutParams.WRAP_CONTENT;
            mImageView.setLayoutParams(lp);
        }

        public void bindPost(Post post) {
            String previousImageUrl = null;
            if (mPost != null) {
                cacheImageViewSize();
                previousImageUrl = mPost.getImageUrl();
            }

            mPost = post;

            String name = getResources().getString(R.string.post_item_description,
                    mPost.getSubredditName(), mPost.getCommentCount());
            mNameTextView.setText(name);

            mFreshnessTextView.setText(Util.getRelativeTimeString(getActivity(), mPost.getCreatedUtc()));

            Util.showView(mNsfwTextView, mPost.isNsfw());
            Util.showView(mMoreImagesTextView, Util.isImgurAlbumUrl(mPost.getUrl()));

            // Don't bother reloading the image if it's the same one.
            if (!mPost.getImageUrl().equals(previousImageUrl)) {
                // If we've already scrolled past this post before, use the cached size.
                setImageViewSize();

                // Get an approximate pixel width for the image view. The width is approximately equal to
                // the width of the screen divided by the number of columns. It is only approximate since
                // there is margin/padding around the image. This is fine, since we are using it only to
                // downscale the image - no big deal if the image is a bit bigger.
                Point size = new Point();
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);
                int approximateImageViewWidth = size.x / NUM_COLUMNS;

                RequestCreator requestCreator = Picasso.with(getActivity())
                        .load(post.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .resize(approximateImageViewWidth, 0)
                        .onlyScaleDown();

                if (mPost.isNsfw()) {
                    requestCreator.transform(new BlurTransformation(getActivity(), 25, 2));
                }

                requestCreator.into(mImageView);
            }
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        private List<Post> mPosts;

        public PostAdapter(List<Post> posts) {
            setPosts(posts);
        }

        public void setPosts(List<Post> posts) {
            mPosts = posts;
            notifyDataSetChanged();
        }

        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.post_item, parent, false);
            return new PostHolder(view);
        }

        @Override
        public void onBindViewHolder(PostHolder holder, int position) {
            holder.bindPost(mPosts.get(position));
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }
    }

    private class FetchParameters {
        public String subredditName;
        public boolean findRandomSubreddit;
        public String after;

        public FetchParameters setSubreddit(String name) {
            subredditName = name;
            findRandomSubreddit = false;
            return this;
        }

        public FetchParameters setDoRandomSubreddit(boolean random) {
            findRandomSubreddit = random;
            return this;
        }

        public FetchParameters setAfter(String a) {
            after = a;
            return this;
        }
    }


    private class FetchContext {
        private FetchParameters mBaseParameters;
        private String mNextAfter;

        public void startNewFrontPageFetch() {
            mBaseParameters = new FetchParameters();
            resetContext();
        }

        public void startNewRandomFetch() {
            mBaseParameters = new FetchParameters()
                    .setDoRandomSubreddit(true);
            resetContext();
        }

        public void startNewSubredditFetch(String subreddit) {
            mBaseParameters = new FetchParameters()
                    .setSubreddit(subreddit);
            resetContext();
        }

        public void notifyFetchComplete(String newAfter, String randomSubredditFound) {
            mNextAfter = newAfter;
            if (randomSubredditFound != null) {
                mBaseParameters.setSubreddit(randomSubredditFound);
            }
        }

        private void resetContext() {
            mNextAfter = null;
        }

        public FetchParameters createParametersForRefresh() {
            FetchParameters fetchParameters = new FetchParameters();
            fetchParameters.subredditName = mBaseParameters.subredditName;
            fetchParameters.findRandomSubreddit = mBaseParameters.findRandomSubreddit;
            return fetchParameters;
        }

        public FetchParameters createParametersForMore() throws IllegalStateException {
            if (mNextAfter == null) {
                throw new IllegalStateException("No after token available");
            }
            return createParametersForRefresh()
                    .setAfter(mNextAfter);
        }
    }

    private class FetchPostsTask extends AsyncTask<FetchParameters,Void,Listing<Post>> {

        private FetchParameters mTaskFetchParameters;
        private String mRandomSubredditName;

        public FetchPostsTask(FetchParameters fetchParameters) {
            mTaskFetchParameters = fetchParameters;
        }

        @Override
        protected void onPreExecute() {
            mFetchTaskState = FETCH_TASK_STATE_RUNNING;
            showProgress(true);

            String infoText;
            if (mTaskFetchParameters.findRandomSubreddit) {
                infoText = getResources().getString(R.string.fetching_random_subreddit);
            } else if (mTaskFetchParameters.subredditName != null) {
                infoText = getResources().getString(R.string.fetching_subreddit_format, mTaskFetchParameters.subredditName);
            } else if (Preferences.getUserName(getActivity()) != null) {
                infoText = getResources().getString(R.string.fetching_front_page_with_name, Preferences.getUserName(getActivity()));
            } else {
                infoText = getResources().getString(R.string.fetching_front_page);
            }

            Snackbar.make(mRecyclerView, infoText, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.dismiss, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    })
                    .show();
        }

        @Override
        protected Listing<Post> doInBackground(FetchParameters... params) {
            try {
                Reddit reddit = Session.getInstance().getReddit();
                final int numPosts = 100;

                if (mTaskFetchParameters.subredditName != null) {
                    return reddit.fetchPosts(mTaskFetchParameters.subredditName, numPosts, mTaskFetchParameters.after, Util.IMAGE_POST_FILTERER);
                } else if (mTaskFetchParameters.findRandomSubreddit) {
                    return reddit.fetchPosts("random", numPosts, mTaskFetchParameters.after, new Reddit.Filterer<Post>() {
                        @Override
                        public boolean filter(Post post) {
                            mRandomSubredditName = post.getSubredditName();
                            return Util.IMAGE_POST_FILTERER.filter(post);
                        }
                    });
                } else {
                    return reddit.fetchPosts(numPosts, mTaskFetchParameters.after, Util.IMAGE_POST_FILTERER);
                }
            } catch (Reddit.AuthenticationException ae) {
                Log.w(TAG, "Access token expired", ae);
                startAuthorizeActivityForResult();
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to load posts", ioe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Listing<Post> postListing) {
            if (mTaskFetchParameters.after == null) {
                mPosts = (postListing != null) ? postListing.getItems() : new ArrayList<Post>();
                mRecyclerView.smoothScrollToPosition(0);
                mImageSizeMap.clear();
            } else if (postListing != null) {
                mPosts.addAll(postListing.getItems());
            }
            setupAdapter();
            showProgress(false);
            mRecyclerView.notifyDoneLoading();
            mFetchTaskState = FETCH_TASK_STATE_DONE;

            // If we just found a random subreddit, morph the FetchParameters to point to the
            // subreddit that was found. This makes it so performing a refresh will refresh the found
            // subreddit, as opposed to doing another random subreddit search.
            mFetchContext.notifyFetchComplete((postListing != null) ? postListing.getAfter() : null, mRandomSubredditName);

            if (mPosts.size() == 0) {
                String text;
                if (mTaskFetchParameters.findRandomSubreddit) {
                    text = getResources().getString(R.string.no_image_posts_in_subreddit, mRandomSubredditName);
                } else if (mTaskFetchParameters.subredditName != null) {
                    text = getResources().getString(R.string.no_image_posts_in_subreddit, mTaskFetchParameters.subredditName);
                } else {
                    text = getResources().getString(R.string.no_image_posts);
                }

                Snackbar.make(mRecyclerView, text, Snackbar.LENGTH_LONG)
                        .show();
            }

            String infoText;
            if (mTaskFetchParameters.findRandomSubreddit) {
                infoText = "Showing r/" + mRandomSubredditName;
            } else if (mTaskFetchParameters.subredditName != null) {
                infoText = "Showing r/" + mTaskFetchParameters.subredditName;
            } else if (Preferences.getUserName(getActivity()) != null) {
                infoText = getResources().getString(R.string.front_page_with_name, Preferences.getUserName(getActivity()));
            } else {
                infoText = getResources().getString(R.string.front_page);
            }

            setInfoBarText(infoText);
            showInfoBar(infoText != null);
        }
    }

    private class LogOutTask extends AsyncTask<Void,Void,Void> {
        ProgressDialog mProgressDialog;
        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.logging_out));
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Session.getInstance().getReddit().revokeTokens();
            } catch (IOException ioe) {
                Log.e(TAG, "Exception thrown trying to revoke tokens", ioe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.hide();
            Session.getInstance().setNewTokens(null);
            Preferences.setUserName(getActivity(), null);
            getActivity().invalidateOptionsMenu();
            fetchPosts(true /* refresh */);
        }
    }
}
