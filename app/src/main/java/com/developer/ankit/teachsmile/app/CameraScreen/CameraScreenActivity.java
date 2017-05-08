package com.developer.ankit.teachsmile.app.CameraScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;

import com.developer.ankit.teachsmile.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ankit on 4/29/17.
 */

public class CameraScreenActivity extends AppCompatActivity implements CameraScreenInterface.View {

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

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraScreenPresenter presenter;

    public static Intent startCameraScreen(Context context) {
        Intent intent = new Intent(context, CameraScreenActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_screen);
        ButterKnife.bind(this);
        presenter = new CameraScreenPresenter();
        presenter.setView(this);
    }
}
