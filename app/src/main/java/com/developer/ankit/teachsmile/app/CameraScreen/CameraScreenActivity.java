package com.developer.ankit.teachsmile.app.CameraScreen;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.developer.ankit.teachsmile.R;
import com.developer.ankit.teachsmile.app.Settings.SettingsActivity;
import com.developer.ankit.teachsmile.app.Utils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class CameraScreenActivity extends Activity implements CameraScreenInterface.View,
                    ActivityCompat.OnRequestPermissionsResultCallback, Detector.FaceListener,
                                    Detector.ImageListener, CameraDetector.CameraEventListener,
        CaptureView.CaptureThreadListener {

    private static final String USER_NAME = "user_name";
    private static final String USER_LOCATION = "user_location";
    private static final String JOY = "Joy";
    private static final String SURPRISE = "Surprise";
    private static final String ANGER = "Anger";
    private static final float MAX_EMOTION_VALUE = 20.0f;
    private static final float MAX_JOY_VALUE = 80.0f;

    @BindView(R.id.camera_view)
    SurfaceView cameraView;
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
    @BindView(R.id.capture_view)
    CaptureView captureView;

    private final String EMOTION_PREF_KEY = "emotion_selection";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private File cameraFile;
    private String emotionPref;

    /* Affectiva SDK variables */
    private CameraDetector.CameraType cameraType = CameraDetector.CameraType.CAMERA_FRONT;
    private CameraDetector detector = null;
    private Frame recentFrame;

    private void showToast(final String text) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
                }
            });
    }

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
        sideNavigation.setAdapter(new ArrayAdapter<>(this, R.layout.side_navigation_layout,
                R.id.side_navigation_element, new String[]{name, location}));
        captureView.setEventListener(this);
        initializeCameraDetector();
    }

    @Override
    protected void onResume() {
        updateEmotion();
        updateDetection();
        super.onResume();
    }

    private void updateDetection() {
        detector.setDetectAllEmotions(false);
        switch (emotionPref) {
            case JOY:
                Timber.d("Detect Joy");
                detector.setDetectJoy(true);
                detector.setDetectSurprise(false);
                detector.setDetectAnger(false);
                break;
            case SURPRISE:
                Timber.d("Detect Surprise");
                detector.setDetectJoy(false);
                detector.setDetectSurprise(true);
                detector.setDetectAnger(false);
                break;
            case ANGER:
                Timber.d("Detect Sadness");
                detector.setDetectJoy(false);
                detector.setDetectSurprise(false);
                detector.setDetectAnger(true);
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            cameraView.post(new Runnable() {
                @Override
                public void run() {
                    startDetector();
                }
            });
        }
    }

    private void startDetector() {
        detector.setDetectValence(true);
        if (!detector.isRunning()) {
            try {
                Timber.d("Detector start");
                detector.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeCameraDetector() {
        detector = new CameraDetector(this, cameraType, cameraView, 1, Detector.FaceDetectorMode.LARGE_FACES);
        detector.setImageListener(this);
        detector.setFaceListener(this);
        detector.setOnCameraEventListener(this);
    }

    private void updateEmotion() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        emotionPref = pref.getString(EMOTION_PREF_KEY, "");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void askPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //presenter.startCamera();
                } else {
                    Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT)
                            .show();
                    this.finish();
                }
                return;
            }
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

    @OnClick(R.id.take_photo)
    public void takePhotoClicked() {
        Rect frame = cameraView.getHolder().getSurfaceFrame();

        cameraFile = Utils.getFile();
        Timber.d("Got new file " + cameraFile.getAbsolutePath());
    }

    @Override
    public void onCameraSizeSelected(int i, int i1, Frame.ROTATE rotate) {

    }

    @Override
    public void onFaceDetectionStarted() {

    }

    @Override
    public void onFaceDetectionStopped() {

    }

    @Override
    public void onImageResults(List<Face> faces, Frame frame, float v) {
        recentFrame = frame;

        if (faces == null) {
            return;
        }

        if (faces.size() == 1) {
            Face face = faces.get(0);
            float joy = face.emotions.getJoy();
            float anger = face.emotions.getAnger();
            float surprise = face.emotions.getSurprise();

            Timber.d("Joy %s Anger %s Surprise %s", joy, anger, surprise);

            if (emotionPref.equals(JOY) && joy > MAX_JOY_VALUE) {
                takePhotoButton.setClickable(true);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            } else if (emotionPref.equals(ANGER) && anger > MAX_EMOTION_VALUE) {
                takePhotoButton.setClickable(true);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            } else if (emotionPref.equals(SURPRISE) && surprise > MAX_EMOTION_VALUE) {
                takePhotoButton.setClickable(true);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            } else {
                takePhotoButton.setClickable(false);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(WHITE));
            }
        }
    }

    @Override
    public void onBitmapGenerated(@NonNull final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processScreenshot(bitmap);
            }
        });
    }

    private void processScreenshot(Bitmap bitmap) {

    }
}
