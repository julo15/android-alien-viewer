package com.julo.android.redditpix.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.util.Util;

/**
 * Created by julianlo on 12/13/15.
 */
public class BaseWebViewFragment extends Fragment {
    private static final String TAG = BaseWebViewFragment.class.getSimpleName();

    private static final String ARG_URL = "url";

    protected WebView mWebView;
    private ProgressBar mProgressBar;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onPageLoading(String url);
    }

    protected class BaseWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.v(TAG, "Started: " + url);
            if (mCallbacks != null) {
                mCallbacks.onPageLoading(url);
            }
        }
    }

    public static class Builder {
        private String mUrl;
        private boolean mShowAddress;

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public BaseWebViewFragment build() {
            BaseWebViewFragment fragment = new BaseWebViewFragment();
            Bundle args = new Bundle();
            args.putString(ARG_URL, mUrl);
            fragment.setArguments(args);
            return fragment;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_web_view, container, false);
        initializeWebView(view, R.id.fragment_base_web_view_web_view, R.id.fragment_base_web_view_progress_bar, new BaseWebViewClient());
        return view;
    }

    protected void initializeWebView(View root, int webViewResId, int progressBarResId, BaseWebViewClient webViewClient) {
        mProgressBar = Util.findView(root, R.id.fragment_base_web_view_progress_bar);
        mProgressBar.setMax(100);

        mWebView = Util.findView(root, R.id.fragment_base_web_view_web_view);

        // Enabling these for Reddit auth support. Not sure about other implications of this though.
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);

        // Presumably I added this to get pinch zooming working, but don't fully recall.
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        // Get webpages to load zoomed out.
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setProgress(newProgress);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
        mWebView.setWebViewClient(webViewClient);

        Bundle args = getArguments();
        String url = (args != null) ? args.getString(ARG_URL) : null;
        if (url != null) {
            mWebView.loadUrl(url);
        }
    }

    public boolean handleBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else {
            return false;
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        mCallbacks = callbacks;
    }
}
