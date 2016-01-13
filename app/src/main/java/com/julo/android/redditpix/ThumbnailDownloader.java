package com.julo.android.redditpix;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.julo.android.redditpix.reddit.Post;
import com.julo.android.redditpix.reddit.Reddit;
import com.julo.android.redditpix.util.Util;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by julianlo on 12/12/15.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";

    private static final int MESSAGE_DOWNLOAD = 0;

    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, String thumbnailUrl);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T)msg.obj;
                    Log.v(TAG, "Got a request for subreddit: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    public void queueThumbnail(T target, String subreddit) {
        Log.v(TAG, "Got a subreddit: " + subreddit);

        if (subreddit == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, subreddit);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final T target) {
        try {
            final String subreddit = mRequestMap.get(target);
            List<Post> posts = new Reddit(null)
                    .fetchPosts(subreddit, 5, Util.IMAGE_POST_FILTERER)
                    .getItems();

            if (posts.size() > 0) {
                final String imageUrl = posts.get(0).getImageUrl();
                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mRequestMap.get(target) != subreddit) {
                            return;
                        }

                        mRequestMap.remove(target);
                        mThumbnailDownloadListener.onThumbnailDownloaded(target, imageUrl);
                    }
                });
                return;
            }
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse post JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch posts", ioe);
        }
        mRequestMap.remove(target);
    }
}
