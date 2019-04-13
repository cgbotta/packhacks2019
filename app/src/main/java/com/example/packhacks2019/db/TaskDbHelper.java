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
        String createTableCards = "CREATE TABLE " + TaskContract.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskContract.TaskEntry.COL_NAME + " TEXT NOT NULL, " + TaskContract.TaskEntry.COL_BALANCE + " REAL NOT NULL);";

        String createTableLocations = "CREATE TABLE " + LocationTable.TaskEntry.TABLE + " ( " +
                TaskContract.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LocationTable.TaskEntry.COL_NAME + " TEXT NOT NULL, " + LocationTable.TaskEntry.COL_LATITUDE + LocationTable.TaskEntry.COL_LONGITUDE + " TEXT NOT NULL, " + " REAL NOT NULL );";

        db.execSQL(createTableCards);
        db.execSQL(createTableLocations);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskContract.TaskEntry.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LocationTable.TaskEntry.TABLE);
        onCreate(db);
    }
}