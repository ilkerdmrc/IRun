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
    public static final String TAG = "DB_ERR_LOG";
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
        TextView result_cal_txt = (TextView) findViewById(R.id.result_cal_txt);
        TextView result_date_txt = (TextView) findViewById(R.id.result_date_txt);

        dbHelper = new DBHelper(this);

        String runId = getIntent().getExtras().getString("runId");
        int totalTime = getIntent().getExtras().getInt("totalTime");
        Cursor cursor = null;

        try{
            cursor = dbHelper.getRouteSummaryByRunId(runId);

            if(cursor != null && cursor.moveToFirst()){
                // Feed summary
                Log.i("Feed summary  : ", "Feed summary inside...");
                float totalDistance = cursor.getFloat(cursor.getColumnIndex("deltaDistance"));
                float avgSpeed = cursor.getFloat(cursor.getColumnIndex("speed"));
                float totalCal = cursor.getFloat(cursor.getColumnIndex("deltaCal"));
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                String dt = sdf.format(new Date());

                dbHelper.insertRouteSummary(runId, avgSpeed, totalDistance, totalTime, totalCal, dt);
                Log.i("insertRouteSummary : ", "insertRouteSummary ok...");

                //--- Set Display Values
                String formattedTotalDistance = String.format("%.2f", totalDistance);
                String formattedAvgSpeed = String.format("%.2f", avgSpeed);

                total_distance_txt.setText(formattedTotalDistance);
                result_speed_txt.setText(formattedAvgSpeed);
                result_time_txt.setText("" + totalTime);
                result_cal_txt.setText("" + totalCal);
                result_date_txt.setText(dt);
            }
        } catch (final Exception e) {
            Log.e(TAG, "getRouteSummaryByRunId DB ERROR for runId : " + runId + " : "+ e.toString());
        } finally {
            cursor.close();
            dbHelper.close();
        }
    }
}
