package com.julo.android.redditpix.reddit;

import android.os.Parcelable;

import org.parceler.Parcel;

/**
 * Created by julianlo on 12/12/15.
 */
@Parcel
public class Post {
    String mId;
    String mTitle;
    String mUrl;
    String mImageUrl;
    String mCommentsUrl;
    String mSubredditName;
    int mCommentCount;
    int mKarmaCount;
    boolean mIsNsfw;
    int mCreatedUtc;
    Boolean mIsLiked; // true == upvoted, false == downvoted, null == no vote

    // Empty constructor needed for Parceler library
    public Post() {}

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    public String getCommentsUrl() {
        return mCommentsUrl;
    }

    public void setCommentsUrl(String commentsUrl) {
        mCommentsUrl = commentsUrl;
    }

    public String getSubredditName() {
        return mSubredditName;
    }

    public void setSubredditName(String subredditName) {
        mSubredditName = subredditName;
    }

    public int getCommentCount() {
        return mCommentCount;
    }

    public void setCommentCount(int commentCount) {
        mCommentCount = commentCount;
    }

    public int getKarmaCount() {
        return mKarmaCount;
    }

    public void setKarmaCount(int karmaCount) {
        mKarmaCount = karmaCount;
    }

    public boolean isNsfw() {
        return mIsNsfw;
    }

    public void setIsNsfw(boolean isNsfw) {
        mIsNsfw = isNsfw;
    }

    public int getCreatedUtc() {
        return mCreatedUtc;
    }

    public void setCreatedUtc(int createdUtc) {
        mCreatedUtc = createdUtc;
    }

    public Boolean isLiked() {
        return mIsLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        mIsLiked = isLiked;
    }
}
