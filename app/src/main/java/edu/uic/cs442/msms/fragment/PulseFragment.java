package edu.uic.cs442.msms.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import edu.uic.cs442.msms.R;
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment;

/**
 * Created by jun on 2/21/18.
 */

public class PulseFragment extends SupportBlurDialogFragment {


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_pulse, null);



        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }
}
