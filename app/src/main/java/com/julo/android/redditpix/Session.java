package com.julo.android.redditpix;

import android.content.Context;

import com.julo.android.redditpix.reddit.Reddit;
import com.julo.android.redditpix.util.Util;

/**
 * Created by julianlo on 12/25/15.
 */
public class Session implements Reddit.Listener {
    private static Session sSession;

    private Reddit mReddit;
    private Context mAppContext;

    public static void initInstance(Context appContext) {
        sSession = new Session(appContext);
    }

    public static Session getInstance() {
        return sSession;
    }

    private Session(Context appContext) {
        mAppContext = appContext;
        bindReddit(Util.getRedditTokensFromPreferences(mAppContext));
    }

    private void bindReddit(Reddit.Tokens tokens) {
        if (mReddit != null) {
            mReddit.setListener(null);
        }
        mReddit = new Reddit(tokens);
        mReddit.setListener(this);
    }

    public Reddit getReddit() {
        return mReddit;
    }

    public void setNewTokens(Reddit.Tokens tokens) {
        bindReddit(tokens);
        persistTokens(tokens);
    }

    @Override
    public void onTokensChange(Reddit.Tokens tokens) {
        persistTokens(tokens);
    }

    private void persistTokens(Reddit.Tokens tokens) {
        Util.setRedditTokensToPreferences(mAppContext, tokens);
    }
}
