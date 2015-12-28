package com.julo.android.redditpix;

import com.julo.android.redditpix.reddit.Post;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by julianlo on 12/13/15.
 */
public class PostCache {

    public static final int OWNERID_POST_IMAGE_PAGER_ACTIVITY = 1;

    private static PostCache sInstance;

    private ConcurrentHashMap<Integer,ConcurrentMap<String,Post>> mCache = new ConcurrentHashMap<>();

    public static PostCache get() {
        if (sInstance == null) {
            sInstance = new PostCache();
        }
        return sInstance;
    }

    public void initCache(int ownerId, List<Post> posts) {
        ConcurrentMap<String,Post> cache = new ConcurrentHashMap<>(posts.size());

        for (Post post : posts) {
            cache.put(post.getUrl(), post);
        }

        mCache.put(ownerId, cache);
    }

    public Post getPost(int owner, String url) {
        return mCache.get(owner).get(url);
    }

    public void removeCache(int owner) {
        mCache.remove(owner);
    }
}
