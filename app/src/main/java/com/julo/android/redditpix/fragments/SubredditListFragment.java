package com.julo.android.redditpix.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.julo.android.redditpix.Preferences;
import com.julo.android.redditpix.R;
import com.julo.android.redditpix.RedditManager;
import com.julo.android.redditpix.Session;
import com.julo.android.redditpix.ThumbnailDownloader;
import com.julo.android.redditpix.activities.AuthorizeActivity;
import com.julo.android.redditpix.activities.PostPagerActivity;
import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.reddit.Reddit;
import com.julo.android.redditpix.reddit.Subreddit;
import com.julo.android.redditpix.util.Util;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by julianlo on 12/11/15.
 */
public class SubredditListFragment extends Fragment {
    private static final String TAG = "SubredditListFragment";

    private static final String SAVED_SEARCH_QUERY = "search_query";
    private static final String DIALOG_SUBREDDIT_PICKER = "DialogSubredditPicker";

    private static final int REQUEST_SUBREDDIT = 0;
    private static final int REQUEST_AUTHORIZE = 1;

    private static final int FETCH_TASK_STATE_NOT_RUN = 0;
    private static final int FETCH_TASK_STATE_RUNNING = 1;
    private static final int FETCH_TASK_STATE_DONE = 2;

    public static SubredditListFragment newInstance() {
        return new SubredditListFragment();
    }

    public interface Callbacks {
        void onSwitchToPostsSelected();
    }

    private RecyclerView mRecyclerView;
    private View mProgressView;
    private TextView mProgressTextView;
    private SearchView mSearchView;
    private List<Subreddit> mSubreddits = new ArrayList<>();
    private ThumbnailDownloader<SubredditHolder> mThumbnailDownloader;
    private FetchSubredditsTask mFetchSubredditsTask;
    private int mFetchTaskState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true); // so we don't need to requery reddit on rotation

        updateUserName();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<SubredditHolder>() {
            @Override
            public void onThumbnailDownloaded(SubredditHolder target, String thumbnailUrl) {
                target.bindImageUrl(thumbnailUrl);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper(); // call to avoid rare race condition
        Log.i(TAG, "Background thread started");

        if (savedInstanceState != null) {
            //mSearchQuery = savedInstanceState.getString(SAVED_SEARCH_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit_list, container, false);

        mRecyclerView = Util.findView(view, R.id.fragment_subreddit_list_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        setupAdapter();

        mProgressView = Util.findView(view, R.id.fragment_subreddit_list_progress_view);
        mProgressTextView = Util.findView(view, R.id.fragment_subreddit_list_progress_text_view);
        if (mFetchTaskState == FETCH_TASK_STATE_RUNNING) {
            showProgress(true);
        }

        if (!Session.getInstance().getReddit().isLoggedIn()) {
            startAuthorizeActivityForResult();
        } else if (mFetchTaskState == FETCH_TASK_STATE_NOT_RUN) {
            fetchSubreddits(null);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFetchSubredditsTask != null) {
            mFetchSubredditsTask.cancel(false);
        }
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_subreddit_list, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        mSearchView = Util.cast(searchItem.getActionView());
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fetchSubreddits(query);
                Util.hideSearchView(mSearchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if (!Preferences.isNsfwAllowed(getActivity())) {
            menu.findItem(R.id.menu_item_toggle_nsfw).setTitle(R.string.show_nsfw);
        }

        if (Session.getInstance().getReddit().isLoggedIn()) {
            menu.findItem(R.id.menu_item_log_in).setVisible(false);
        } else {
            menu.findItem(R.id.menu_item_log_out).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_add_subreddit:
                SubredditPickerFragment dialog = SubredditPickerFragment.newInstance();
                dialog.setTargetFragment(SubredditListFragment.this, REQUEST_SUBREDDIT);
                dialog.show(getFragmentManager(), DIALOG_SUBREDDIT_PICKER);
                return true;

            case R.id.menu_item_refresh_list:
                fetchSubreddits(null);
                return true;

            case R.id.menu_item_toggle_nsfw:
                Preferences.setNsfwAllowed(getActivity(), !Preferences.isNsfwAllowed(getActivity()));
                getActivity().invalidateOptionsMenu();
                fetchSubreddits(null);
                return true;

            case R.id.menu_item_log_in: {
                Intent intent = AuthorizeActivity.newIntent(getActivity());
                startActivityForResult(intent, REQUEST_AUTHORIZE);
                return true;
            }

            case R.id.menu_item_log_out:
                Session.getInstance().setNewTokens(null);
                Preferences.setUserName(getActivity(), null);
                getActivity().invalidateOptionsMenu();
                fetchSubreddits(null);
                return true;

            case R.id.menu_item_post_view: {
                ((Callbacks)getActivity()).onSwitchToPostsSelected();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_SUBREDDIT) {
            String subredditName = data.getStringExtra(SubredditPickerFragment.EXTRA_SUBREDDIT);
            RedditManager.get(getActivity()).addSubreddit(subredditName);
            new LoadSubredditsTask().execute();
        } else if (requestCode == REQUEST_AUTHORIZE) {
            updateUserName();
            getActivity().invalidateOptionsMenu();
            fetchSubreddits(null);
        }
    }

    private void setupAdapter() {
        mRecyclerView.setAdapter(new SubredditAdapter(mSubreddits));
    }

    private void notifyLoadSubredditsDone() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private void updateProgress(String status) {
        mProgressTextView.setText(status);
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void fetchSubreddits(String query) {
        if (mFetchSubredditsTask != null) {
            mFetchSubredditsTask.cancel(false);
        }
        mFetchSubredditsTask = new FetchSubredditsTask();
        mFetchSubredditsTask.execute(query);
    }

    private void updateUserName() {
        String userName = Preferences.getUserName(getActivity());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(userName);
    }

    private void startAuthorizeActivityForResult() {
        Intent intent = AuthorizeActivity.newIntent(getActivity());
        startActivityForResult(intent, REQUEST_AUTHORIZE);
    }

    private class SubredditHolder extends RecyclerView.ViewHolder {
        private Subreddit mSubreddit;
        private TextView mNameTextView;
        private TextView mTitleTextView;
        private TextView mSubscribersTextView;
        private TextView mFreshnessTextView;
        private ImageView mImageView;
        private String mImageUri;

        public SubredditHolder(final View itemView) {
            super(itemView);
            mNameTextView = Util.findView(itemView, R.id.subreddit_item_name_text_view);
            mTitleTextView = Util.findView(itemView, R.id.subreddit_item_title_text_view);
            mSubscribersTextView = Util.findView(itemView, R.id.subreddit_item_subscribers_text_view);
            mFreshnessTextView = Util.findView(itemView, R.id.subreddit_item_freshness_text_view);
            mImageView = Util.findView(itemView, R.id.subreddit_item_image_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = PostPagerActivity.newIntent(getActivity(), mSubreddit.getName(), mImageUri);
                    PostPagerActivity.startWithTransition(getActivity(), intent, mImageView);
                }
            });
        }

        public void bindSubreddit(Subreddit subreddit) {
            mSubreddit = subreddit;
            mNameTextView.setText(subreddit.getName());
            mTitleTextView.setText(subreddit.getTitle());
            mSubscribersTextView.setText(getString(R.string.subscribers_format, subreddit.getSubscribers()));
            mFreshnessTextView.setText(getResources().getString(R.string.subreddit_freshness,
                    DateUtils.getRelativeDateTimeString(getActivity(),
                            subreddit.getTopPostUtc() * 1000L,
                            DateUtils.MINUTE_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,
                            0)));
            mImageView.setImageResource(R.drawable.ic_reddit_circle);

        }

        public void bindImageUrl(String url) {
            mImageUri = url;
            Picasso.with(getActivity())
                    .load(Uri.parse(url))
                    .fit()
                    .centerInside()
                    .into(mImageView);
        }
    }

    private class SubredditAdapter extends RecyclerView.Adapter<SubredditHolder> {
        private List<Subreddit> mSubreddits;

        public SubredditAdapter(List<Subreddit> subreddits) {
            mSubreddits = subreddits;
        }

        @Override
        public SubredditHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.subreddit_item, viewGroup, false);
            return new SubredditHolder(view);
        }

        @Override
        public void onBindViewHolder(SubredditHolder subredditHolder, int position) {
            Subreddit subreddit = mSubreddits.get(position);
            subredditHolder.bindSubreddit(subreddit);
            mThumbnailDownloader.queueThumbnail(subredditHolder, subreddit.getName());
        }

        @Override
        public int getItemCount() {
            return mSubreddits.size();
        }
    }

    private class LoadSubredditsTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Reddit reddit = Session.getInstance().getReddit();
            for (Subreddit subreddit : mSubreddits) {
                reddit.loadSubreddit(subreddit);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            notifyLoadSubredditsDone();
        }
    }

    private class FetchSubredditsTask extends AsyncTask<String,String,List<Subreddit>> {

        @Override
        protected void onPreExecute() {
            mFetchTaskState = FETCH_TASK_STATE_RUNNING;
            showProgress(true);
        }

        @Override
        protected List<Subreddit> doInBackground(String... params) {
            try {
                return fetchSubredditsByNewestPost(params[0]);
            } catch (Reddit.AuthenticationException ae) {
                Log.w(TAG, "Access token expired", ae);
                startAuthorizeActivityForResult();
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to load subreddit", ioe);
            }
            return new ArrayList<>();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            updateProgress(values[0]);
        }

        @Override
        protected void onPostExecute(List<Subreddit> subreddits) {
            if (isCancelled()) {
                return;
            }
            mSubreddits = subreddits;
            setupAdapter();
            mFetchTaskState = FETCH_TASK_STATE_DONE;
            showProgress(false);
        }

        private List<Subreddit> fetchSubredditsByNewestPost(final String query)
                throws Reddit.AuthenticationException, JSONException, IOException {
            publishProgress(getResources().getString(R.string.fetching_subs_progress));
            Reddit reddit = Session.getInstance().getReddit();
            List<Subreddit> subreddits = reddit.fetchSubscribedSubreddits(100, new Reddit.Filterer<Subreddit>() {
                @Override
                public boolean filter(Subreddit item) {
                    return ((query == null) || item.getName().toLowerCase().contains(query.toLowerCase()));
                }
            });

            if (isCancelled()) {
                return null;
            }

            boolean allowNsfw = Preferences.isNsfwAllowed(getActivity());

            int i = 0;
            int initialSize = subreddits.size();
            for (Iterator<Subreddit> iterator = subreddits.iterator(); iterator.hasNext();) {
                i++;
                Subreddit subreddit = iterator.next();
                try {
                    publishProgress(getResources().getString(R.string.fetching_sub_posts_progress, i, initialSize));

                    // Filter
                    if (!allowNsfw && subreddit.isOver18()) {
                        iterator.remove();
                        continue;
                    }

                    subreddit.setTopPostUtc(0);

                    List<Post> posts = new Reddit(null).fetchPosts(subreddit.getName(), 3, Util.IMAGE_POST_FILTERER);

                    if (isCancelled()) {
                        return null;
                    }

                    for (Post post : posts) {
                        if (subreddit.getTopPostUtc() < post.getCreatedUtc()) {
                            subreddit.setTopPostUtc(post.getCreatedUtc());
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Failed to fetch posts for " + subreddit.getName(), e);
                }
            }

            publishProgress(getResources().getString(R.string.sorting_subs_progress));
            Collections.sort(subreddits, new Comparator<Subreddit>() {
                @Override
                public int compare(Subreddit lhs, Subreddit rhs) {
                    int l = lhs.getTopPostUtc();
                    int r = rhs.getTopPostUtc();
                    if (l > r) {
                        return -1;
                    } else if (l < r) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            return subreddits;
        }

        private List<Subreddit> fetchSubredditsByHotPosts()
            throws Reddit.AuthenticationException, JSONException, IOException {
            Reddit reddit = Session.getInstance().getReddit();
            List<String> subredditNames = new ArrayList<>();
            List<Post> posts = reddit.fetchPosts(200, Util.IMAGE_POST_FILTERER);

            boolean allowNsfw = Preferences.isNsfwAllowed(getActivity());

            for (Post post : posts) {
                if (!allowNsfw && post.isNsfw()) {
                    continue;
                }

                if (!subredditNames.contains(post.getSubredditName())) {
                    subredditNames.add(post.getSubredditName());
                }
            }
            List<Subreddit> subreddits = new ArrayList<>();
            for (String name : subredditNames) {
                Subreddit subreddit = new Subreddit(name);
                subreddits.add(subreddit);
            }
            return subreddits;
        }
    }
}
