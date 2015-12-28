package com.julo.android.redditpix.reddit;

/**
 * Created by julianlo on 12/13/15.
 */
public class User {
    private String mName;
    private String mId;
    private int mLinkKarma;
    private int mCommentKarma;
    private boolean mIsOver18;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public int getLinkKarma() {
        return mLinkKarma;
    }

    public void setLinkKarma(int linkKarma) {
        mLinkKarma = linkKarma;
    }

    public int getCommentKarma() {
        return mCommentKarma;
    }

    public void setCommentKarma(int commentKarma) {
        mCommentKarma = commentKarma;
    }

    public boolean isOver18() {
        return mIsOver18;
    }

    public void setIsOver18(boolean isOver18) {
        mIsOver18 = isOver18;
    }
}
