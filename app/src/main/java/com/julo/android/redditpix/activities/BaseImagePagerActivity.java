package com.julo.android.redditpix.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.util.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by julianlo on 12/15/15.
 */
public class BaseImagePagerActivity extends AppCompatActivity {
    private static final String TAG = "BaseImagePagerActivity";

    private static final String EXTRA_TRANSITION_IMAGE_URL = "com.julo.android.redditpix.transition_image_url";

    protected ViewPager mViewPager;
    protected ImageView mTransitionImageView;
    private View mTransitionImageParentView;
    private PhotoViewAttacher mPhotoViewAttacher;

    protected static void putBaseExtras(Intent intent, String transitionImageUrl) {
        intent.putExtra(EXTRA_TRANSITION_IMAGE_URL, transitionImageUrl);
    }

    public static void startWithTransition(Activity activity, Intent intent, View sourceView) {
        ViewCompat.setTransitionName(sourceView, "image");
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sourceView, "image");
        activity.startActivity(intent, options.toBundle());
    }

    public static void startWithTransitionForResult(Activity activity, Intent intent, View sourceView, int requestCode) {
        ViewCompat.setTransitionName(sourceView, "image");
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sourceView, "image");
        activity.startActivityForResult(intent, requestCode, options.toBundle());
    }

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_base_image_pager;
    }

    protected int getViewPagerId() {
        return R.id.activity_base_image_pager_view_pager;
    }

    protected int getTransitionImageViewId() {
        return R.id.activity_base_image_pager_transition_image_view;
    }

    protected int getTransitionImageViewParentId() {
        return R.id.activity_base_image_pager_transition_parent_view;
    }

    protected Integer getImageViewScaleType() {
        return null;
    }

    protected void onTransitionComplete() {}

    protected final void showTransitionImage(boolean show) {
        mTransitionImageParentView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        mViewPager = Util.findView(this, getViewPagerId());
        mTransitionImageParentView = Util.findView(this, getTransitionImageViewParentId());

        mTransitionImageView = Util.findView(this, getTransitionImageViewId());
        if (getImageViewScaleType() != null) {
            mTransitionImageView.setScaleType(ImageView.ScaleType.values()[getImageViewScaleType()]);
        }

        mPhotoViewAttacher = new PhotoViewAttacher(mTransitionImageView);

        String imageUrl = getIntent().getStringExtra(EXTRA_TRANSITION_IMAGE_URL);
        if (imageUrl != null) {
            Picasso.with(this)
                    .load(Uri.parse(imageUrl))
                    .fit()
                    .centerInside()
                    .into(mTransitionImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mPhotoViewAttacher.update();
                        }

                        @Override
                        public void onError() {
                            mPhotoViewAttacher.update();
                        }
                    });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onTransitionComplete();
            }
        }, 300);
    }
}
