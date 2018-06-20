package edu.uic.cs442.msms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import edu.uic.cs442.msms.fragment.DataExploreFragment;
import edu.uic.cs442.msms.fragment.EquipmentFragment;
import edu.uic.cs442.msms.fragment.HelpFragment;
import edu.uic.cs442.msms.fragment.ImageResultFragment;
import edu.uic.cs442.msms.fragment.ReportsFragment;
import edu.uic.cs442.msms.manager.DataManager;
import edu.uic.cs442.msms.model.DangerZone;
import edu.uic.cs442.msms.model.History;

import static edu.uic.cs442.msms.fragment.EquipmentFragment.maxNumberOfLines;
import static edu.uic.cs442.msms.fragment.EquipmentFragment.numberOfPoints;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    // Google Maps 
    private GoogleMap mMap;
    private FusedLocationProviderClient mLocationProvider;
    private Boolean mLocationPermissionsGranted = false;

    // Center Layout
    private RelativeLayout cameraView;
    private TextureView textureView;
    private FrameLayout mapLayout;
    
    // Buttons
    private EasyFlipView pulseButton;
    private EasyFlipView gasButton;
    private EasyFlipView geoView;
    private EasyFlipView equipmentButton;

    private FragmentManager manager = getSupportFragmentManager();
    private DataManager dataManager = DataManager.getInstance();

    // check state orientation of output image
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    // Camera
    private String cameraID;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size ImageDimension;
    private ImageReader imageReader;

    // Save to FILE
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        cameraView = findViewById(R.id.camera_view);
        textureView = findViewById(R.id.texture_view);
        mapLayout = findViewById(R.id.map_layout);

        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        final Button captureButton = findViewById(R.id.button_capture);
        captureButton.setOnClickListener(buttonListener);

        final LinearLayout reportsLayout = findViewById(R.id.reports_layout);
        reportsLayout.setOnClickListener(buttonListener);

        final LinearLayout dataExploreLayout = findViewById(R.id.data_explore);
        dataExploreLayout.setOnClickListener(buttonListener);

        final LinearLayout helpLayout = findViewById(R.id.help);
        helpLayout.setOnClickListener(buttonListener);

        //pulseButton = findViewById(R.id.button_pulse);
        //pulseButton.setOnFlipListener(flipListener);

        gasButton = findViewById(R.id.button_gas);
        gasButton.setOnFlipListener(flipListener);

        geoView = findViewById(R.id.button_geo);
        geoView.setOnFlipListener(flipListener);

        equipmentButton = findViewById(R.id.button_equipment);
        equipmentButton.setOnFlipListener(flipListener);

        getLocationPermission();

        //-------- pre-defined values ------------//
        setPredefinedDangerZones();
        setPredefinedHistory();
        setPredefinedDangerValue();
        setPredefinedEquipmentValue();
    }

    //------------------------------------------------------------ Button Setting ------------------------------------------------------------//
    // TODO: Button Actions Go HERE!!
    View.OnClickListener buttonListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.button_capture:
                    takePicture();
                    break;

                case R.id.reports_layout:

                    ReportsFragment dialog = new ReportsFragment();

                    dialog.show(manager, "REPORTS_DIALOG");

                    break;
                case R.id.data_explore:

                    DataExploreFragment dataExploreDialog = new DataExploreFragment();
                    dataExploreDialog.show(manager, "DATA_EXPLORE_DIALOG");

                    break;

                case R.id.help:
                    HelpFragment helpDialog = new HelpFragment();
                    helpDialog.show(manager, "HELP_DIALOG");

                    break;
            }
        }
    };

    // TODO: Flip Actions Go HERE!!
    EasyFlipView.OnFlipAnimationListener flipListener = new EasyFlipView.OnFlipAnimationListener() {
        @Override
        public void onViewFlipCompleted(EasyFlipView easyFlipView, EasyFlipView.FlipState newCurrentSide) {

            switch (easyFlipView.getId()){

                case R.id.button_gas:

                    List<Marker> markers = dataManager.getMarkers();

                    for(Marker marker : markers){
                        if(marker.isVisible()){
                            marker.setVisible(false);
                        } else {
                            marker.setVisible(true);
                        }
                    }

                    break;

                case R.id.button_geo:

                    if(cameraView.getVisibility() == View.VISIBLE){
                        cameraView.setVisibility(View.INVISIBLE);
                        mapLayout.setVisibility(View.VISIBLE);
                    } else {
                        cameraView.setVisibility(View.VISIBLE);
                        mapLayout.setVisibility(View.INVISIBLE);
                    }

                    List<Marker> markerList = dataManager.getMarkers();

                    for(Marker marker : markerList){
                        marker.setVisible(false);
                    }

                    break;

                case R.id.button_equipment:

                    EquipmentFragment dialog_equipment = new EquipmentFragment();

                    dialog_equipment.show(manager, "EQUIPMENT_DIALOG");

                    break;
            }
        }
    };

    //------------------------------------------------------------ Marker Setting ------------------------------------------------------------//
    // TODO:Pre Set the Dangerous Zones

    double[][] dangerLocations = {
            {41.885887, -87.644321},
            {41.878282, -87.641145},
    };
    String[][] dangerMessage = {
            {"Toxic", "87%"},
            {"Flammable", "97%"},
    };
    String[] dangerValue = {
            "computer",
            "no person"
    };

    private void setPredefinedDangerZones(){

        // Format: (LatLng, Title, Subtitle)
//        DangerZone zone = new DangerZone(new LatLng(41.881555, -87.642028), "Your Location", "Get the hell out of here!");
        int index = 0;
        for(double[] danger : dangerLocations){
            DangerZone zone = new DangerZone(new LatLng(danger[0], danger[1]), dangerMessage[index][0], dangerMessage[index][1]);

            dataManager.addDangerZone(zone);
            index++;
        }
    }

    private void setPredefinedHistory(){

        double[] levels = {0, 44, 97, 12.3, 87};

        for(int i = 0; i < levels.length; i++){
            History history = new History(i, levels[i], "Toxic");
            //Log.d(TAG, "Adding history " + levels[i]);

            dataManager.addHistory(history);
        }

        //Log.d(TAG, "Size: " + dataManager.getHistories().size());
    }

    private void setPredefinedDangerValue(){
        for(String v : dangerValue){
            dataManager.addDangerValue(v);
        }
    }

    private void setPredefinedEquipmentValue() {

        float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

        int initial = 99;

        for(int i = 0; i < maxNumberOfLines; ++i){
            for(int j = 0; j < numberOfPoints; ++j){
                randomNumbersTab[i][j] = initial - (float) Math.random() * 10f * j;
            }
        }

        dataManager.setTable(randomNumbersTab);
    }


    //------------------------------------------------------------ Map Setting ------------------------------------------------------------//
    private void getDeviceLocation() {
        mLocationProvider = LocationServices.getFusedLocationProviderClient(this);

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

                            //TODO: Comment for Emulator
                            moveCamera(myLocation, DEFAULT_ZOOM);

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

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Map is Ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {

            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_custom));

            setDangerMarkers();
        }
    }

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(HomeActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    // Set markers
    private void setDangerMarkers(){

        List<MarkerOptions> markerOptions = dataManager.getMarkerOptions();

        for(MarkerOptions option : markerOptions){
            dataManager.addMarkers(mMap.addMarker(option));
        }
    }

    public void setMarkerFromImageResult(DangerZone zone){
        dataManager.addDangerZone(zone);

        MarkerOptions options = dataManager.getMarkerOption(zone);

        if(options != null){
            dataManager.addMarkers(mMap.addMarker(options));
        }
    }

    // NO NEED TO FIX BELOW THIS POINT!!
    //------------------------------------------------------------ Camera Setting ------------------------------------------------------------//
    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
             transformImage(textureView.getWidth(), textureView.getHeight());
             openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private void takePicture() {
        if(cameraDevice == null)
            return;

        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;

            if(characteristics != null){
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

                //Capture image with custom size
                int width = 640;
                int height = 480;
                if(jpegSizes != null && jpegSizes.length > 0)
                {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }

                final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                List<Surface> outputSurface = new ArrayList<>(2);
                outputSurface.add(reader.getSurface());
                outputSurface.add(new Surface(textureView.getSurfaceTexture()));

                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                //Check orientation base on device
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));

                file = new File(Environment.getExternalStorageDirectory()+"/"+ UUID.randomUUID().toString()+".jpg");
                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = null;
                        try {
                            image = reader.acquireLatestImage();

                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);

                            // Send to Clarifai
                            imageRecognition(bytes);

                            save(bytes);
                        }
                        catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                        finally {
                            if(image != null)
                                image.close();
                        }
                    }


                    private void save(byte[] bytes) throws IOException{
                        OutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            outputStream.write(bytes);
                        } finally {
                            if(outputStream != null)
                                outputStream.close();
                        }
                    }

                    // TODO: Clarifai API
                    private void imageRecognition(final byte[] imageBytes) throws IOException{
                        FragmentManager manager = getSupportFragmentManager();
                        ImageResultFragment dialog = ImageResultFragment.newInstance(imageBytes);

                        dialog.show(manager, "Image_Recognize");
                    }
                };

                reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
                final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        //Toast.makeText(HomeActivity.this, "Saved " + file, Toast.LENGTH_LONG).show();

                        createCameraPreview();
                    }
                };

                cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        try{
                            cameraCaptureSession.capture(captureBuilder.build(), captureCallback, mBackgroundHandler);
                        } catch (CameraAccessException e){
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    }
                }, mBackgroundHandler);

            }
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void createCameraPreview(){
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();

            assert texture != null;

            texture.setDefaultBufferSize(ImageDimension.getWidth(), ImageDimension.getHeight());

            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if(cameraDevice == null)
                        return;

                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(HomeActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        try{
            cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            ImageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // check realtime permission if run higher API 23
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_CAMERA_PERMISSION);

                return;
            }
            manager.openCamera(cameraID, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Can't use camera without permission", Toast.LENGTH_SHORT).show();

            }
        }
        else if(requestCode == LOCATION_PERMISSION_REQUEST)
        {
            if(grantResults.length > 0)
            {
                for(int i = 0; i < grantResults.length; i++)
                {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mLocationPermissionsGranted = false;
                        return;
                    }
                }

                mLocationPermissionsGranted = true;
                // initialize map
                initMap();
            }
        }
    }

    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable()) {
            transformImage(textureView.getWidth(), textureView.getHeight());
            openCamera();
        }
        else
            textureView.setSurfaceTextureListener(textureListener);
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void transformImage(int width, int height){
        if(textureView == null) return;

        Matrix matrix = new Matrix();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, textureView.getHeight(), textureView.getWidth());
        float centerX = textureRectF.centerX();
        float centerY = textureRectF.centerY();
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / width, (float) height / width);
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private long lastTimeBackPressed;

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - lastTimeBackPressed < 1500)
        {
            finish();
            return;
        }

        Toast.makeText(this, "Press again to end the MSMS", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    }
}
