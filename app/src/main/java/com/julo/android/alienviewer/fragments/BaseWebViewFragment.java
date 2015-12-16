package com.julo.android.alienviewer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.julo.android.alienviewer.R;
import com.julo.android.alienviewer.util.Util;

/**
 * Created by julianlo on 12/13/15.
 */
public class BaseWebViewFragment extends Fragment {

    private static final String ARG_URL = "url";

    protected WebView mWebView;
    private ProgressBar mProgressBar;

    protected static class BaseWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    public static BaseWebViewFragment newInstance(String url) {
        BaseWebViewFragment fragment = new BaseWebViewFragment();
        fragment.setArguments(newBaseWebViewFragmentArguments(url));
        return fragment;
    }

    protected static Bundle newBaseWebViewFragmentArguments(String url) {
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        return args;
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
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
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
}
