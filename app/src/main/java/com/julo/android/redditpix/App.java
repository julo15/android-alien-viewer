package com.julo.android.redditpix;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.squareup.leakcanary.LeakCanary;

import io.fabric.sdk.android.Fabric;

/**
 * Created by julianlo on 12/25/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        Fabric.with(this, new Crashlytics());
        Analytics.initializeAnalytics(this);
        Session.initInstance(getApplicationContext());
    }
}
