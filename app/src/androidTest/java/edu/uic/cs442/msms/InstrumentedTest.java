package edu.uic.cs442.msms;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("edu.uic.cs442.msms", appContext.getPackageName());
    }

    @Test
    public void google_map_isService() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext);

        assertEquals(available, ConnectionResult.SUCCESS);
    }

    @Test
    public void location_isService() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        FusedLocationProviderClient mLocationProvider =
                LocationServices.getFusedLocationProviderClient(appContext);

        try {
            final Task location = mLocationProvider.getLastLocation();

            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    assertTrue(task.isSuccessful());
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void camera_isService() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();

        CameraManager manager = (CameraManager) appContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assertTrue(map != null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
