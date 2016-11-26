package com.julo.android.redditpix.fragments;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.julo.android.redditpix.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by julianlo on 11/13/16.
 */

public class OnboardingFragment extends Fragment {

    private static final String ARG_IMAGE_RESID = "image";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";

    @Bind(R.id.fragment_onboarding_imageview) ImageView mImageView;
    @Bind(R.id.fragment_onboarding_title_textview) TextView mTitleTextView;
    @Bind(R.id.fragment_onboarding_desc_textview) TextView mDescTextView;

    public static OnboardingFragment newInstance(
            @DrawableRes int image, String title, String description) {
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RESID, image);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);

        OnboardingFragment fragment = new OnboardingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding, container, false);
        ButterKnife.bind(this, view);

        mImageView.setImageResource(getArguments().getInt(ARG_IMAGE_RESID));
        mTitleTextView.setText(getArguments().getString(ARG_TITLE));
        mDescTextView.setText(getArguments().getString(ARG_DESCRIPTION));

        return view;
    }
}
