package com.julo.android.redditpix.reddit;

import java.util.List;

/**
 * Created by julianlo on 1/12/16.
 */
public class Listing<T> {
    private List<T> mItems;
    private String mBefore;
    private String mAfter;

    public List<T> getItems() {
        return mItems;
    }

    public void setItems(List<T> items) {
        mItems = items;
    }

    public String getBefore() {
        return mBefore;
    }

    public void setBefore(String before) {
        mBefore = before;
    }

    public String getAfter() {
        return mAfter;
    }

    public void setAfter(String after) {
        mAfter = after;
    }
}
