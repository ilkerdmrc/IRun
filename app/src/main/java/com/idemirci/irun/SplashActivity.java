package com.idemirci.irun;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

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
}
