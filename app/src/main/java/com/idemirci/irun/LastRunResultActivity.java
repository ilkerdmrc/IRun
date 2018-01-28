package com.idemirci.irun;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.idemirci.irun.adapters.CustomAndroidGridViewAdapter;
import com.idemirci.irun.adapters.RouteListAdapter;
import com.idemirci.irun.database.DBHelper;
import com.idemirci.irun.fragments.LastRunMapFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class LastRunResultActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = "DB_ERR_LOG";
    private DBHelper dbHelper;
    private RouteListAdapter empListAdapter;
    private float avgBpm, totalBurnedCal;
    private ArrayList<LatLng> allLatsLng;

    GoogleMap mMap;
    MapView mMapView;
    Fragment fragment = null;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayoutAndroid;
    CoordinatorLayout rootLayoutAndroid;
    Button last_run_fragment_btn;
    Fragment denemeFragment = new LastRunMapFragment();
    String dataStr = "";
    String runId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_run_2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mMapView = (MapView) findViewById(R.id.mapView);
        if(mMapView != null){
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        initInstances();

        TextView total_distance_txt = (TextView) findViewById(R.id.lastRunTxt1);
        TextView result_time_txt = (TextView) findViewById(R.id.lastRunTxt2);
        TextView result_cal_txt = (TextView) findViewById(R.id.lastRunTxt3);
        TextView result_date_txt = (TextView) findViewById(R.id.lastRunTxt4);
        TextView result_speed_txt = (TextView) findViewById(R.id.lastRunTxt5);
        last_run_fragment_btn = (Button) findViewById(R.id.last_run_fragment_btn);


        /*
        TextView result_speed_txt = (TextView) findViewById(R.id.result_speed_txt);
        TextView result_time_txt = (TextView) findViewById(R.id.result_time_txt);
        TextView result_cal_txt = (TextView) findViewById(R.id.result_cal_txt);
        TextView result_date_txt = (TextView) findViewById(R.id.result_date_txt);
        TextView latlntText = (TextView) findViewById(R.id.latlngText);
        TextView result_avgBpm_txt = (TextView) findViewById(R.id.result_avgBpm_txt);*/

        dbHelper = new DBHelper(this);

        runId = getIntent().getExtras().getString("runId");
        float totalTime = getIntent().getExtras().getFloat("totalTime");
        float totalTimeFetch = totalTime / 60000;
        Cursor cursor = null;

        avgBpm = dbHelper.getAvgBpm();
        totalBurnedCal = calculateCal(avgBpm,totalTimeFetch);

        allLatsLng = new ArrayList<LatLng>();
        Cursor allLatLngCursor = dbHelper.getAllLatLng(runId);

        if(allLatLngCursor != null){
            allLatLngCursor.moveToFirst();
            while(allLatLngCursor.moveToNext()){
                String lat = allLatLngCursor.getString(allLatLngCursor.getColumnIndex("lat"));
                String lng = allLatLngCursor.getString(allLatLngCursor.getColumnIndex("lng"));
                System.out.println("Lat / Lng : " + lat + " / " + lng);
                LatLng cors = new LatLng(Double.valueOf(lat),(Double.valueOf(lng)));
                allLatsLng.add(cors);
            }
        }

        Log.i(TAG, "**** Coordinats : " + allLatsLng.toString());

        try{
            cursor = dbHelper.getRouteSummaryByRunId(runId);

            if(cursor != null && cursor.moveToFirst()){
                // Feed summary
                Log.i("Feed summary  : ", "Feed summary inside...");
                float totalDistance = cursor.getFloat(cursor.getColumnIndex("deltaDistance"));
                float avgSpeed = cursor.getFloat(cursor.getColumnIndex("speed"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                String dt = sdf.format(new Date());

                dbHelper.insertRouteSummary(runId, avgSpeed, totalDistance, totalTimeFetch, totalBurnedCal, dt);
                Log.i("insertRouteSummary : ", "insertRouteSummary ok...");

                //--- Set Display Values
                String formattedTotalDistance = String.format("%.2f", totalDistance);
                String formattedAvgSpeed = String.format("%.2f", avgSpeed);
                total_distance_txt.setText(formattedTotalDistance + " meter");
                result_speed_txt.setText(formattedAvgSpeed + " m/s");
                result_time_txt.setText(new DecimalFormat("##.##").format(totalTimeFetch) + " min");
                result_date_txt.setText(dt);
                if(totalBurnedCal < 0){
                    Toast.makeText(LastRunResultActivity.this, "There was a problem while calculating the total calorie you've just burned", Toast.LENGTH_LONG).show();
                    result_cal_txt.setText("ERROR");
                }else {
                    result_cal_txt.setText(new DecimalFormat("##.##").format(totalBurnedCal)+ " cal");
                }
            }
        } catch (final Exception e) {
            Log.e(TAG, "getRouteSummaryByRunId DB ERROR for runId : " + runId + " : "+ e.toString());
        } finally {
            cursor.close();
            dbHelper.close();
        }

        last_run_fragment_btn.setOnClickListener(new View.OnClickListener() {         // Buttona tıklandığında ActionActivity'den aldığın runid'yi fragmenta yolluyor. Aynı zamanda Db'ye ArrayListe çevrilmiş
                                                                                      // Daha sonrasında gson objesi olarak tek bir Stringe indirgenmiş halini runId ile birlikte kaydediyor.
            @Override
            public void onClick(View view) {

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.android_coordinator_layout, denemeFragment);
                Bundle bundleForRunIdPass = new Bundle();
                bundleForRunIdPass.putString("runIdForThisRun", runId);
                denemeFragment.setArguments(bundleForRunIdPass);
                ft.addToBackStack("Tag").commit();
                dbHelper.insertLatLngData(runId,dataStr);
            }
        });
    }

    private float calculateCal(float avgBpm, float totalTimeFetch) {
        float totalCal = 0;
        String sex = dbHelper.getSex();
        int age = dbHelper.getAge();
        int weight = dbHelper.getWeight();
        if(sex.equals("Male")){
            totalCal = (float) (((age * 0.2017) - (weight * 0.09036) + (avgBpm * 0.6309) - 55.0969)*(totalTimeFetch / 4.184));
        } else if(sex.equals("Female")){
            totalCal = (float) (((age * 0.074) - (weight * 0.05741) + (avgBpm * 0.4472) - 20.4022)*(totalTimeFetch / 4.184));
        }
        return totalCal;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        allLatsLng = new ArrayList<LatLng>();
        String runId2 = getIntent().getExtras().getString("runId");
        Cursor allLatLngCursor = dbHelper.getAllLatLng(runId2);

        mMap = googleMap;
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(getBaseContext(), R.raw.night);
        mMap.setMapStyle(style);

        if(allLatLngCursor != null) {
            allLatLngCursor.moveToFirst();
            while (allLatLngCursor.moveToNext()) {
                String lat = allLatLngCursor.getString(allLatLngCursor.getColumnIndex("lat"));
                String lng = allLatLngCursor.getString(allLatLngCursor.getColumnIndex("lng"));
                LatLng cors = new LatLng(Double.valueOf(lat), (Double.valueOf(lng)));
                allLatsLng.add(cors);
            }
            dataStr = new Gson().toJson(allLatsLng);
            Log.i(TAG, "List : " + dataStr);
        }

        PolylineOptions polylineOptions = new PolylineOptions();
        PolylineOptions polylineOptionsDark = new PolylineOptions();

        polylineOptions.addAll(allLatsLng);
        polylineOptionsDark.addAll(allLatsLng);
        polylineOptions.width(10).color(getResources().getColor(R.color.dandelion));
        polylineOptionsDark.width(20).color(getResources().getColor(R.color.black));

        mMap.addPolyline(polylineOptionsDark);
        mMap.addPolyline(polylineOptions);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int width = (mMapView.getResources().getDisplayMetrics().widthPixels);
        int height = (mMapView.getResources().getDisplayMetrics().heightPixels);
        int padding = (int) (width * 0.15);

        if(allLatsLng.size() != 0){
            for(LatLng pointsForBound : allLatsLng){
                builder.include(pointsForBound);
            }
            LatLngBounds bounds  = builder.build();

            LatLng startPoint = allLatsLng.get(0); // Starting Position
            LatLng finishPoint = allLatsLng.get(allLatsLng.size() - 1);// Ending Position

            mMap.addMarker(new MarkerOptions().position(finishPoint).icon(BitmapDescriptorFactory.defaultMarker()));

            mMap.addMarker(new MarkerOptions().position(startPoint).icon(BitmapDescriptorFactory.defaultMarker(5)));

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height,padding); // newLatLngBounds(LatLngBounds bounds, int width, int height, int padding)
            mMap.moveCamera(cu);
        }else{
            Toast.makeText(LastRunResultActivity.this, "You didn't change your position", Toast.LENGTH_SHORT).show();
        }
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }


    private void initInstances() {
        rootLayoutAndroid = (CoordinatorLayout) findViewById(R.id.android_coordinator_layout);
        collapsingToolbarLayoutAndroid = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_android_layout);
        collapsingToolbarLayoutAndroid.setTitle("Last Run");
    }


    private void drawCustomMarker(LatLng point) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title("START");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.finish2));
        mMap.addMarker(markerOptions);

    }

    @Override
    public boolean onSupportNavigateUp(){
        //code it to launch an intent to the activity you want
        onBackPressed();
        return true;
    }

   /* @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(LastRunResultActivity.this, NavActivity.class));
        finish();
    }
    */
}
