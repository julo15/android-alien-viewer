package com.julo.android.alienviewer;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by julianlo on 12/11/15.
 */
public class Preferences {
    private static final String TAG = "Preferences";

    private static final String PREF_SUBREDDITS = "subreddits";
    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String PREF_NSFW_ALLOWED = "nsfw_allowed";
    private static final String PREF_USER_NAME = "user_name";

    public static Set<String> getSubreddits(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(PREF_SUBREDDITS, new HashSet<String>());
    }

    public static boolean addSubreddit(Context context, String subreddit) {
        Set<String> subreddits = getSubreddits(context);
        int beforeSize = subreddits.size();
        Set<String> copy = new HashSet<>(subreddits);
        copy.add(subreddit);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREF_SUBREDDITS, copy)
                .commit();
        return (beforeSize != copy.size());
    }

    public static void removeSubreddit(Context context, String subreddit) {
        Set<String> subreddits = getSubreddits(context);
        subreddits.remove(subreddit);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putStringSet(PREF_SUBREDDITS, subreddits)
                .commit();
    }

    public static String getAccessToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACCESS_TOKEN, null);
    }

    public static void setAccessToken(Context context, String accessToken) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .commit();
    }

    public static String getRefreshToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_REFRESH_TOKEN, null);
    }

    public static void setRefreshToken(Context context, String accessToken) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_REFRESH_TOKEN, accessToken)
                .commit();
    }

    public static boolean isNsfwAllowed(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_NSFW_ALLOWED, false);
    }

    public static void setNsfwAllowed(Context context, boolean allowed) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_NSFW_ALLOWED, allowed)
                .commit();
    }

    public static String getUserName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_USER_NAME, null);
    }

    public static void setUserName(Context context, String name) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_USER_NAME, name)
                .commit();
    }
}
