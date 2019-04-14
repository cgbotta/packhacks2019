package com.example.packhacks2019.db;

import android.provider.BaseColumns;

public class LocationTable {

    public class LocationTableEntry implements BaseColumns {
        public static final String TABLE = "locations";

        public static final String COL_NAME = "name";
        public static final String COL_LATITUDE = "latitude";
        public static final String COL_LONGITUDE = "longitude";

    }
}