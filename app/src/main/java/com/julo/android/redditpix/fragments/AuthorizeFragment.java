package com.julo.android.redditpix.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.julo.android.redditpix.R;
import com.julo.android.redditpix.reddit.Reddit;
import com.julo.android.redditpix.util.Util;

/**
 * Created by julianlo on 12/12/15.
 */
public class AuthorizeFragment extends BaseWebViewFragment {
    private static final String TAG = "AuthorizeFragment";

    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCodeRetrieved(String code);
    }

    public static AuthorizeFragment newInstance() {
        return new AuthorizeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallbacks = Util.cast(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_web_view, container, false);

        Uri authorizeUri = new Reddit(null).getAuthorizeUri(true, "identity,mysubreddits,read");
        Log.i(TAG, "Got authorize URL: " + authorizeUri.toString());

        initializeWebView(view, R.id.fragment_base_web_view_web_view, R.id.fragment_base_web_view_progress_bar, new BaseWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Page finished loading: " + url);

                super.onPageFinished(view, url);

                if (url.startsWith(Reddit.REDIRECT_URI)) {
                    view.setVisibility(View.GONE);
                    Uri uri = Uri.parse(url);
                    if (uri.getQueryParameter("error") == null) {
                        String code = uri.getQueryParameter("code");
                        sendResult(code);
                    } else {
                        sendResult(null);
                    }
                }
            }
        });

        mWebView.loadUrl(authorizeUri.toString());
        return view;
    }

    private void sendResult(String code) {
        mCallbacks.onCodeRetrieved(code);
    }
}
