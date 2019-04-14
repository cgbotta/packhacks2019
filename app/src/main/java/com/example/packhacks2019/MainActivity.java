package com.example.packhacks2019;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.packhacks2019.db.CardTable;
import com.example.packhacks2019.db.LocationTable;
import com.example.packhacks2019.db.TaskDbHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private double lat;
    private double lng;
    private ArrayAdapter<String> mAdapter;
    private FusedLocationProviderClient locationClient;
    private static final int LOCATION_REQUEST_CODE = 0;
    private String REQUESTING_LOCATION_UPDATES_KEY = "requesting_location_updates";
    private boolean requestingLocationUpdates = false;
    private String CHANNEL_ID = "gift_card_notification";
    private int notificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new TaskDbHelper(this);
        mTaskListView = findViewById(R.id.list_todo);
        createNotificationChannel();

        updateUI();
        // Get the phone's current location
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_task:
                LinearLayout layout = new LinearLayout(this);
                layout.setOrientation(LinearLayout.VERTICAL);
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);

                // Add a TextView here for the "Title" label
                final EditText titleBox = new EditText(this);
                titleBox.setHint("Name of company");
                layout.addView(titleBox); // Notice this is an add method

                // Add another TextView here for the "Balance" label
                final EditText balanceBox = new EditText(this);
                balanceBox.setHint("Balance on card");
                layout.addView(balanceBox); // Another add method

                dialog.setView(layout); // Again this is a set method, not add
                dialog.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = String.valueOf(titleBox.getText());
                                SQLiteDatabase db = mHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put(CardTable.CardTableEntry.COL_NAME, name);
                                String balance = String.valueOf(balanceBox.getText());
                                values.put(CardTable.CardTableEntry.COL_BALANCE, balance);
                                db.insertWithOnConflict(CardTable.CardTableEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                db.close();
                                updateUI();
                                queryPlacesAPI(name);

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);
        String card = String.valueOf(taskTextView.getText());
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String[] colName = {card.substring(0, card.indexOf(" "))};
        db.delete(CardTable.CardTableEntry.TABLE,
                CardTable.CardTableEntry.COL_NAME + " = ?",
               colName);
        db.delete(LocationTable.LocationTableEntry.TABLE,
                LocationTable.LocationTableEntry.COL_NAME + " = ?",
                colName);
        db.close();
        updateUI();
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(CardTable.CardTableEntry.TABLE,
                new String[]{CardTable.CardTableEntry._ID, CardTable.CardTableEntry.COL_NAME, CardTable.CardTableEntry.COL_BALANCE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int nameIdx = cursor.getColumnIndex(CardTable.CardTableEntry.COL_NAME);
            int balanceIdx = cursor.getColumnIndex(CardTable.CardTableEntry.COL_BALANCE);
            taskList.add(cursor.getString(nameIdx) + "    Balance: $" + cursor.getString(balanceIdx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<String>(this,
                    R.layout.gift_card,
                    R.id.task_title,
                    taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }

        cursor.close();
        db.close();
    }

    private void queryPlacesAPI(final String name) {
        String apiKey = BuildConfig.PlacesAPIKey;
        final String url = String.format(
                Locale.US,
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=50000&keyword=%s&key=%s",
                lat,
                lng,
                name,
                apiKey
        );
        final RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String nextUrl;
                    if (response.has("next_page_token")) {
                        String nextPageToken = response.getString("next_page_token");
                        nextUrl = url + "&pagetoken=" + nextPageToken;
                    }
                    JSONArray results = response.getJSONArray("results");
                    SQLiteDatabase db = mHelper.getWritableDatabase();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject place = results.getJSONObject(i);
                        double latitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        double longitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        ContentValues values = new ContentValues();
                        values.put(LocationTable.LocationTableEntry.COL_NAME, name);
                        values.put(LocationTable.LocationTableEntry.COL_LATITUDE, latitude);
                        values.put(LocationTable.LocationTableEntry.COL_LONGITUDE, longitude);
                        db.insertWithOnConflict(LocationTable.LocationTableEntry.TABLE,
                                null,
                                values,
                                SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    db.close();
                } catch (JSONException e ) {
                    // Shouldn't happen if Google's json is consistent
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
            }
        });
        queue.add(request);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        requestingLocationUpdates = true;
        try {
            locationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                    }
                }
            });
            // Create with default params. Can change them if needed
            LocationRequest request = LocationRequest.create();
            locationClient.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<String> stores = getNearbyStores(locationResult);
                    if (stores.size() > 0) {
                        sendNotification(stores);
                    }
                }
            }, null /* Looper */);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Query for stores that are within 200 meters of the current location
     */
    private List<String> getNearbyStores(LocationResult locationResult) {
        // https://stackoverflow.com/a/1253545
        double latDistance = 200.0 / 110574.0;
        double lngDistance = Math.abs(200.0 / (111320.0 * Math.cos(locationResult.getLastLocation().getLatitude())));
        String minLat = Double.toString(locationResult.getLastLocation().getLatitude() - latDistance);
        String maxLat = Double.toString(locationResult.getLastLocation().getLatitude() + latDistance);
        String minLng = Double.toString(locationResult.getLastLocation().getLongitude() - lngDistance);
        String maxLng = Double.toString(locationResult.getLastLocation().getLongitude() + lngDistance);

        SQLiteDatabase db = mHelper.getReadableDatabase();
        List<String> nameList = new ArrayList<>();
        Cursor cursor = db.query(
                true,
                LocationTable.LocationTableEntry.TABLE,
                new String[] {LocationTable.LocationTableEntry.COL_NAME},
                "latitude > ? AND latitude < ? AND longitude > ? AND longitude < ?",
                new String[] {minLat, maxLat, minLng, maxLng},
                null,
                null,
                null,
                null
        );
        while (cursor.moveToNext()) {
            int nameIdx = cursor.getColumnIndex(LocationTable.LocationTableEntry.COL_NAME);
            nameList.add(cursor.getString(nameIdx));
        }
        cursor.close();
        db.close();
        return nameList;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * Notify the user that they have a gift card at the store(s) they're nearby
     */
    private void sendNotification(List<String> stores) {
        StringBuilder storesString = new StringBuilder(stores.get(0));
        if (stores.size() == 2) {
            storesString.append(" and ");
            storesString.append(stores.get(1));
        } else {
            for (int i = 1; i < stores.size() - 1; i++) {
                storesString.append(", ");
                storesString.append(stores.get(i));
            }
            storesString.append(" and ");
            storesString.append(stores.get(stores.size() - 1));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.giftcard)
                .setContentTitle("Gift card store nearby!")
                .setContentText("Don't forget to use your gift " + (stores.size() > 1 ? "cards" : "card") + " at " + storesString.toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId++, builder.build());
    }

}