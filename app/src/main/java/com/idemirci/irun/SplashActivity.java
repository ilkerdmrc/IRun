package com.idemirci.irun;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG ="SplashLog" ;
    private GoogleApiClient googleApiClient;

    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;
    CountDownTimer StartJapa_Timer;
    TextView count;
    long millisUntilFinished;
    long counter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        count=(TextView)findViewById(R.id.txtCountdown);
        getSupportActionBar().hide();

        checkGpsState();
    }

    public void initiateCountDown(){
        StartJapa_Timer = new CountDownTimer(4000, 1000) {
            @Override
            public void onFinish() {
                startActivity(new Intent(getApplicationContext(),ActionActivity.class));
                finish();


            }
            @Override
            public void onTick(long millisUntilFinished) {

                count.setText("" + (millisUntilFinished/1000));

            }
        };
        StartJapa_Timer.start();
    }

    private void checkGpsState(){

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(SplashActivity.this).addApi(LocationServices.API).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
// **************************
            builder.setAlwaysShow(true); // this is the key ingredient
// **************************
            PendingResult result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback()
            {
                @Override
                public void onResult(Result result)
                {
                    final Status status = result.getStatus();
                    Log.i(TAG, "CODE : " + status.getStatusCode());
                    final LocationSettingsStates state = ((LocationSettingsResult) result).getLocationSettingsStates();
                    switch (status.getStatusCode())
                    {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied.
                            // You can initialize location requests here.
                            int permissionLocation = ContextCompat
                                    .checkSelfPermission(SplashActivity.this,
                                            android.Manifest.permission.ACCESS_FINE_LOCATION);
                            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                initiateCountDown();
                            }
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied.
                            // But could be fixed by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                // Ask to turn on GPS automatically
                                status.startResolutionForResult(SplashActivity.this,
                                        REQUEST_CHECK_SETTINGS_GPS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied.
                            // However, we have no way
                            // to fix the
                            // settings so we won't show the dialog.
                            // finish();
                            break;
                        /*case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(getActivity(), 1000);
                                //startLocationService();
                            } catch (IntentSender.SendIntentException e)
                            {}
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;*/
                    }
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "ActivityResult Triggered*********");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        initiateCountDown();
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                        break;
                }
                break;
        }
    }
}
