package com.julo.android.alienviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;

import com.julo.android.alienviewer.R;
import com.julo.android.alienviewer.fragments.BaseWebViewFragment;
import com.julo.android.alienviewer.util.Util;

/**
 * Created by julianlo on 12/13/15.
 */
public class WebViewActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context, String url) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.setData(Uri.parse(url));
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return BaseWebViewFragment.newInstance(getIntent().getData().toString());
    }

    @Override
    public void onBackPressed() {
        BaseWebViewFragment fragment = Util.cast(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        if (!fragment.handleBackPressed()) {
            super.onBackPressed();
        }
    }
}
