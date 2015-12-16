package com.julo.android.alienviewer.util;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;

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

    public static final Reddit.PostFilterer IMAGE_POST_FILTERER = new Reddit.PostFilterer() {
        @Override
        public boolean filterPost(Post post) {
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
}
