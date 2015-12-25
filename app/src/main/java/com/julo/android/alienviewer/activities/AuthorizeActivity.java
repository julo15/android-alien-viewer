package com.julo.android.alienviewer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.julo.android.alienviewer.Preferences;
import com.julo.android.alienviewer.Session;
import com.julo.android.alienviewer.reddit.Reddit;
import com.julo.android.alienviewer.fragments.AuthorizeFragment;
import com.julo.android.alienviewer.reddit.User;

import org.json.JSONException;

import java.io.IOException;

/**
 * Created by julianlo on 12/12/15.
 */
public class AuthorizeActivity extends SingleFragmentActivity
    implements AuthorizeFragment.Callbacks {
    private static final String TAG = "AuthorizeActivity";

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AuthorizeActivity.class);
        return intent;
    }

    @Override
    public Fragment createFragment() {
        return AuthorizeFragment.newInstance();
    }

    @Override
    public void onCodeRetrieved(String code) {
        if (code != null) {
            new GetTokenTask().execute(code);
            return;
        } else {
            // If the user explicitly declines to log in, then we want to clear out the persisted
            // access token so that they can fetch posts anonymously.
            // If we didn't clear out the persisted token, then subsequent calls to fetchPosts(getAccessToken)
            // would cause an Reddit.AuthorizeException, potentially launching this activity again.
            finishActivity(null, null);
        }
    }

    private void finishActivity(Reddit.Tokens tokens, User user) {
        Session.getInstance().setNewTokens(tokens);
        Preferences.setUserName(AuthorizeActivity.this, (user != null) ? user.getName() : null);

        setResult(Activity.RESULT_OK);
        finish();
    }

    private class GetTokenTask extends AsyncTask<String,Void,Void> {
        private Reddit.Tokens mTokens;
        private User mUser;

        @Override
        protected Void doInBackground(String... params) {
            String code = params[0];
            try {
                mTokens = new Reddit(null).fetchTokens(code);
                mUser = new Reddit(mTokens).fetchUser();
            } catch (Reddit.AuthenticationException ae) {
                Log.e(TAG, "Failed to authenticate", ae);
            } catch (IOException ioe) {
                Log.e(TAG, "Failed to get token: ", ioe);
            } catch (JSONException je) {
                Log.e(TAG, "Failed to parse JSON: ", je);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            finishActivity(mTokens, mUser);
        }
    }
}
