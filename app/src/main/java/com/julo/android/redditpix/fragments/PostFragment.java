package com.julo.android.redditpix.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.activities.ImagePagerActivity;
import com.julo.android.redditpix.imgur.Imgur;
import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.util.Util;

/**
 * Created by julianlo on 12/12/15.
 */
public class PostFragment extends ImageFragment {
    private static final String TAG = "PostFragment";

    private static final String ARG_POST_INDEX = "post_index";

    private Post mPost;
    private TextView mTitleTextView;
    private TextView mUrlTextView;
    private TextView mMoreTextView;
    private View mOpenButton;

    public static PostFragment newInstance(int postIndex) {
        Bundle args = new Bundle();
        args.putInt(ARG_POST_INDEX, postIndex);

        PostFragment fragment = new PostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks {
        Post onPostNeeded(int index);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_post;
    }

    @Override
    protected int getImageViewResId() {
        return R.id.fragment_post_image_image_view;
    }

    @Override
    protected int getProgressViewResId() {
        return R.id.fragment_post_image_progress_bar;
    }

    @Override
    protected String getImageUrl() {
        return mPost.getImageUrl();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPost = ((Callbacks)getActivity()).onPostNeeded(getArguments().getInt(ARG_POST_INDEX));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mTitleTextView = Util.findView(view, R.id.fragment_post_image_title_text_view);
        mTitleTextView.setText(mPost.getTitle());

        mUrlTextView = Util.findView(view, R.id.fragment_post_image_url_text_view);
        mUrlTextView.setText(mPost.getUrl());

        mMoreTextView = Util.findView(view, R.id.fragment_post_image_more_text_view);
        mMoreTextView.setVisibility((Imgur.extractAlbumIdFromUrl(mPost.getUrl()) != null) ? View.VISIBLE : View.GONE);

        mOpenButton = Util.findView(view, R.id.fragment_post_image_open_button);
        mOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = ImagePagerActivity.newIntent(getActivity(), mPost, mPost.getImageUrl());
                    ImagePagerActivity.startWithTransition(getActivity(), intent, mImageView);
            }
        });

        return view;
    }
}
