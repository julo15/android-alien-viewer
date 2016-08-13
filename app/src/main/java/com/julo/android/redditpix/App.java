package com.julo.android.redditpix;

import android.app.Application;

/**
 * Created by julianlo on 12/25/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        asdf

        Analytics.initializeAnalytics(this);
        Session.initInstance(getApplicationContext());
    }
}
