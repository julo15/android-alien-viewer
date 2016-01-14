package com.julo.android.redditpix.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.fragments.BaseWebViewFragment;
import com.julo.android.redditpix.util.Util;

/**
 * Created by julianlo on 12/13/15.
 */
public class WebViewActivity extends SingleFragmentActivity
        implements BaseWebViewFragment.Callbacks {

    private static final String EXTRA_SHOW_ADDRESS = "com.julo.android.redditpix.show_address";

    public static class IntentBuilder {
        Context mContext;
        String mUrl;
        boolean mShowAddress;

        public IntentBuilder(Context context) {
            mContext = context;
        }

        public IntentBuilder url(String url) {
            mUrl = url;
            return this;
        }

        public IntentBuilder showAddress(boolean show) {
            mShowAddress = show;
            return this;
        }

        public Intent build() {
            Intent intent = new Intent(mContext, WebViewActivity.class);
            if (mUrl != null) {
                intent.setData(Uri.parse(mUrl));
            }
            intent.putExtra(EXTRA_SHOW_ADDRESS, mShowAddress);
            return intent;
        }
    }

    @Override
    protected Fragment createFragment() {
        BaseWebViewFragment fragment = new BaseWebViewFragment.Builder()
                .url(getIntent().getData().toString())
                .build();

        fragment.setCallbacks(this);

        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_web_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_copy_link:
                CharSequence url = getSupportActionBar().getSubtitle();
                String urlString = (url != null) ? url.toString() : null;

                if (urlString != null) {
                    ClipboardManager clipboardManager = Util.cast(getSystemService(CLIPBOARD_SERVICE));
                    clipboardManager.setPrimaryClip(ClipData.newRawUri(urlString, Uri.parse(urlString)));
                    Toast toast = Toast.makeText(this, R.string.link_copied, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, getResources().getDimensionPixelOffset(R.dimen.padding_normal));
                    toast.show();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        BaseWebViewFragment fragment = Util.cast(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
        if (!fragment.handleBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageLoading(String url) {
        getSupportActionBar().setSubtitle(url);
    }
}
