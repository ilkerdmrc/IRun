package com.idemirci.irun.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.idemirci.irun.R;

import java.text.DecimalFormat;
import java.util.Date;


public class RouteListAdapter extends CursorAdapter {

    private int index;

    public RouteListAdapter(Context context, Cursor cursor, int flag){
        super(context, cursor, 0);
        index = 0;
    }

    public void bindView(View view, Context context, Cursor cursor){

        index++;
        TextView txtRouteId = (TextView) view.findViewById(R.id.txtRouteId);
        TextView txtSpeed = (TextView) view.findViewById(R.id.txtSpeed);
        TextView txtDistance = (TextView) view.findViewById(R.id.txtDistance);
        TextView txtTime = (TextView) view.findViewById(R.id.txtTime);
        TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
        TextView txtRunID = (TextView) view.findViewById(R.id.txtRunId);




        float avgSpeed = cursor.getFloat(cursor.getColumnIndex("avgSpeed"));
        float totalDistance = cursor.getFloat(cursor.getColumnIndex("totalDistance"));
        float totalTime = cursor.getFloat(cursor.getColumnIndex("totalTime"));
        String activityDate = cursor.getString(cursor.getColumnIndex("activityDate"));
        String runId = cursor.getString(cursor.getColumnIndex("_id"));



        String formattedSpeed = String.format("%.2f", avgSpeed);
        float formattedDistance = ((float) Math.round(Math.abs(totalDistance)))/1000;

        txtRouteId.setText(""+index);

        String avgSpeedLbl = context.getResources().getString(R.string.history_fragment_avg_speed );
        String totalDistanceLbl = context.getResources().getString(R.string.history_fragment_total_distance);
        String totalTimeLbl = context.getResources().getString(R.string.history_fragment_total_time);
        String activityDateLbl = context.getResources().getString(R.string.history_fragment_activity_date);

        txtSpeed.setText(avgSpeedLbl + " : " + formattedSpeed +" m/s" );
        txtDistance.setText(totalDistanceLbl + " : " + formattedDistance + " km");
        txtTime.setText(totalTimeLbl + " : " + new DecimalFormat("##.##").format(totalTime) + " min");
        txtDate.setText(activityDateLbl + " : " + activityDate);
        txtRunID.setText(runId);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent){
        return LayoutInflater.from(context).inflate(R.layout.data_list, parent,false);
    }

}
