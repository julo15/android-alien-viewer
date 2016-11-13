package com.julo.android.redditpix.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.julo.android.redditpix.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by julianlo on 12/15/15.
 */
public abstract class ImageFragment extends Fragment {
    private static final String TAG = "ImageFragment";

    @Bind(R.id.fragment_image_image_view) /*protected*/ ImageView mImageView;
    @Bind(R.id.fragment_image_progress_bar) View mProgressView;

    PhotoViewAttacher mAttacher;

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
        ButterKnife.bind(this, view);

        if (imageUrl != null) {
            mProgressView.setVisibility(View.VISIBLE);
            Picasso picasso = new Picasso.Builder(getActivity())
                    .listener(new Picasso.Listener() {
                        @Override
                        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                            Log.e(TAG, "Error loading", exception);
                        }
                    })
                    .build();
            picasso
                    .load(Uri.parse(imageUrl))
                    .fit()
                    .centerInside()
                    .error(android.R.drawable.ic_delete)
                    .into(mImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mAttacher.update();
                            mProgressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            mProgressView.setVisibility(View.GONE);
                            mImageView.setImageResource(android.R.drawable.ic_menu_delete);
                        }
                    });
        }

        mAttacher = new PhotoViewAttacher(mImageView);

        return view;
    }
}
