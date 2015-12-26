package com.julo.android.alienviewer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.julo.android.alienviewer.BlurTransformation;
import com.julo.android.alienviewer.Preferences;
import com.julo.android.alienviewer.R;
import com.julo.android.alienviewer.Session;
import com.julo.android.alienviewer.activities.AuthorizeActivity;
import com.julo.android.alienviewer.activities.ImagePagerActivity;
import com.julo.android.alienviewer.reddit.Post;
import com.julo.android.alienviewer.reddit.Reddit;
import com.julo.android.alienviewer.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/15/15.
 */
public class PostListFragment extends Fragment {
    private static final String TAG = "PostListFragment";

    private static final String ARG_SUBREDDIT = "subreddit";

    private static final int REQUEST_AUTHORIZE = 1;

    private static final int FETCH_TASK_STATE_NOT_RUN = 0;
    private static final int FETCH_TASK_STATE_RUNNING = 1;
    private static final int FETCH_TASK_STATE_DONE = 2;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mInfoBarTextView;
    private View mInfoBarView;

    private List<Post> mPosts = new ArrayList<>();
    private FetchPostsTask mFetchPostsTask;
    private int mFetchTaskState = FETCH_TASK_STATE_NOT_RUN;
    private FetchParameters mFetchParameters;
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

        mRecyclerView = Util.findView(view, R.id.fragment_post_list_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        setupAdapter();

        mSwipeRefreshLayout = Util.findView(view, R.id.fragment_posts_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryLight, R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPosts();
            }
        });

        mInfoBarTextView = Util.findView(view, R.id.info_bar_view_text_view);
        mInfoBarView = Util.findView(view, R.id.info_bar_view);
        mInfoBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSubredditSearch(null, true); // clear out the search view
                fetchPosts();
            }
        });

        /*
        if (Preferences.getAccessToken(getActivity()) == null) {
            startAuthorizeActivityForResult();
        } else */ if (mFetchTaskState == FETCH_TASK_STATE_NOT_RUN) {
            fetchPosts();
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
                fetchPosts();
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
                fetchPosts();
                return true;

            case R.id.menu_item_log_in:
                Intent intent = AuthorizeActivity.newIntent(getActivity());
                startActivityForResult(intent, REQUEST_AUTHORIZE);
                return true;

            case R.id.menu_item_log_out:
                Session.getInstance().setNewTokens(null);
                Preferences.setUserName(getActivity(), null);
                getActivity().invalidateOptionsMenu();
                fetchPosts();
                return true;

            case R.id.menu_item_random_subreddit:
                // Clear out the query, then search for a random subreddit
                setSubredditSearch(null, true);
                mFetchParameters = new FetchParameters().setDoRandomSubreddit(true);
                fetchPosts();
                return true;

            case R.id.menu_item_clear_search:
                setSubredditSearch(null, true);
                fetchPosts();
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
            fetchPosts();
        }
    }

    private void setInfoBarText(String text) {
        mInfoBarTextView.setText(text);
    }

    private void showInfoBar(boolean show) {
        Util.showView(mInfoBarView, show);
    }

    private void setupAdapter() {
        mRecyclerView.setAdapter(new PostAdapter(mPosts));
    }

    private void setSubredditSearch(String query, boolean isSearchQuery) {
        if (isSearchQuery) {
            mSearchQuery = query;
            getActivity().invalidateOptionsMenu();
        }
        mFetchParameters = new FetchParameters().setSubreddit(query);
    }

    private void fetchPosts() {
        if (mFetchPostsTask != null) {
            mFetchPostsTask.cancel(false);
        }
        mFetchPostsTask = new FetchPostsTask();
        mFetchPostsTask.execute(mFetchParameters);
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
                    Intent intent = ImagePagerActivity.newIntent(getActivity(), mPost, mPost.getImageUrl());
                    ImagePagerActivity.startWithTransition(getActivity(), intent, mImageView);
                }
            });
        }

        public void bindPost(Post post) {
            mPost = post;

            String name = getResources().getString(R.string.post_item_description,
                    mPost.getSubredditName(), mPost.getCommentCount());
            mNameTextView.setText(name);

            mFreshnessTextView.setText(Util.getRelativeTimeString(getActivity(), mPost.getCreatedUtc()));

            Util.showView(mNsfwTextView, mPost.isNsfw());
            Util.showView(mMoreImagesTextView, Util.isImgurAlbumUrl(mPost.getUrl()));

            //Util.recycleImageViewDrawable(mImageView);

            RequestCreator requestCreator = Picasso.with(getActivity())
                    .load(post.getImageUrl())
                    .fit()
                    .centerCrop();

            if (mPost.isNsfw()) {
                requestCreator.transform(new BlurTransformation(getActivity(), 25, 4));
            }

            requestCreator.into(mImageView);
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {
        private List<Post> mPosts;

        public PostAdapter(List<Post> posts) {
            mPosts = posts;
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

        public FetchParameters setSubreddit(String name) {
            subredditName = name;
            findRandomSubreddit = false;
            return this;
        }

        public FetchParameters setDoRandomSubreddit(boolean random) {
            findRandomSubreddit = random;
            return this;
        }
    }

    private class FetchPostsTask extends AsyncTask<FetchParameters,Void,List<Post>> {

        private FetchParameters mTaskFetchParameters;
        private String mRandomSubredditName;

        @Override
        protected void onPreExecute() {
            mFetchTaskState = FETCH_TASK_STATE_RUNNING;
            showProgress(true);
        }

        @Override
        protected List<Post> doInBackground(FetchParameters... params) {
            try {
                FetchParameters fetchParams = params[0];
                mTaskFetchParameters = fetchParams;

                Reddit reddit = Session.getInstance().getReddit();
                final int numPosts = 100;

                if (fetchParams.subredditName != null) {
                    return reddit.fetchPosts(fetchParams.subredditName, numPosts, Util.IMAGE_POST_FILTERER);
                } else if (fetchParams.findRandomSubreddit) {
                    return reddit.fetchPosts("random", numPosts, new Reddit.Filterer<Post>() {
                        @Override
                        public boolean filter(Post post) {
                            mRandomSubredditName = post.getSubredditName();
                            return Util.IMAGE_POST_FILTERER.filter(post);
                        }
                    });
                } else {
                    return reddit.fetchPosts(numPosts, Util.IMAGE_POST_FILTERER);
                }
            } catch (Reddit.AuthenticationException ae) {
                Log.w(TAG, "Access token expired", ae);
                startAuthorizeActivityForResult();
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON", je);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to load posts", ioe);
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<Post> posts) {
            mPosts = posts;
            setupAdapter();
            showProgress(false);
            mFetchTaskState = FETCH_TASK_STATE_DONE;

            if (posts.size() == 0) {
                String text;
                if (mFetchParameters.findRandomSubreddit) {
                    text = getResources().getString(R.string.no_image_posts_in_subreddit, mRandomSubredditName);
                } else if (mTaskFetchParameters.subredditName != null) {
                    text = getResources().getString(R.string.no_image_posts_in_subreddit, mTaskFetchParameters.subredditName);
                } else {
                    text = getResources().getString(R.string.no_image_posts);
                }

                Toast.makeText(getActivity(), text, Toast.LENGTH_LONG)
                        .show();
            }

            // If we just found a random subreddit, morph the FetchParameters to point to the
            // subreddit that was found. This makes it so performing a refresh will refresh the found
            // subreddit, as opposed to doing another random subreddit search.
            if (mTaskFetchParameters.findRandomSubreddit) {
                mTaskFetchParameters.setSubreddit(mRandomSubredditName);
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
}
