package com.example.packhacks2019.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {

    public TaskDbHelper(Context context) {
        super(context, "com.example.packhacks2019.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableCards = "CREATE TABLE " + CardTable.CardTableEntry.TABLE + " ( " +
                CardTable.CardTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CardTable.CardTableEntry.COL_NAME + " TEXT NOT NULL, " + CardTable.CardTableEntry.COL_BALANCE + " REAL NOT NULL);";

        String createTableLocations = "CREATE TABLE " + LocationTable.LocationTableEntry.TABLE + " ( " +
                LocationTable.LocationTableEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationTable.LocationTableEntry.COL_NAME + " TEXT NOT NULL, " +
                LocationTable.LocationTableEntry.COL_LATITUDE + " REAL NOT NULL," +
                LocationTable.LocationTableEntry.COL_LONGITUDE + " REAL NOT NULL);";

        db.execSQL(createTableCards);
        db.execSQL(createTableLocations);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CardTable.CardTableEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable.LocationTableEntry.TABLE);
        onCreate(db);
    }
}