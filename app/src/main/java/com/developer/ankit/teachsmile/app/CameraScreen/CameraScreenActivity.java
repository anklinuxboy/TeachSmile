package com.developer.ankit.teachsmile.app.CameraScreen;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.developer.ankit.teachsmile.app.data.DatabaseContract;
import com.developer.ankit.teachsmile.app.images.ImageViewer;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.graphics.Color.GREEN;
import static android.graphics.Color.WHITE;

public class CameraScreenActivity extends Activity implements CameraScreenInterface.View,
                    ActivityCompat.OnRequestPermissionsResultCallback, Detector.FaceListener,
                    Detector.ImageListener, CameraDetector.CameraEventListener {

    private static final String USER_NAME = "user_name";
    private static final String USER_LOCATION = "user_location";
    public static final String JOY = "Joy";
    public static final String SURPRISE = "Surprise";
    public static final String ANGER = "Anger";
    private static final float MAX_EMOTION_VALUE = 20.0f;
    private static final float MAX_JOY_VALUE = 80.0f;
    private FirebaseAnalytics mFirebaseAnalytics;

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
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Intent intent = getIntent();
        String name = intent.getStringExtra(USER_NAME);
        String location = intent.getStringExtra(USER_LOCATION);
        presenter = new CameraScreenPresenter();
        presenter.setView(this);
        sideNavigation.setAdapter(new ArrayAdapter<>(this, R.layout.side_navigation_layout,
                R.id.side_navigation_element, new String[]{name, location}));
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

    @OnClick(R.id.photo_gallery)
    public void photoGalleryButtonClicked() {
        startActivity(ImageViewer.startImageViewerActivity(this));
    }

    @OnClick(R.id.take_photo)
    public void takePhotoClicked() {

        if (recentFrame == null) {
            showToast(getString(R.string.frame_undetected));
            return;
        }

        Bitmap faceBitmap = ImageSaver.getBitmapFromFrame(recentFrame);

        if (faceBitmap == null) {
            Timber.e( "Unable to generate bitmap for frame, aborting screenshot");
            return;
        }

        Bitmap finalScreenshot = Bitmap.createBitmap(faceBitmap.getWidth(), faceBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalScreenshot);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(faceBitmap, 0, 0,paint);

        cameraFile = Utils.getFile();
        Timber.d("Got new file " + cameraFile.getAbsolutePath());
        try {
            ImageSaver.saveBitmapToFile(finalScreenshot, cameraFile);
        } catch (IOException e) {
            Timber.e("Cannot save screenshot");
            return;
        }

        String[] imageNameTokens = cameraFile.getAbsolutePath().split("/");

        Timber.d("after");
        new SaveValuesToDB().execute(imageNameTokens[imageNameTokens.length-1], emotionPref, cameraFile.getAbsolutePath());
        Timber.d("afte");
        faceBitmap.recycle();
        finalScreenshot.recycle();
        showToast(cameraFile.toString() + " " + getString(R.string.image_saved));

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, Integer.toString(takePhotoButton.getId()));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, takePhotoButton.toString());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onCameraSizeSelected(int i, int i1, Frame.ROTATE rotate) {}

    @Override
    public void onFaceDetectionStarted() {}

    @Override
    public void onFaceDetectionStopped() {}

    @Override
    public void onImageResults(List<Face> faces, Frame frame, float v) {

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
                recentFrame = frame;
                takePhotoButton.setClickable(true);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            } else if (emotionPref.equals(ANGER) && anger > MAX_EMOTION_VALUE) {
                recentFrame = frame;
                takePhotoButton.setClickable(true);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            } else if (emotionPref.equals(SURPRISE) && surprise > MAX_EMOTION_VALUE) {
                recentFrame = frame;
                takePhotoButton.setClickable(true);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(GREEN));
            } else {
                takePhotoButton.setClickable(false);
                takePhotoButton.setBackgroundTintList(ColorStateList.valueOf(WHITE));
            }
        }
    }

    private class SaveValuesToDB extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            Timber.d("in do background");
            String imageName = params[0];
            String emotion = params[1];
            String imagePath = params[2];

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_IMAGE_NAME, imageName);
            values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_EMOTION, emotion);
            values.put(DatabaseContract.DatabaseEntry.COLUMN_NAME_IMAGE_PATH, imagePath);

            getContentResolver().insert(DatabaseContract.CONTENT_URI, values);
            Timber.d(getString(R.string.values_saved));
            return null;
        }
    }
}
