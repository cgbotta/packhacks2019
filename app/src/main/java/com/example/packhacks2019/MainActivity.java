package com.example.packhacks2019;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
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
import com.example.packhacks2019.db.TaskContract;
import com.example.packhacks2019.db.TaskDbHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TaskDbHelper mHelper;
    private ListView mTaskListView;
    private double lat;
    private double lng;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new TaskDbHelper(this);
        mTaskListView = findViewById(R.id.list_todo);

        updateUI();
        // Get the phone's current location
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);

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
                                values.put(TaskContract.TaskEntry.COL_NAME, name);
                                String balance = String.valueOf(balanceBox.getText());
                                values.put(TaskContract.TaskEntry.COL_BALANCE, balance);
                                db.insertWithOnConflict(TaskContract.TaskEntry.TABLE,
                                        null,
                                        values,
                                        SQLiteDatabase.CONFLICT_REPLACE);
                                updateUI();
                                queryPlacesAPI(name, db);
                                db.close();
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
        db.delete(TaskContract.TaskEntry.TABLE,
                TaskContract.TaskEntry.COL_NAME + " = ?",
               colName);
        db.close();
        updateUI();
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskContract.TaskEntry.TABLE,
                new String[]{TaskContract.TaskEntry._ID, TaskContract.TaskEntry.COL_NAME, TaskContract.TaskEntry.COL_BALANCE},
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int nameIdx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_NAME);
            int balanceIdx = cursor.getColumnIndex(TaskContract.TaskEntry.COL_BALANCE);
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

    private void queryPlacesAPI(String name, SQLiteDatabase db) {
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
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject place = results.getJSONObject(i);
                        double latitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                        double longitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                        // TODO: Add lat/lng to db associated with name
                    }
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
}