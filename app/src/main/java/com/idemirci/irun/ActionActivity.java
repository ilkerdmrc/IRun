package com.idemirci.irun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.idemirci.irun.database.DBHelper;
import com.idemirci.irun.services.MyLocationService;
import com.idemirci.irun.util.CalorieHelper;
import com.idemirci.irun.util.PhoneStateReceiver;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ActionActivity extends AppCompatActivity {

    public static final String TAG = "MyLog"; // Used for Log Tag
    private TextView action_txt, totalDistance_txt, kilometer_txt;
    private Chronometer mChronometer;
    private long mLastStopTime = 0;
    private Button btnStart, btnPause, btnRestart, btnStop = null;
    private Animation pulse;
    private int informationResult;
    private float currentTotalDistance;
    private ConstraintLayout action_layout;

    // Settings
    boolean isBCActive, phoneState;
    private final PhoneStateReceiver mybroadcast = new PhoneStateReceiver(); // Telefonun Çağrı Durumunu Kontrol Eden Class'ın Instance'ı
    SharedPreferences sp;
    SharedPreferences.Editor edit;

    private BroadcastReceiver broadcastReceiver;
    private DBHelper dbHelper;
    private String runId;

    private float totalTime;
    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;

    static Random rnd = new Random();
    Location prevLocation;

    private boolean invokedByPause;


    /**
     * Generates a random id for the specific activity
     * @param len
     * @return
     */
    private String generateRunId(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isBCActive = sp.getBoolean("isPhoneReceived", false);

        if (!isBCActive) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.PHONE_STATE");
            registerReceiver(mybroadcast, filter);
            mybroadcast.setActionActivity(this);
        }

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "onReceive inside...");
                    Location loc = (Location) intent.getExtras().get("coordinates");

                    if (loc != null) {
                        float deltaBpm;
                        //Random rand = new Random();
                        //int speed = rand.nextInt((3 - 2) + 1) + 2;
                        //Log.i(TAG, "random speed" + speed);

                        float speed = loc.getSpeed(); // Koşulan süre / Koşulan km Pace'i verir.
                        float accuracy = loc.getAccuracy();
                        Toast.makeText(ActionActivity.this, "Accuracy : " + accuracy ,Toast.LENGTH_SHORT).show();

                        Log.i(TAG, "Accuracy : " + accuracy);

                        action_txt.setText(String.valueOf(speed));

                        deltaBpm = calculateBpm(speed);

                        Log.i(TAG, "CurrentBpm" + deltaBpm);

                        float deltaDistance = 0;
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                        String dt = sdf.format(new Date());

                        if (invokedByPause) {
                            Log.i(TAG, "invokedByPause true");
                            prevLocation = null;
                            invokedByPause = false;
                        } else {
                            Log.i(TAG, "invokedByPause false");
                        }

                        if (prevLocation != null) {
                            deltaDistance = loc.distanceTo(prevLocation);
                        }

                        prevLocation = loc;
                        Log.i(TAG, "Speed / deltaDistance : " + speed + "  /  " + deltaDistance);

                        String lat = String.valueOf(loc.getLatitude());
                        String lng = String.valueOf(loc.getLongitude());

                        Log.i(TAG, "Lat : " + lat);
                        Log.i(TAG, "Lng : " + lng);

                        if (dbHelper != null && speed > 0 && deltaDistance > 0 && accuracy < 50) {
                            dbHelper.insertRoute(runId, lat, lng, Math.abs(deltaDistance), speed, deltaBpm, dt);
                            Log.i(TAG, "Lokasyon Data kaydedildi...");
                            currentTotalDistance = dbHelper.getCurrentTotalDistance(runId);
                            if (currentTotalDistance < 1000) {
                                totalDistance_txt.setText(String.valueOf((int) currentTotalDistance));
                                kilometer_txt.setText(getResources().getString(R.string.action_activity_meter));
                            } else {
                                totalDistance_txt.setText(String.valueOf(currentTotalDistance / 1000));
                                kilometer_txt.setText(getResources().getString(R.string.action_activity_kilometer));
                            }
                        } else {
                            Log.i(TAG, "No requirement data to save DB");
                        }
                        // checkCurrentRunningState(currentTotalDistance, speed); Need Some Work
                    }
                }

            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    /*
    private void checkCurrentRunningState(float currentTotalDistance, float speed) {
        if (currentTotalDistance > 0 && speed == 0) {
            chronoPause();
        }
    }
    */



    private float calculateBpm(float speed) {
        double result = 0;
        double minSpeed =  0.1;
        double maxSpeed = 0.2;
        double bpmConstant = 1;
        double baseBpm = baseBpmCalc();
        double incrementVal = 0.02;
        double speedIncrementValue = 0.1;
        int MAX_SPEED = 10;

        if(speed > MAX_SPEED){
            Toast.makeText(this, "You must be in a car right now, or maybe you are Usain Bolt.", Toast.LENGTH_SHORT).show();
        }

        for (double i = 0.1; i <= MAX_SPEED; i += 0.1) {
            bpmConstant = bpmConstant + incrementVal;

            if (((speed > minSpeed) && (speed < maxSpeed)) || ((speed == minSpeed) || (speed == maxSpeed))) {
                Log.i(TAG, "If'inn içinde");
                result = baseBpm * bpmConstant;
                break;
            }
            minSpeed = minSpeed + speedIncrementValue;
            maxSpeed = maxSpeed + speedIncrementValue;
        }
        return (float) result;
    }


        @Override
        protected void onCreate (Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_action2);
            getSupportActionBar().hide();


            //checkPermissions();

            sp = PreferenceManager.getDefaultSharedPreferences(ActionActivity.this);
            edit = sp.edit();
            phoneState = sp.getBoolean("isPhoneReceived", false);

            runId = "run_" + generateRunId(3);
            dbHelper = new DBHelper(this);
            mChronometer = (Chronometer) findViewById(R.id.chronometer);
            btnStart = (Button) findViewById(R.id.start_button);
            btnPause = (Button) findViewById(R.id.pause_button);
            btnRestart = (Button) findViewById(R.id.restart_button);
            btnStop = (Button) findViewById(R.id.stop_button);
            action_txt = (TextView) findViewById(R.id.action_txt);
            totalDistance_txt = (TextView) findViewById(R.id.totalDistance_txt);
            kilometer_txt = (TextView) findViewById(R.id.kilometer_txt);
            pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
            action_layout = (ConstraintLayout) findViewById(R.id.action_layout);

            Random rand = new Random();
            int imageId = rand.nextInt((1 - 0) + 1) + 0;


            AssetManager am = getBaseContext().getApplicationContext().getAssets();
            Typeface typeface = Typeface.createFromAsset(am,
                    String.format("fonts/%s", "Sport.ttf"));

            mChronometer.setTypeface(typeface);
            totalDistance_txt.setTypeface(typeface);
            action_txt.setTypeface(typeface);
            kilometer_txt.setTypeface(typeface);
            action_layout.setBackgroundResource(imagesForActivity[imageId]);
            if(imageId == 0){

            }

            chronoStart();
            btnStart.setVisibility(View.GONE);

            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnStart.setEnabled(false);
                    chronoStart();
                    btnPause.setEnabled(true);
                }
            });

            btnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnPause.setEnabled(false);
                    chronoPause();
                    btnStart.setEnabled(true);

                }
            });

            btnRestart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chronoRestart();
                    btnPause.setEnabled(true);
                }
            });

            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActionActivity.this);

                    builder.setMessage("Aktiviteyi sonlandırmak istediğinize emin misiniz?");

                    builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            goResult();
                            ActionActivity.super.onBackPressed();
                        }
                    });
                    builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });

        }

    public void chronoStart() {
        Log.i(TAG, "chronoStart ...");

        startLocationService();
        mChronometer.clearAnimation();
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);

        if (mLastStopTime == 0)
            mChronometer.setBase(SystemClock.elapsedRealtime());
        else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
            mChronometer.setBase(mChronometer.getBase() + intervalOnPause);
        }
        mChronometer.start();
    }


    public void chronoPause() {
        Log.i(TAG, "chronoPause ...");
        btnPause.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        mChronometer.setAnimation(pulse);
        stopLocationService();
        mChronometer.stop();
        invokedByPause = true;
        mLastStopTime = SystemClock.elapsedRealtime();
    }

    public void chronoRestart() {
        Log.i(TAG, "chronoRestart ...");
        btnStart.setEnabled(false);
        stopLocationService();
        mChronometer.stop();
        initializeAction();
        chronoStart();
    }

    private void initializeAction() {
        mLastStopTime = 0;
        totalDistance_txt.setText("0");
        dbHelper.removeRunRecords(runId);
        runId = "run_" + generateRunId(3);
    }

    private void startLocationService() {
        Log.i(TAG, "startLocationService ....");
        Intent iService = new Intent(getApplicationContext(), MyLocationService.class);
        startService(iService);
    }

    private void stopLocationService() {
        Log.i(TAG, "stopLocationService ..........");

        Intent iService = new Intent(ActionActivity.this, MyLocationService.class);
        boolean result = stopService(iService);
        if (result) {
            Log.i(TAG, "stopService ok...");
        } else {
            Log.i(TAG, "stopService Nok!!!!!...");
        }
    }


    private float getChronoTime(String chronoText) {
        int stoppedMilliseconds = 0;

        String array[] = chronoText.split(":");

        if (array.length == 2) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000 + Integer.parseInt(array[1]) * 1000;
        } else if (array.length == 3) {
            stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000 + Integer.parseInt(array[1]) * 60 * 1000
                    + Integer.parseInt(array[2]) * 1000;
        }

        return stoppedMilliseconds; /*TODO summary tablosu oluşturulacak, diğer veriler toptan

                                    */
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActionActivity.this);
        builder.setMessage(R.string.action_activity_cancel);
        builder.setPositiveButton(R.string.action_activity_accept, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        builder.setNegativeButton(R.string.action_activity_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void goResult() {
        stopLocationService();
        mChronometer.stop();
        // TODO : Goto Result page

        totalTime =  getChronoTime((String) mChronometer.getText());
        NumberFormat formatter = new DecimalFormat("#0.00");
        formatter.format(totalTime);
        Intent lastResultActivity = new Intent(ActionActivity.this, LastRunResultActivity.class);
        lastResultActivity.putExtra("runId", runId);
        lastResultActivity.putExtra("totalTime", totalTime);
        startActivity(lastResultActivity);
    }

    public int baseBpmCalc() {
        int age = dbHelper.getAge();
        Log.i(TAG, "Age : " + age);
        int baseBpm = 0;
        if (age >= 20 && age <= 30) {
            baseBpm = 100;
        } else if (age <= 35 && age > 30) {
            baseBpm = 95;
        } else if (age <= 40 && age > 35) {
            baseBpm = 93;
        } else if (age <= 45 && age > 40) {
            baseBpm = 90;
        } else if (age <= 50 && age > 45) {
            baseBpm = 88;
        } else if (age <= 55 && age > 50) {
            baseBpm = 85;
        } else if (age <= 60 && age > 55) {
            baseBpm = 83;
        } else if (age <= 65 && age > 60) {
            baseBpm = 80;
        } else if (age <= 70 && age > 65) {
            baseBpm = 78;
        } else if (age > 70) {
            baseBpm = 75;
        }
        return baseBpm;
    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mybroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(mybroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private void checkPermissions() {
        int permissionLocation = ContextCompat.checkSelfPermission(ActionActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }
    }

    public int [] imagesForActivity = {R.drawable.running_man_image_cropped,
            R.drawable.running_woman_cropped
    };
}
