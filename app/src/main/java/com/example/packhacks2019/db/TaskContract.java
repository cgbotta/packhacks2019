package com.example.packhacks2019.db;

import android.provider.BaseColumns;

public class TaskContract {
    public static final String DB_NAME = "com.example.packhacks2019.db";
    public static final int DB_VERSION = 1;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "cards";

        public static final String COL_NAME = "name";
        public static final String COL_BALANCE = "balance";
    }
}