package com.julo.android.redditpix.reddit;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by julianlo on 12/12/15.
 */
public class Post implements Parcelable {
    private String mId;
    private String mTitle;
    private String mUrl;
    private String mImageUrl;
    private String mCommentsUrl;
    private String mSubredditName;
    private int mCommentCount;
    private int mKarmaCount;
    private boolean mIsNsfw;
    private int mCreatedUtc;
    private Boolean mIsLiked; // true == upvoted, false == downvoted, null == no vote

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public Post() {}

    private Post(Parcel in) {
        mId = in.readString();
        mTitle = in.readString();
        mUrl = in.readString();
        mImageUrl = in.readString();
        mCommentsUrl = in.readString();
        mSubredditName = in.readString();
        mCommentCount = in.readInt();
        mKarmaCount = in.readInt();
        boolean[] boolArray = new boolean[1];
        in.readBooleanArray(boolArray);
        mIsNsfw = boolArray[0];
        mCreatedUtc = in.readInt();
        mIsLiked = (Boolean)in.readSerializable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mTitle);
        dest.writeString(mUrl);
        dest.writeString(mImageUrl);
        dest.writeString(mCommentsUrl);
        dest.writeString(mSubredditName);
        dest.writeInt(mCommentCount);
        dest.writeInt(mKarmaCount);
        dest.writeBooleanArray(new boolean[]{mIsNsfw});
        dest.writeInt(mCreatedUtc);
        dest.writeSerializable(mIsLiked);
    }

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
