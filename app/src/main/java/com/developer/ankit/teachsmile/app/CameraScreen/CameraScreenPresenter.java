package com.developer.ankit.teachsmile.app.CameraScreen;

/**
 * Created by ankit on 4/29/17.
 */

public class CameraScreenPresenter implements CameraScreenInterface.Presenter {

    private CameraScreenInterface.View view;

    @Override
    public void setView(CameraScreenInterface.View view) {
        this.view = view;
        this.view.askPermission();
    }
}
