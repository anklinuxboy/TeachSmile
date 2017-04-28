package com.developer.ankit.teachsmile.app.Login;

/**
 * Created by ankit on 4/18/17.
 */

public class LoginPresenter implements LoginInterface.Presenter {

    private LoginInterface.View view;
    @Override
    public void setView(LoginInterface.View view) {
        this.view = view;
    }

    @Override
    public void skipLoginClicked() {
        view.skipLogin();
    }
}
