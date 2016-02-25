package com.julo.android.redditpix.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.SearchView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;

import com.julo.android.redditpix.Preferences;
import com.julo.android.redditpix.imgur.Imgur;
import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.reddit.Reddit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by julianlo on 12/11/15.
 */
public class Util {

    public static <T> T findView(View view, int id) {
        return (T)view.findViewById(id);
    }

    public static <T> T findView(Activity activity, int id) {
        return (T)activity.findViewById(id);
    }

    public static void showView(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public static <T> T cast(Object o) {
        return (T)o;
    }

    public static boolean isImageUrl(String url) {
        String urlLower = url.toLowerCase();
        String[] imageExtensions = {
                ".jpg",
                ".png",
                ".jpeg",
                ".gif",
        };

        for (String extension : imageExtensions) {
            if (urlLower.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static final Reddit.Filterer<Post> IMAGE_POST_FILTERER = new Reddit.Filterer<Post>() {
        @Override
        public boolean filter(Post post) {
            return (post.getImageUrl() != null);
        }
    };

    public static boolean isImgurAlbumUrl(String url) {
        return (Imgur.extractAlbumIdFromUrl(url) != null);
    }

    public static CharSequence getRelativeTimeString(Context context, int utc) {
        return DateUtils.getRelativeDateTimeString(context,
                utc * 1000L,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS,
                0);
    }

    public static String ensureUrlHasRedditPrefix(String url) {
        return Reddit.ENDPOINT.toString() + url;
    }

    public static void hideSearchView(SearchView searchView) {
        // Hide the keyboard.
        searchView.clearFocus();

        // Just calling setIconified(true) will clear the query, but not collapse the SearchView.
        // This combo of setQuery + setIconified does work, although not exactly sure why.
        searchView.setQuery("", false);
        searchView.setIconified(true);
    }

    public static Reddit.Tokens getRedditTokensFromPreferences(Context context) {
        String accessToken = Preferences.getAccessToken(context);
        String refreshToken = Preferences.getRefreshToken(context);
        return (accessToken != null) ? new Reddit.Tokens(accessToken, refreshToken) : null;
    }

    public static void setRedditTokensToPreferences(Context context, Reddit.Tokens tokens) {
        Preferences.setAccessToken(context, (tokens != null) ? tokens.getAccessToken() : null);
        Preferences.setRefreshToken(context, (tokens != null) ? tokens.getRefreshToken() : null);
    }

    public static Boolean getBigBooleanFromJsonObject(JSONObject object, String field) throws JSONException {
        if (object.has(field) && object.isNull(field)) {
            return null;
        }
        return object.getBoolean(field);
    }

    public static int convertLikeToVote(Boolean isLiked) {
        if (isLiked == null) {
            return Reddit.VOTE_UNVOTE;
        }
        return (isLiked.booleanValue() ? Reddit.VOTE_UP : Reddit.VOTE_DOWN);
    }

    public static Boolean convertVoteToLike(int vote) {
        switch (vote) {
            case Reddit.VOTE_UP:
                return Boolean.TRUE;
            case Reddit.VOTE_DOWN:
                return Boolean.FALSE;
            case Reddit.VOTE_UNVOTE:
                return null;
            default:
                throw new IllegalArgumentException("Expected one of Reddit.VOTE_UP, Reddit.VOTE_DOWN, or Reddit.VOTE_UNVOTE");
        }
    }
}
