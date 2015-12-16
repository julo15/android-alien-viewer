package com.julo.android.alienviewer.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.julo.android.alienviewer.BlurTransformation;
import com.julo.android.alienviewer.Preferences;
import com.julo.android.alienviewer.R;
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

    private static final int REQUEST_AUTHORIZE = 1;

    private static final int FETCH_TASK_STATE_NOT_RUN = 0;
    private static final int FETCH_TASK_STATE_RUNNING = 1;
    private static final int FETCH_TASK_STATE_DONE = 2;

    private RecyclerView mRecyclerView;
    private View mProgressView;
    private TextView mProgressTextView;
    private List<Post> mPosts = new ArrayList<>();
    private FetchPostsTask mFetchPostsTask;
    private int mFetchTaskState = FETCH_TASK_STATE_NOT_RUN;

    public static PostListFragment newInstance() {
        return new PostListFragment();
    }

    public interface Callbacks {
        void onSwitchToSubredditsSelected();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true); // so we don't need to requery reddit on rotation
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_list, container, false);

        mRecyclerView = Util.findView(view, R.id.fragment_post_list_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        setupAdapter();

        mProgressView = Util.findView(view, R.id.progress_full_progress_view);
        mProgressTextView = Util.findView(view, R.id.progress_full_progress_text_view);
        mProgressTextView.setText(R.string.fetching_posts_progress);

        if (Preferences.getAccessToken(getActivity()) == null) {
            startAuthorizeActivityForResult();
        } else if (mFetchTaskState == FETCH_TASK_STATE_NOT_RUN) {
            fetchPosts();
        } else if (mFetchTaskState == FETCH_TASK_STATE_RUNNING) {
            showProgress(true);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh_list:
                fetchPosts();
                return true;

            case R.id.menu_item_refresh_token:
                Intent intent = AuthorizeActivity.newIntent(getActivity());
                startActivityForResult(intent, REQUEST_AUTHORIZE);
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
            fetchPosts();
        }
    }

    private void setupAdapter() {
        mRecyclerView.setAdapter(new PostAdapter(mPosts));
    }

    private void fetchPosts() {
        if (mFetchPostsTask != null) {
            mFetchPostsTask.cancel(false);
        }
        mFetchPostsTask = new FetchPostsTask();
        mFetchPostsTask.execute();
    }

    private void showProgress(boolean show) {
        Util.showView(mProgressView, show);
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

            RequestCreator requestCreator = Picasso.with(getActivity())
                    .load(post.getImageUrl());

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

    private class FetchPostsTask extends AsyncTask<Void,Void,List<Post>> {

        @Override
        protected void onPreExecute() {
            mFetchTaskState = FETCH_TASK_STATE_RUNNING;
            showProgress(true);
        }

        @Override
        protected List<Post> doInBackground(Void... params) {
            try {
                List<Post> posts = new Reddit(Preferences.getAccessToken(getActivity())).fetchPosts(100, Util.IMAGE_POST_FILTERER);
                return posts;
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
        }
    }
}
