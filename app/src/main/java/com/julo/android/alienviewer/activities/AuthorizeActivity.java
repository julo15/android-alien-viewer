package com.julo.android.alienviewer.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.julo.android.alienviewer.Preferences;
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

    public static final String EXTRA_ACCESS_TOKEN = "com.julo.android.alienviewer.access_token"; // output

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
        }
    }

    private class GetTokenTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            String code = params[0];
            try {
                String accessToken = new Reddit(null).fetchAccessToken(AuthorizeActivity.this, code);
                Preferences.setAccessToken(AuthorizeActivity.this, accessToken);

                User user = new Reddit(accessToken).fetchUser();
                Preferences.setUserName(AuthorizeActivity.this, user.getName());

                return accessToken;
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
        protected void onPostExecute(String accessToken) {
            Intent data = new Intent();
            data.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
            setResult((accessToken != null) ? Activity.RESULT_OK : Activity.RESULT_CANCELED, data);
            finish();
        }
    }
}
