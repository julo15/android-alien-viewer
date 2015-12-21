package com.julo.android.alienviewer.reddit;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.julo.android.alienviewer.imgur.Imgur;
import com.julo.android.alienviewer.util.Util;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julianlo on 12/11/15.
 */
public class Reddit {
    private static final String TAG = "Reddit";

    public static final String CLIENT_ID = "8ETjWlUdiA0jeA";
    public static final String REDIRECT_URI = "http://localhost";

    public static final Uri ENDPOINT = Uri.parse("https://www.reddit.com");
    private static final Uri API_ENDPOINT = ENDPOINT
            .buildUpon()
            .appendPath("api")
            .appendPath("v1")
            .build();
    private static final Uri AUTHORIZE_URI = API_ENDPOINT
            .buildUpon()
            .appendPath("authorize.compact")
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .build();
    private static final Uri ACCESS_TOKEN_API = API_ENDPOINT
            .buildUpon()
            .appendPath("access_token")
            .build();

    private static final Uri OAUTH_ENDPOINT = Uri.parse("https://oauth.reddit.com");
    private static final Uri OAUTH_API_ENDPOINT = OAUTH_ENDPOINT
            .buildUpon()
            .appendPath("api")
            .appendPath("v1")
            .build();

    private static final Uri MY_SUBREDDITS_API = OAUTH_ENDPOINT
            .buildUpon()
            .appendPath("subreddits")
            .appendPath("mine")
            .build();

    public interface PostFilterer {
        boolean filterPost(Post post);
    }

    public interface Filterer<T> {
        boolean filter(T item);
    }

    public static class Tokens {
        private String mAccessToken;
        private String mRefreshToken;

        public Tokens(String access, String refresh) {
            if (access == null) {
                throw new IllegalArgumentException("Can't have null access token");
            }

            mAccessToken = access;
            mRefreshToken = refresh;
        }

        public String getAccessToken() {
            return mAccessToken;
        }

        public String getRefreshToken() {
            return mRefreshToken;
        }
    }

    public static class AuthenticationException extends Exception {
    }

    private interface ListingItemParser<T> {
        T parseItem(JSONObject itemJsonObject) throws IOException, JSONException;
    }

    private OkHttpClient mHttpClient = new OkHttpClient();
    private Tokens mTokens;

    public Reddit(Tokens tokens) {
        mTokens = tokens;

        mHttpClient.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                if (mTokens == null || mTokens.getRefreshToken() == null) {
                    return null;
                }

                try {
                    fetchRefreshedAccessToken(mTokens.getRefreshToken());
                } catch (JSONException je) {
                    Log.e(TAG, "Got JSON exception attempting to refresh access token in authenticator", je);
                    return null;
                }

                return response.request().newBuilder()
                        .header("Authorization", "bearer " + mTokens.getAccessToken())
                        .build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        Request request = new Request.Builder()
                .url(urlSpec)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        return response.body().bytes();
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public JSONObject getUrlJSONObject(String urlSpec) throws IOException, JSONException {
        return new JSONObject(getUrlString(urlSpec));
    }

    public User fetchUser() throws JSONException, IOException, AuthenticationException {
        String url = OAUTH_API_ENDPOINT
                .buildUpon()
                .appendPath("me")
                .build()
                .toString();
        Request request = newAccessTokenRequestBuilder(url)
                .build();
        Response response = mHttpClient.newCall(request).execute();
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new AuthenticationException();
        } else if (response.code() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP failure response: " + response.code());
        }

        JSONObject responseJsonObject = new JSONObject(response.body().string());
        User user = new User();
        user.setName(responseJsonObject.getString("name"));
        user.setId(responseJsonObject.getString("id"));
        user.setLinkKarma(responseJsonObject.getInt("link_karma"));
        user.setCommentKarma(responseJsonObject.getInt("comment_karma"));
        user.setIsOver18(responseJsonObject.getBoolean("over_18"));
        return user;
    }

    public void loadSubreddit(Subreddit subreddit) {
        if (subreddit.isLoaded()) {
            return;
        }

        try {
            String url = ENDPOINT
                    .buildUpon()
                    .appendPath("r")
                    .appendPath(subreddit.getName())
                    .appendPath("about.json")
                    .build()
                    .toString();
            JSONObject jsonData = getUrlJSONObject(url).getJSONObject("data");
            parseSubreddit(subreddit, jsonData);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to load subreddit", ioe);
        }
    }

    private Uri getMySubredditsUri(String where) {
        return MY_SUBREDDITS_API
                .buildUpon()
                .appendPath(where)
                .build();
    }

    private Request.Builder newAccessTokenRequestBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .header("Authorization", "bearer " + mTokens.getAccessToken());
    }

    public List<Subreddit> fetchSubscribedSubreddits(int limit, final Filterer<Subreddit> filterer) throws IOException, JSONException, AuthenticationException {
        String url = getMySubredditsUri("subscriber")
                .buildUpon()
                .appendQueryParameter("limit", String.valueOf(limit))
                .toString();
        Request request = newAccessTokenRequestBuilder(url).build();
        Response response = mHttpClient.newCall(request).execute();
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new AuthenticationException();
        } else if (response.code() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP failure response: " + response.code());
        }

        List<Subreddit> subreddits = parseListingItems(response.body().string(), new ListingItemParser<Subreddit>() {
            @Override
            public Subreddit parseItem(JSONObject itemJsonObject) throws JSONException {
                Log.v(TAG, "Processing subreddit JSON: " + itemJsonObject.toString());
                Subreddit subreddit = new Subreddit(itemJsonObject.getString("display_name"));
                parseSubreddit(subreddit, itemJsonObject);
                if ((filterer == null) || filterer.filter(subreddit)) {
                    return subreddit;
                } else {
                    return null;
                }
            }
        });
        return subreddits;
    }

    private void parseSubreddit(Subreddit subreddit, JSONObject jsonData) throws JSONException {
        subreddit.setTitle(jsonData.getString("title"));
        subreddit.setSubscribers(jsonData.getInt("subscribers"));
        subreddit.setOver18(jsonData.getBoolean("over18"));
        subreddit.setLoaded(true);
    }

    public Uri getAuthorizeUri(boolean permanent, String scopes) {
        return AUTHORIZE_URI
                .buildUpon()
                .appendQueryParameter("state", "asdf")
                .appendQueryParameter("duration", permanent ? "permanent" : "temporary")
                .appendQueryParameter("scope", scopes)
                .build();
    }

    public Tokens fetchTokens(String code) throws IOException, JSONException {
        RequestBody body = new FormEncodingBuilder()
                .add("grant_type", "authorization_code")
                .add("redirect_uri", REDIRECT_URI)
                .add("code", code)
                .build();
        Request request = new Request.Builder()
                .url(ACCESS_TOKEN_API.toString())
                .header("Authorization", Credentials.basic(CLIENT_ID, ""))
                .post(body)
                .build();
        Response response = mHttpClient.newCall(request).execute();

        String responseBody = response.body().string();
        Log.i(TAG, "Got access token response: " + responseBody);

        JSONObject jsonData = new JSONObject(responseBody);
        mTokens = new Tokens(jsonData.getString("access_token"),
                jsonData.optString("refresh_token", null));
        return mTokens;
    }

    private Tokens fetchRefreshedAccessToken(String refreshToken) throws IOException, JSONException {
        RequestBody body = new FormEncodingBuilder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .build();
        Request request = new Request.Builder()
                .url(ACCESS_TOKEN_API.toString())
                .header("Authorization", Credentials.basic(CLIENT_ID, ""))
                .post(body)
                .build();
        Response response = mHttpClient.newCall(request).execute();

        String responseBody = response.body().string();
        Log.i(TAG, "Got refreshed access token response: " + responseBody);

        JSONObject jsonData = new JSONObject(responseBody);
        mTokens = new Tokens(jsonData.getString("access_token"), mTokens.getRefreshToken());
        return mTokens;
    }

    public List<Post> fetchPosts(String subreddit) throws IOException, JSONException {
        return fetchPosts(subreddit, null);
    }

    public List<Post> fetchPosts(String subreddit, final PostFilterer filterer) throws IOException, JSONException {
        return fetchPosts(subreddit, null, filterer);
    }

    public List<Post> fetchPosts(String subreddit, Integer limit, final PostFilterer filterer) throws IOException, JSONException {
        Uri.Builder uriBuilder = ENDPOINT
                .buildUpon()
                .appendPath("r")
                .appendPath(subreddit)
                .appendPath("hot.json");

        if (limit != null) {
            uriBuilder.appendQueryParameter("limit", String.valueOf(limit.intValue()));
        }

        String url = uriBuilder.build().toString();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            return fetchPosts(request, filterer);
        } catch (AuthenticationException ae) {
            Log.e(TAG, "Unexpected authentication exception", ae);
            return null;
        }
    }

    public List<Post> fetchPosts(Integer limit, final PostFilterer filterer)
            throws AuthenticationException, IOException, JSONException {
        boolean useOAuth = (mTokens != null);
        Uri baseUri = useOAuth ? OAUTH_ENDPOINT : ENDPOINT;
        Uri.Builder uriBuilder = baseUri
                .buildUpon()
                .appendPath("hot.json");

        if (limit != null) {
            uriBuilder.appendQueryParameter("limit", String.valueOf(limit.intValue()));
        }

        String url = uriBuilder.build().toString();
        Request request = (useOAuth ? newAccessTokenRequestBuilder(url) : new Request.Builder().url(url))
                .build();
        return fetchPosts(request, filterer);
    }

    private List<Post> fetchPosts(Request request, final PostFilterer filterer)
            throws AuthenticationException, JSONException, IOException {
        Response response = mHttpClient.newCall(request).execute();
        String responseBody = response.body().string();
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            throw new AuthenticationException();
        } else if (response.code() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP failure response: " + response.code());
        }

        Log.v(TAG, "Got posts response: " + responseBody);
        return parseListingItems(responseBody, new ListingItemParser<Post>() {
            @Override
            public Post parseItem(JSONObject itemJsonObject) throws IOException, JSONException {
                Log.v(TAG, "Parsing post JSON:" + itemJsonObject.toString());
                Post post = parsePost(itemJsonObject);
                if ((post != null) && ((filterer == null) || filterer.filterPost(post))) {
                    return post;
                } else {
                    return null;
                }
            }
        });
    }

    public static Post parsePost(JSONObject itemJsonObject) throws IOException, JSONException {
        Post post = new Post();
        post.setTitle(itemJsonObject.getString("title"));
        post.setUrl(itemJsonObject.getString("url"));
        post.setCommentsUrl(itemJsonObject.getString("permalink"));
        post.setSubredditName(itemJsonObject.getString("subreddit"));
        post.setCommentCount(itemJsonObject.getInt("num_comments"));
        post.setKarmaCount(itemJsonObject.getInt("score"));
        post.setIsNsfw(itemJsonObject.getBoolean("over_18"));
        post.setCreatedUtc(itemJsonObject.getInt("created_utc"));
        try {
            post.setImageUrl(determinePostImageUrl(post.getUrl()));
        } catch (Exception e) {
            Log.w(TAG, "Failed to get image url from post (" + post.getUrl() + ")", e); // failed ok
        }
        return post;
    }

    private <T> List<T> parseListingItems(String responseBody, ListingItemParser<T> parser) throws IOException, JSONException {
        JSONArray itemsJsonArray = new JSONObject(responseBody)
                .getJSONObject("data")
                .getJSONArray("children");
        List<T> items = new ArrayList<>();
        for (int i = 0; i < itemsJsonArray.length(); i++) {
            JSONObject itemJsonObject = itemsJsonArray.getJSONObject(i).getJSONObject("data");
            T item = parser.parseItem(itemJsonObject);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private static String determinePostImageUrl(String postUrl) throws IOException, JSONException {
        if (Util.isImageUrl(postUrl)) {
            return postUrl;
        }

        String id = Imgur.extractImageIdFromUrl(postUrl);
        if (id != null) {
            return new Imgur().fetchImage(id);
        }

        id = Imgur.extractAlbumIdFromUrl(postUrl);
        if (id != null) {
            return new Imgur().fetchAlbum(id).getImageUrls().get(0);
        }

        return null;
    }
}
