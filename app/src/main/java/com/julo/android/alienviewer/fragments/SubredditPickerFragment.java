package com.julo.android.alienviewer.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.julo.android.alienviewer.R;
import com.julo.android.alienviewer.util.Util;

/**
 * Created by julianlo on 12/11/15.
 */
public class SubredditPickerFragment extends DialogFragment {

    public static final String EXTRA_SUBREDDIT = "com.julo.android.alienviewer.subreddit";

    private EditText mSubredditEditText;

    public static SubredditPickerFragment newInstance() {
        return new SubredditPickerFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_subreddit_picker, null);

        mSubredditEditText = Util.findView(view, R.id.fragment_subreddit_picker_subreddit_edit_text);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK, mSubredditEditText.getText().toString());
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, String subreddit) {
        Fragment targetFragment = getTargetFragment();

        if (targetFragment == null) {
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_SUBREDDIT, subreddit);

        targetFragment.onActivityResult(getTargetRequestCode(), resultCode, data);
    }
}
