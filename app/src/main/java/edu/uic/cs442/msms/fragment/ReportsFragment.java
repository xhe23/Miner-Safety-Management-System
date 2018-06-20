package edu.uic.cs442.msms.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.uic.cs442.msms.HomeActivity;
import edu.uic.cs442.msms.R;
import edu.uic.cs442.msms.manager.DataManager;
import edu.uic.cs442.msms.model.DangerZone;
import edu.uic.cs442.msms.model.History;

/**
 * Created by jun on 4/8/18.
 */

public class ReportsFragment extends DialogFragment{

    private static final String TAG = ReportsFragment.class.getSimpleName();

    private FusedLocationProviderClient mLocationProvider;
    private Boolean mLocationPermissionsGranted = true;

    DataManager dataManager = DataManager.getInstance();

    private TextView locationText;

    private Spinner typeSpinner;
    private Spinner percentSpinner;

    public Dialog onCreateDialog(Bundle state){

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_reports, null);

        final TextView dateText = v.findViewById(R.id.edit_date);
        final TextView timeText = v.findViewById(R.id.edit_time);

        locationText = v.findViewById(R.id.location_view);

        typeSpinner = v.findViewById(R.id.type_spinner);
        final ArrayAdapter mtypeAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.type, android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(mtypeAdapter);

        percentSpinner = v.findViewById(R.id.percent_spinner);
        ArrayList<Integer> percents = new ArrayList<>();
        for(int i = 100; i >= 0; i--){
            percents.add(i);
        }
        final ArrayAdapter mpercentAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, percents);
        mpercentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item );
        percentSpinner.setAdapter(mpercentAdapter);

        // final EditText commentText = v.findViewById(R.id.edit_comment);
        final Button submit = v.findViewById(R.id.report_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Report Submit Success!", Toast.LENGTH_SHORT).show();

                String type = typeSpinner.getSelectedItem().toString();
                String percent = percentSpinner.getSelectedItem().toString();

                History history = new History(dataManager.getHistorySize(), Double.valueOf(percent), type);
                dataManager.addHistory(history);

                Double total = Double.valueOf(percent);
                if(total > 85){
                    String totalPercent = String.format("%.2f", total) + "%";

                    DangerZone zone = new DangerZone(myLocation, type, totalPercent);
                    ((HomeActivity)getActivity()).setMarkerFromImageResult(zone);
                }
            }
        });


        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy", Locale.US);
        DateFormat df2 = new SimpleDateFormat("HH:mm", Locale.US);

        String date = df.format(Calendar.getInstance().getTime());
        String time = df2.format(Calendar.getInstance().getTime());

        dateText.setText(date);
        timeText.setText(time);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
    }

    LatLng myLocation = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDeviceLocation();
    }

    private void getDeviceLocation() {
        mLocationProvider = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if (mLocationPermissionsGranted) {
                final Task location = mLocationProvider.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "current location found");
                            Location currentLocation = (Location) task.getResult();

                            myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            // set location text
                            setLocation(myLocation);

                        } else {
                            Log.d(TAG, "current location is null");
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    private void setLocation(LatLng location){
        String loc = location.latitude + ", " + location.longitude;
        locationText.setText(loc);
    }




}
