package com.developer.ankit.teachsmile.app.CameraScreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.developer.ankit.teachsmile.R;
import com.developer.ankit.teachsmile.app.Settings.SettingsActivity;
import com.developer.ankit.teachsmile.app.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ankit on 4/29/17.
 */

public class CameraScreenActivity extends Activity implements CameraScreenInterface.View,
                    ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String USER_NAME = "user_name";
    private static final String USER_LOCATION = "user_location";
    @BindView(R.id.camera_view)
    TextureView cameraView;
    @BindView(R.id.take_photo)
    FloatingActionButton takePhotoButton;
    @BindView(R.id.photo_gallery)
    ImageButton openPhotoGalleryButton;
    @BindView(R.id.open_profile)
    ImageButton openProfileButton;
    @BindView(R.id.settings)
    ImageButton settingsButton;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.side_navigation)
    ListView sideNavigation;

    private final String EMOTION_PREF_KEY = "emotion_selection";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private SurfaceTexture surfaceTexture;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Semaphore cameraOpenCloseLock;
    private ImageReader imageReader;
    private File cameraFile;
    private CameraDevice camera;
    private String emotionPref;

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            camera = cameraDevice;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            camera = null;
            finish();
        }
    };

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraScreenPresenter presenter;

    public static Intent startCameraScreen(Context context, String userName, String userLocation) {
        Intent intent = new Intent(context, CameraScreenActivity.class);
        if (userName == null) {
            userName = "Not Logged In";
        }

        if (userLocation == null) {
            userLocation = "Location Not Determined";
        }

        intent.putExtra(CameraScreenActivity.USER_NAME, userName);
        intent.putExtra(CameraScreenActivity.USER_LOCATION, userLocation);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String name = intent.getStringExtra(USER_NAME);
        String location = intent.getStringExtra(USER_LOCATION);
        presenter = new CameraScreenPresenter();
        presenter.setView(this);
        cameraOpenCloseLock = new Semaphore(1);
        sideNavigation.setAdapter(new ArrayAdapter<String>(this, R.layout.side_navigation_layout,
                R.id.side_navigation_element, new String[]{name, location}));
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraFile = new File(this.getExternalFilesDir(null), Utils.getFileName());
    }

    @Override
    protected void onResume() {
        updateEmotion();
        startBackgroundThread();
        super.onResume();
    }

    private void updateEmotion() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        emotionPref = pref.getString(EMOTION_PREF_KEY, "");
    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.startCamera();
                } else {
                    Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT)
                            .show();
                    this.onDestroy();
                }
                return;
            }
        }
    }

    @Override
    public void startCamera() {
        stateCallbackSetup();
        setupTextureListener();
        fetchCameraData();
    }

    private void stateCallbackSetup() {

    }

    private void fetchCameraData() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics cc = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap configMap = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] jpegSize = configMap.getOutputSizes(ImageFormat.JPEG);
            //Log.d("TAG", Utils.getFileName());
        } catch (CameraAccessException e) {

        }



    }

    private void setupTextureListener() {
        cameraView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                surfaceTexture = surface;
                openCamera(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void openCamera(int width, int height) {
        setupCameraOutput(width, height);
    }

    private void setupCameraOutput(int width, int height) {
        CameraManager manager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
        String frontCameraID;
        CameraCharacteristics cameraCharacteristics = null;
        boolean frontFacing = false;
        try {
            for (String cameraID : manager.getCameraIdList()) {
                CameraCharacteristics cc = manager.getCameraCharacteristics(cameraID);
                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraCharacteristics = cc;
                    frontCameraID = cameraID;
                    frontFacing = true;
                }
            }

            // We only use the front camera for this app. If no front camera, show message to the
            // user and exit the app
            if (!frontFacing) {
                Toast.makeText(this, getString(R.string.front_facing_required), Toast.LENGTH_LONG)
                        .show();
                this.finish();
            }

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.
                    SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());
            imageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                    ImageFormat.JPEG, 1);



        } catch (CameraAccessException|NullPointerException e) {
            e.printStackTrace();
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    @OnClick(R.id.settings)
    public void settingsButtonClicked() {
        startActivity(SettingsActivity.startSettingsActivity(this));
    }

    @OnClick(R.id.open_profile)
    public void profileButtonClicked() {
        drawerLayout.openDrawer(Gravity.START);
    }
}
