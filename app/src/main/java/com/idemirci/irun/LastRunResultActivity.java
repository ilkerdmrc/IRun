package com.idemirci.irun;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.idemirci.irun.adapters.RouteListAdapter;
import com.idemirci.irun.database.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LastRunResultActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private RouteListAdapter empListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_run_result_activity);
        getSupportActionBar().hide();

        TextView total_distance_txt = (TextView) findViewById(R.id.total_distance_txt);
        TextView result_speed_txt = (TextView) findViewById(R.id.result_speed_txt);
        TextView result_time_txt = (TextView) findViewById(R.id.result_time_txt);
        TextView result_date_txt = (TextView) findViewById(R.id.result_date_txt);

        dbHelper = new DBHelper(this);

        String runId = getIntent().getExtras().getString("runId");
        int totalTime = getIntent().getExtras().getInt("totalTime");

        Cursor cursor = dbHelper.getRouteSummaryByRunId(runId);

        if(cursor != null){
            // Feed summary
            Log.i("Feed summary : ", "Before Feed summary...");

            cursor.moveToFirst();

            Log.i("Feed summary  : ", "Feed summary inside...");
            float totalDistance = cursor.getFloat(cursor.getColumnIndex("deltaDistance"));
            float avgSpeed = cursor.getFloat(cursor.getColumnIndex("speed"));
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            String dt = sdf.format(new Date());

            dbHelper.insertRouteSummary(runId, avgSpeed, totalDistance, totalTime, dt);
            Log.i("insertRouteSummary : ", "insertRouteSummary ok...");

            //--- Set Display Values
            String formattedTotalDistance = String.format("%.2f", totalDistance);
            String formattedAvgSpeed = String.format("%.2f", avgSpeed);

            total_distance_txt.setText(formattedTotalDistance);
            result_speed_txt.setText(formattedAvgSpeed);
            result_time_txt.setText("" + totalTime);
            result_date_txt.setText(dt);
        }
    }
}
