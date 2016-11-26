package com.julo.android.redditpix.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.fragments.OnboardingFragment;
import com.julo.android.redditpix.views.ViewPagerIndicator;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by julianlo on 11/13/16.
 */

public class OnboardingActivity extends AppCompatActivity {
    @Bind(R.id.activity_onboarding_viewpager) ViewPager mViewPager;
    @Bind(R.id.activity_onboarding_indicator) ViewPagerIndicator mViewPagerIndicator;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, OnboardingActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);

        mViewPager.setAdapter(new OnboardingPagerAdapter(getSupportFragmentManager()));
        mViewPagerIndicator.attach(mViewPager);
    }

    private class OnboardingPagerAdapter extends FragmentStatePagerAdapter {

        public OnboardingPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return OnboardingFragment.newInstance(
                    new int[] {
                            R.drawable.ic_redditgram_dark,
                            R.drawable.ic_zoom_out_map_black_48dp,
                            R.drawable.ic_collections_black_48dp
                    }[position],
                    getResources().getStringArray(R.array.onboarding_titles)[position],
                    getResources().getStringArray(R.array.onboarding_descriptions)[position]
            );
        }

        @Override
        public int getCount() {
            return getResources().getStringArray(R.array.onboarding_titles).length;
        }
    }
}
