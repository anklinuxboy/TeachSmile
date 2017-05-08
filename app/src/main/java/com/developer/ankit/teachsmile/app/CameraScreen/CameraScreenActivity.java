package com.developer.ankit.teachsmile.app.CameraScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.developer.ankit.teachsmile.R;

import butterknife.ButterKnife;

/**
 * Created by ankit on 4/29/17.
 */

public class CameraScreenActivity extends AppCompatActivity implements CameraScreenInterface.View {

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
