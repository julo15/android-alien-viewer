package com.julo.android.alienviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.julo.android.alienviewer.R;

/**
 * Created by julianlo on 12/13/15.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = MainListActivity.newIntent(SplashActivity.this);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
