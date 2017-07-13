package com.developer.ankit.teachsmile.app.Login;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.ankit.teachsmile.R;
import com.developer.ankit.teachsmile.app.CameraScreen.CameraScreenActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity implements LoginInterface.View,
        ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.login_skip)
    TextView skipTextView;

    private CallbackManager callbackManager;
    private LoginInterface.Presenter presenter;
    private boolean isUserLoggedIn = false;
    private String LOGIN_PREF_KEY = "loggedIn";
    private FusedLocationProviderClient locationProviderClient;
    private final int LOCATION_PERMISSION = 101;
    private String userLocation = null;
    private ProfileTracker profileTracker;
    private String userName = null;

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
                startActivity(CameraScreenActivity.startCameraScreen(getBaseContext(), userName, userLocation));
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "Login Error");
            }
        });

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                userName = currentProfile.getName();
                Timber.d(userName);
            }
        };

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastKnownLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void getLastKnownLocation() {
        if (this.checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        userLocation = getLocationName(location.getLatitude(), location.getLongitude());
                    }
                }
            });
        }
    }

    private String getLocationName(double lat, double lon) {
        String location = null;
        Geocoder geocoder = new Geocoder(this, Locale.US);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            Address addr = addresses.get(0);
            location = addr.getLocality() + " " + addr.getCountryName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return location;
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
    public void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int resultCode, String[] permissions, int[] grantResults) {
        switch (resultCode) {
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, getString(R.string.location_required), Toast.LENGTH_SHORT)
                            .show();
                }
                return;
        }
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
        startActivity(CameraScreenActivity.startCameraScreen(this, userName, userLocation));
    }
}
