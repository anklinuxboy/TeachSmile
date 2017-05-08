package com.developer.ankit.teachsmile.app.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.developer.ankit.teachsmile.R;
import com.developer.ankit.teachsmile.app.CameraScreen.CameraScreenActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements LoginInterface.View {

    @BindView(R.id.login_skip)
    TextView skipTextView;

    private CallbackManager callbackManager;
    private LoginInterface.Presenter presenter;
    private boolean isUserLoggedIn = false;
    private String LOGIN_PREF_KEY = "loggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        presenter = new LoginPresenter();
        presenter.setView(this);
        checkIfUserLoggedIn();
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                isUserLoggedIn = true;
                saveLoginState(isUserLoggedIn);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "Login Error");
            }
        });
    }

    private void checkIfUserLoggedIn() {
        isUserLoggedIn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(LOGIN_PREF_KEY, false);

        if (isUserLoggedIn) {
            skipLogin();
        }
    }

    private void saveLoginState(boolean loggedIn) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.edit().putBoolean(LOGIN_PREF_KEY, loggedIn).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.login_skip)
    public void loginSkipClicked() {
        presenter.skipLoginClicked();
    }

    @Override
    public void skipLogin() {
        startActivity(CameraScreenActivity.startCameraScreen(this));
    }
}
