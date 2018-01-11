package com.idemirci.irun.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "test_gps.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table routes " +
                        "(_id integer primary key, runid text, lat text,lng text, deltaDistance real, speed real, deltaBpm real, dt datetime default current_timestamp)"
        );

        db.execSQL(
                "create table routeSummary " +
                        "(_id integer primary key,runid text, avgSpeed text, totalDistance text, totalTime text, totalCal text, activityDate datetime default current_timestamp)"
        );

        db.execSQL(
                "create table personalInformation " +
                        "(_id integer primary key,age Integer, weight Integer, heartbeat text, totalTime text, sex text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS routes");
        db.execSQL("DROP TABLE IF EXISTS routeSummary");
        db.execSQL("DROP TABLE IF EXISTS personalInformation");
        onCreate(db);
    }

    public boolean insertRouteSummary(String runId, float avgSpeed, float totalDistance, float totalTime, float totalCal, String activityDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("runid", runId);
        contentValues.put("avgSpeed", avgSpeed);
        contentValues.put("totalDistance", totalDistance);
        contentValues.put("totalTime", totalTime);
        contentValues.put("totalCal", totalCal);
        contentValues.put("activityDate", activityDate);
        db.insert("routeSummary", null, contentValues);
        return true;
    }

    public boolean insertPersonalInfo(int age, int weight, String sex){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("age", age);
        contentValues.put("weight",weight);
        contentValues.put("sex",sex);
        db.insert("personalInformation", null, contentValues);
        return true;
    }

    public boolean insertRoute(String runId, String lat, String lng, float deltaDistance, float speed, float deltaBpm,  String dt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("runid", runId);
        contentValues.put("lat", lat);
        contentValues.put("lng", lng);
        contentValues.put("deltaDistance", deltaDistance);
        contentValues.put("speed", speed);
        contentValues.put("deltaBpm", deltaBpm);
        contentValues.put("dt", dt);
        db.insert("routes", null, contentValues);
        return true;
    }

    public Cursor getRouteByRunId(String runId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from routes where runid='" + runId + "'", null);
        return res;
    }

    public Cursor getAllRoutes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from routes", null);
        return res;
    }

    /**
     * Used by history fragment
     *
     * @return
     */
    public Cursor getSummaryForHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select totalDistance, avgSpeed, " +
                "runid as _id, totalTime, activityDate from routeSummary ORDER BY activityDate desc limit 5", null);
        return res;
    }

    public Cursor getRouteSummaryByRunId(String runId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select SUM(deltaDistance) as deltaDistance, AVG(speed) as speed, runid as _id, dt from routes WHERE runid = '" + runId + "' GROUP BY runid", null);
        return res;
    }

    public float getCurrentTotalDistance(String runId) {
        float formattedDistance = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select SUM(deltaDistance) as totalDistance from routes " +
                "WHERE runid = '" + runId + "' GROUP BY runid", null);

        if (cursor != null) {
            cursor.moveToFirst();
            float totalDistance = cursor.getFloat(cursor.getColumnIndex("totalDistance"));
            formattedDistance = ((float) Math.round(Math.abs(totalDistance)));

        }
        return formattedDistance;
    }

    /**
     * Called by chronoRestart method
     * Deletes runid rows
     *
     * @param runId
     */
    public void removeRunRecords(String runId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("DELETE FROM routes WHERE runid = '" + runId + "'");
        database.close();
    }

    public Integer checkTable() {
        SQLiteDatabase database = this.getWritableDatabase();
        String count = "SELECT * FROM personalInformation";
        Cursor mcursor = database.rawQuery(count, null);

        mcursor.moveToFirst();
        int icount =  mcursor.getCount();
        return  icount;
    }

    public int getAge(){
        int getAge = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select age from personalInformation", null);

        if (cursor != null) {
            cursor.moveToFirst();
            getAge = cursor.getInt(cursor.getColumnIndex("age"));
        }
        return getAge;
    }

    public int getWeight(){
        int weight = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select weight from personalInformation", null);

        if (cursor != null) {
            cursor.moveToFirst();
            weight = cursor.getInt(cursor.getColumnIndex("weight"));
        }
        return weight;
    }

    public String getSex(){
        String sex = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select sex from personalInformation", null);

        if (cursor != null) {
            cursor.moveToFirst();
            sex = cursor.getString(cursor.getColumnIndex("sex"));
        }
        return sex;
    }

    public float getAvgBpm(){
        float avgBpm = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select AVG(deltaBpm) as avgBpm from routes", null);

        if (cursor != null) {
            cursor.moveToFirst();
            avgBpm = cursor.getFloat(cursor.getColumnIndex("avgBpm"));
        }
        return avgBpm;
    }

    public Cursor getAllLatLng(String runId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select lat,lng from routes WHERE runid = '" + runId + "'", null);
        return res;
    }


}