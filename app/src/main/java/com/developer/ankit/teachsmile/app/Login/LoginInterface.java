package com.developer.ankit.teachsmile.app.Login;

/**
 * Created by ankit on 4/18/17.
 */

public interface LoginInterface {
    interface View {
        void skipLogin();
    }

    interface Presenter {
        void setView(LoginInterface.View view);
        void skipLoginClicked();
    }
}
