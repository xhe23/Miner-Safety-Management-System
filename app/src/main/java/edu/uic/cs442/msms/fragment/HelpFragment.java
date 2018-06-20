package edu.uic.cs442.msms.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;

import edu.uic.cs442.msms.R;

/**
 * Created by xhe on 4/11/18.
 */

public class HelpFragment extends android.support.v4.app.DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_help, null);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
