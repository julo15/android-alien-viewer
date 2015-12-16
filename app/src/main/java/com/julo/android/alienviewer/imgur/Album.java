package com.julo.android.alienviewer.imgur;

import java.util.List;

/**
 * Created by julianlo on 12/12/15.
 */
public class Album {
    private String mTitle;
    private String mLink;
    private List<String> mImageUrls;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public List<String> getImageUrls() {
        return mImageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        mImageUrls = imageUrls;
    }
}
