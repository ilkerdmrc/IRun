package com.idemirci.irun.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.StringTokenizer;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "test_gps.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table routes " +
                        "(_id integer primary key,runid text, lat text,lng text, deltaDistance real, speed real, dt datetime default current_timestamp)"
        );

        db.execSQL(
                "create table routeSummary " +
                        "(_id integer primary key,runid text, avgSpeed text, totalDistance text, totalTime text, activityDate datetime default current_timestamp)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS routes");
        db.execSQL("DROP TABLE IF EXISTS routeSummary");
        onCreate(db);
    }

    public boolean insertRouteSummary (String runId, float avgSpeed, float totalDistance, int totalTime, String activityDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("runid", runId);
        contentValues.put("avgSpeed", avgSpeed);
        contentValues.put("totalDistance", totalDistance);
        contentValues.put("totalTime", totalTime);
        contentValues.put("activityDate", activityDate);
        db.insert("routeSummary", null, contentValues);
        return true;
    }

    public boolean insertRoute (String runId, String lat, String lng, float deltaDistance, float speed , String dt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("runid", runId);
        contentValues.put("lat", lat);
        contentValues.put("lng", lng);
        contentValues.put("deltaDistance", deltaDistance);
        contentValues.put("speed", speed);
        contentValues.put("dt", dt);
        db.insert("routes", null, contentValues);
        return true;
    }

    public Cursor getRouteByRunId(String runId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from routes where runid='" + runId+"'", null );
        return res;
    }

    public Cursor getAllRoutes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from routes", null );
        return res;
    }

    /**
     * Used by history fragment
     * @return
     */
    public Cursor getSummaryForHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select totalDistance, avgSpeed, " +
                "runid as _id, totalTime, activityDate from routeSummary ORDER BY activityDate desc limit 5", null );
        return res;
    }

    public Cursor getRouteSummaryByRunId(String runId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select SUM(deltaDistance) as deltaDistance, AVG(speed) as speed, runid as _id, dt from routes WHERE runid = '" + runId + "' GROUP BY runid", null );
        return res;
    }

    public String getCurrentTotalDistance(String runId){
        float formattedDistance = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select SUM(deltaDistance) as totalDistance from routes " +
                "WHERE runid = '" + runId + "' GROUP BY runid", null );

        if(cursor != null){
            cursor.moveToFirst();
            float totalDistance = cursor.getFloat(cursor.getColumnIndex("totalDistance"));
            formattedDistance = ((float) Math.round(Math.abs(totalDistance)))/1000;

        }
        return String.valueOf(formattedDistance);
    }

}
