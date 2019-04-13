package com.example.packhacks2019;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void createNewCard(View view) {
        // Start the NewCardActivity
        Intent intent = new Intent(this, NewCardActivity.class);
        startActivity(intent);
    }

}
