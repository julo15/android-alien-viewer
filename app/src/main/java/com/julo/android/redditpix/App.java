package com.julo.android.redditpix;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by julianlo on 12/25/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());
        Analytics.initializeAnalytics(this);
        Session.initInstance(getApplicationContext());
    }
}
