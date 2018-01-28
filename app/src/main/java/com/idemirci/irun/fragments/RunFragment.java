package com.idemirci.irun.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.idemirci.irun.ActionActivity;
import com.idemirci.irun.MainActivity;
import com.idemirci.irun.R;
import com.idemirci.irun.SplashActivity;
import com.idemirci.irun.services.MyLocationService;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
//import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;


public class RunFragment extends Fragment implements OnMapReadyCallback ,GoogleApiClient.ConnectionCallbacks{

    private static final String TAG = "MyLog";
    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;

    private boolean registered = true;

    private BroadcastReceiver broadcastReceiver;
    private Location lastLocation;
    Marker mPositionMarker;

    SharedPreferences sp;
    SharedPreferences.Editor spEdit;

    private GoogleApiClient googleApiClient;

    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;

    public RunFragment(){

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        startLocationService();

        checkPermissions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationService();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "OnPause Inıtiated");
        /*if(broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);

            Log.i(TAG, "If'in icinde");
        }
        */

        if(registered){
            getActivity().unregisterReceiver(broadcastReceiver);
            registered = false;
        }
    }

    private void startPolyline(GoogleMap map, LatLng location){
        if(map == null){
            Log.d("TAG", "Map object is not null");
            return;
        }
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.add(location);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void markStartingLocationOnMap(GoogleMap mapObject, LatLng location){
        mapObject.addMarker(new MarkerOptions().position(location).title("Current location"));
        mapObject.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

    /* Request updates at startup */
    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG,"OnResume Initiated");

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                    lastLocation = (Location) intent.getExtras().get("coordinates");

                    refreshMap();
                    markStartingLocationOnMap(mGoogleMap, new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
                    startPolyline(mGoogleMap, new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));

                }
            };
        }
        getActivity().registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    private void refreshMap() {
        mGoogleMap.clear();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.run_fragment_title);

        startLocationService();

        Button btnStart = (Button) getActivity().findViewById(R.id.btnStart);

        AssetManager am = getContext().getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am,
                String.format("fonts/%s", "italic.ttf"));

        btnStart.setTypeface(typeface);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        spEdit = sp.edit();


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SplashActivity.class));
            }
        });

        mMapView = (MapView) mView.findViewById(R.id.mapView);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_run, container, false);
        return mView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap = googleMap;
        boolean isMapChanged = sp.getBoolean("isMapChanged", false);

        if(isMapChanged){
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.light);
            mGoogleMap.setMapStyle(style);
        }else{
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.night);
            mGoogleMap.setMapStyle(style);
        }


        com.luckycatlabs.sunrisesunset.dto.Location location = new com.luckycatlabs.sunrisesunset.dto.Location(40.973248, 28.722694);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, "Europe/Istanbul");

        String officialSunrise = calculator.getOfficialSunriseForDate(Calendar.getInstance());
        Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
        String sunset = officialSunset.getTime().toString();
        String [] sunsetFetch =  sunset.split(" ");
        String sunsetWithSeconds = sunsetFetch[3];
        String [] sunsetWithSecondsArray = sunsetWithSeconds.split(":");
        String sunsetFinal = sunsetWithSecondsArray[0] + ":" + sunsetWithSecondsArray[1];

        Log.i("x", "Sunrise : " + officialSunrise);
        Log.i("x", "Sunset : " + sunsetFinal);

    }

    public void animateMarker(final Marker marker, final Location location) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final LatLng startLatLng = marker.getPosition();
        final double startRotation = marker.getRotation();
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                double lng = t * location.getLongitude() + (1 - t)
                        * startLatLng.longitude;
                double lat = t * location.getLatitude() + (1 - t)
                        * startLatLng.latitude;

                float rotation = (float) (t * location.getBearing() + (1 - t)
                        * startRotation);

                marker.setPosition(new LatLng(lat, lng));
                marker.setRotation(rotation);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void startLocationService(){
        Log.i(TAG,"startLocationService Initiated");
        Intent i = new Intent(getActivity().getApplicationContext(),MyLocationService.class);
        getActivity().startService(i);
    }

    private void stopLocationService(){
        Log.i(TAG,"stopLocationService Initiated");
        Intent i =new Intent(getActivity().getApplicationContext(),MyLocationService.class);
        getActivity().stopService(i);
    }


    private void checkGpsState(){

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext()).addApi(LocationServices.API).build();
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
                                    .checkSelfPermission(getActivity(),
                                            Manifest.permission.ACCESS_FINE_LOCATION);
                            if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                startLocationService();
                            }
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied.
                            // But could be fixed by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                // Ask to turn on GPS automatically
                                status.startResolutionForResult(getActivity(),
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "ActivityResult Triggered*********");
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationService();
                        break;
                    case Activity.RESULT_CANCELED:
                        getActivity().finish();
                        break;
                }
                break;
        }
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(getActivity(),
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }else{
            checkGpsState();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}





















