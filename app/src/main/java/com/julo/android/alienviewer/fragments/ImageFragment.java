package com.julo.android.alienviewer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.julo.android.alienviewer.R;
import com.julo.android.alienviewer.util.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by julianlo on 12/15/15.
 */
public abstract class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";

    protected ImageView mImageView;
    private View mProgressView;

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.fragment_image;
    }

    protected int getImageViewResId() {
        return R.id.fragment_image_image_view;
    }

    protected int getProgressViewResId() {
        return R.id.fragment_image_progress_bar;
    }

    // Called in onCreateView
    protected abstract String getImageUrl();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String imageUrl = getImageUrl();

        View view = inflater.inflate(getLayoutResId(), container, false);

        mProgressView = Util.findView(view, getProgressViewResId());

        mImageView = Util.findView(view, getImageViewResId());
        if (imageUrl != null) {
            mProgressView.setVisibility(View.VISIBLE);
            Picasso.with(getActivity())
                    .load(Uri.parse(imageUrl))
                    .error(android.R.drawable.ic_delete)
                    .into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProgressView.setVisibility(View.GONE);
                            mImageView.setImageResource(android.R.drawable.ic_menu_delete);
                        }
                    });
        }

        return view;
    }
}
