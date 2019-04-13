package com.example.packhacks2019.db;

import android.provider.BaseColumns;

public class TaskContract {

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "cards";

        public static final String COL_NAME = "name";
        public static final String COL_BALANCE = "balance";
    }
}