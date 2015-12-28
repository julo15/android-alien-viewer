package com.julo.android.redditpix.imgur;

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/12/15.
 */
public class Imgur {
    private static final String CLIENT_ID = "b33a84be57101f9";
    private static final Uri API_ENDPOINT = Uri.parse("https://api.imgur.com/3");

    private OkHttpClient mHttpClient = new OkHttpClient();

    private Request.Builder newAPIRequestBuilder() {
        return new Request.Builder()
                .addHeader("Authorization", "Client-ID " + CLIENT_ID);
    }

    public Album fetchAlbum(String id) throws IOException, JSONException {
        String url = API_ENDPOINT
                .buildUpon()
                .appendPath("album")
                .appendPath(id)
                .build()
                .toString();
        Request request = newAPIRequestBuilder()
                .url(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        Album album = new Album();
        parseAlbum(album, new JSONObject(response.body().string()).getJSONObject("data"));
        return album;
    }

    private void parseAlbum(Album album, JSONObject dataJson) throws JSONException {
        album.setTitle(dataJson.getString("title"));
        album.setLink(dataJson.getString("link"));

        JSONArray imagesJsonArray = dataJson.getJSONArray("images");
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < imagesJsonArray.length(); i++) {
            imageUrls.add(imagesJsonArray.getJSONObject(i).getString("link"));
        }
        album.setImageUrls(imageUrls);
    }

    public String fetchImage(String id) throws IOException, JSONException {
        String url = API_ENDPOINT
                .buildUpon()
                .appendPath("image")
                .appendPath(id)
                .build()
                .toString();
        Request request = newAPIRequestBuilder()
                .url(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        String body = response.body().string();
        try {
            String imageUrl = new JSONObject(body)
                    .getJSONObject("data")
                    .getString("link");
            return imageUrl;
        } catch (Exception e) {
            throw e;
        }
    }

    public static String extractAlbumIdFromUrl(String url) {
        // http:\\/\\/imgur\\.com\\/a\\/[a-zA-Z0-9]{3,}$
        if (url.matches("http:\\/\\/imgur\\.com\\/a\\/[a-zA-Z0-9]{3,}$")) { // http://imgur.com/a/UXmyx
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        } else {
            return null;
        }
    }

    public static String extractImageIdFromUrl(String url) {
        // http:\/\/imgur\.com\/[a-zA-Z0-9]{6}$ // http:\\/\\/imgur\\.com\\/[a-zA-Z0-9]{3,}$
        if (url.matches("http:\\/\\/imgur\\.com\\/[a-zA-Z0-9]{3,}$")) { // http://imgur.com/6EOaPzn
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        } else {
            return null;
        }
    }
}
