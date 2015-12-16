package com.julo.android.alienviewer;

import android.content.Context;

import com.julo.android.alienviewer.reddit.Subreddit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by julianlo on 12/11/15.
 */
public class RedditManager {

    private static RedditManager sInstance;

    private Context mContext;
    private List<Subreddit> mSubreddits;

    public static RedditManager get(Context context) {
        if (sInstance == null) {
            sInstance = new RedditManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private RedditManager(Context context) {
        mContext = context;
        mSubreddits = new ArrayList<>();

        Set<String> subredditNames = Preferences.getSubreddits(context);

        for (String name : subredditNames) {
            mSubreddits.add(new Subreddit(name));
        }

    }

    public List<Subreddit> getSubreddits() {
        return mSubreddits;
    }

    public void addSubreddit(String subredditName) {
        if (Preferences.addSubreddit(mContext, subredditName)) {
            mSubreddits.add(new Subreddit(subredditName));
        }
    }
}
