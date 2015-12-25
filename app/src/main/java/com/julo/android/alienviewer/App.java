package com.julo.android.alienviewer;

import android.app.Application;

/**
 * Created by julianlo on 12/25/15.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Session.initInstance(getApplicationContext());
    }
}
