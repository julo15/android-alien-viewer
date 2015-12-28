package com.julo.android.redditpix.reddit;

/**
 * Created by julianlo on 12/11/15.
 */
public class Subreddit {

    private String mName;
    private String mTitle;
    private String mImageUrl;
    private int mSubscribers;
    private int mTopPostUtc;
    private boolean mOver18;
    private boolean mLoaded;

    public Subreddit(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public int getSubscribers() {
        return mSubscribers;
    }

    public void setSubscribers(int subscribers) {
        mSubscribers = subscribers;
    }

    public int getTopPostUtc() {
        return mTopPostUtc;
    }

    public void setTopPostUtc(int topPostUtc) {
        mTopPostUtc = topPostUtc;
    }

    public boolean isOver18() {
        return mOver18;
    }

    public void setOver18(boolean over18) {
        mOver18 = over18;
    }

    public boolean isLoaded() {
        return mLoaded;
    }

    public void setLoaded(boolean loaded) {
        mLoaded = loaded;
    }
}
