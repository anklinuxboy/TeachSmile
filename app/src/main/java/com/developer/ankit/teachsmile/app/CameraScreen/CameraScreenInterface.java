package com.developer.ankit.teachsmile.app.CameraScreen;

/**
 * Created by ankit on 4/29/17.
 */

public interface CameraScreenInterface {
    interface View {
        void askPermission();
    }

    interface Presenter {
        void setView(CameraScreenInterface.View view);
    }
}
