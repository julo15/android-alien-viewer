package com.julo.android.redditpix;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by julianlo on 12/26/15.
 */
public class Analytics {
    private static final String TAG = "Analytics";

    private static final String TRACKER_ID = "UA-49886063-5";

    // Singleton state
    private static Context sAppContext;
    private static Tracker sTracker;
    private static SharedPreferences.OnSharedPreferenceChangeListener sPrefListener;

    public static void initializeAnalytics(Application application) {
        sAppContext = application.getApplicationContext();
        setupSharedPreferenceChangeListener();

        if (sTracker != null) {
            throw new IllegalStateException("Analytics has already been initialized!");
        }

        GoogleAnalytics analytics = GoogleAnalytics.getInstance(sAppContext);
        analytics.enableAutoActivityReports(application);

        sTracker = analytics.newTracker(TRACKER_ID);
        sTracker.enableAutoActivityTracking(true);
        sTracker.enableExceptionReporting(true);

        setEnabled(Preferences.isAnalyticsEnabled(sAppContext));
    }

    private static void setupSharedPreferenceChangeListener() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sAppContext);
        sPrefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (Preferences.isAnalyticsEnabledPreferenceKey(key)) {
                    setEnabled(Preferences.isAnalyticsEnabled(sAppContext));
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sPrefListener);
    }

    private static void setEnabled(boolean enabled) {
        GoogleAnalytics.getInstance(sAppContext).setAppOptOut(!enabled);
    }
}
