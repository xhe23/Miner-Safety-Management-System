package edu.uic.cs442.msms.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import edu.uic.cs442.msms.App;
import edu.uic.cs442.msms.HomeActivity;
import edu.uic.cs442.msms.R;
import edu.uic.cs442.msms.adapter.RecognizeConceptsAdapter;
import edu.uic.cs442.msms.manager.DataManager;
import edu.uic.cs442.msms.model.DangerZone;
import edu.uic.cs442.msms.model.History;
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by jun on 3/9/18.
 */

public class ImageResultFragment extends SupportBlurDialogFragment {

    private static final String TAG = ImageResultFragment.class.getSimpleName();

    private static final String ARG_BYTES = "ARG_BYTES";

    private FusedLocationProviderClient mLocationProvider;
    private Boolean mLocationPermissionsGranted = true;

    // the list of results that were returned from the API
    RecyclerView resultsList;
    TextView resultsText;

    // the view where the image the user selected is displayed
    ImageView imageView;

    // switches between the text prompting the user to hit the take photo, and the loading spinner
    ViewSwitcher switcher;

    DataManager dataManager = DataManager.getInstance();

    @NonNull private final RecognizeConceptsAdapter adapter = new RecognizeConceptsAdapter();

    public static ImageResultFragment newInstance(byte[] imageBytes){
        Bundle args = new Bundle();
        args.putByteArray(ARG_BYTES, imageBytes);

        ImageResultFragment fragment = new ImageResultFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("StaticFieldLeak")
    private void onImagePicked(@NonNull final byte[] imageBytes) {

        setBusy(true);

        // Make sure we don't show a list of old concepts while the image is being uploaded
        adapter.setData(Collections.<Concept>emptyList());

        new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Concept>>>>() {
            @Override protected ClarifaiResponse<List<ClarifaiOutput<Concept>>> doInBackground(Void... params) {
                // The default Clarifai model that identifies concepts in images
                final ConceptModel generalModel = App.get().clarifaiClient().getDefaultModels().generalModel();

                // Use this model to predict, with the image that the user just selected as the input
                return generalModel.predict()
                        .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                        .executeSync();
            }

            @Override protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Concept>>> response) {
                setBusy(false);
                if (!response.isSuccessful()) {
                    Log.d(TAG, "!: " + R.string.error_while_contacting_api);
                    return;
                }
                final List<ClarifaiOutput<Concept>> predictions = response.get();
                if (predictions.isEmpty()) {
                    Log.d(TAG, "!: " + R.string.no_results_from_api);
                    return;
                }

                adapter.setData(predictions.get(0).data());

                analyzeData(predictions.get(0).data());

                resultsList.setAdapter(adapter);

                imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
            }

        }.execute();

    }

    private void analyzeData(List<Concept> concepts){
        //TODO: Analyze with DataManager

        float sumedData = 0;
        int length = 0;

        for(Concept concept : concepts){
            // Log.d(TAG, concept.name() + " and " + concept.value());

            length = concept.name().length();

            sumedData += dataManager.containsDanger(concept.name()) ? length * 50 * concept.value() : length * concept.value();
        }

         Log.d(TAG, "Total value: " + sumedData / 10);

        double total = sumedData / 10 % 100;

        String result = "Danger Level: " + String.format("%.2f", total) + "%";

        History history = new History(dataManager.getHistorySize(), total, "Flammable");    // Image Recognition set to Flammable by default
        dataManager.addHistory(history);

        resultsText.setText(result);
        resultsText.setVisibility(VISIBLE);

        if(total > 85){
            resultsText.setTextColor(getResources().getColor(R.color.fbutton_color_alizarin));
        } else {
            resultsText.setTextColor(getResources().getColor(R.color.fbutton_color_turquoise));
        }

        //TODO: If Danger, create marker
        //Will need mMap obejct to add marker
        if(total > 85)
            setDangerMarker(total);
    }

    private void setDangerMarker(final double total) {
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

                            final LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            // set marker on this location
                            setMarker(myLocation, total);

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

    private void setMarker(LatLng location, double total){
        // Log.d(TAG, "Setting marker at: " + location.longitude + ", " + location.latitude);

        String totalPercent = String.format("%.2f", total) + "%";

        DangerZone zone = new DangerZone(location, "Flammable", totalPercent);

        ((HomeActivity)getActivity()).setMarkerFromImageResult(zone);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        byte[] imageBytes = getArguments().getByteArray(ARG_BYTES);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_recognize, null);

        resultsList = v.findViewById(R.id.resultsList);
        resultsText = v.findViewById(R.id.resultsText);
        imageView = v.findViewById(R.id.image);
        switcher = v.findViewById(R.id.switcher);

        onImagePicked(imageBytes);

        resultsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    private void setBusy(final boolean busy) {
        switcher.setVisibility(busy ? VISIBLE : GONE);
        imageView.setVisibility(busy ? GONE : VISIBLE);
    }
}
