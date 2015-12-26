package com.julo.android.alienviewer.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.SearchView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;

import com.julo.android.alienviewer.Preferences;
import com.julo.android.alienviewer.imgur.Imgur;
import com.julo.android.alienviewer.reddit.Post;
import com.julo.android.alienviewer.reddit.Reddit;

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

    public static void recycleImageViewDrawable(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            if (bitmap != null) {
                bitmap.recycle();
                imageView.setImageDrawable(null);
            }
        }
    }
}
