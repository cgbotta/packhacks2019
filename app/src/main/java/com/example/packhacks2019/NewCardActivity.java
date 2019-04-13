package com.example.packhacks2019;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;

//import com.google.android.libraries.places.api.Places;
//import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * A form that lets users add new cards
 */
public class NewCardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_card);

        //Mock up of what will happen once we have UI working
        GiftCard card = new GiftCard("Target", 25);

        Intent i = new Intent(NewCardActivity.this, MainActivity.class);
        i.putExtra("cardToAdd", (Parcelable) card);
        startActivity(i);


        // Initialize connection to Google Places API
        //String apiKey = BuildConfig.PlacesAPIKey;
//        Places.initialize(getApplicationContext(), apiKey);
//        PlacesClient placesClient = Places.createClient(this);
    }
}

