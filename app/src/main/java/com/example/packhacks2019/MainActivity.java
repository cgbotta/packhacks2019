package com.example.packhacks2019;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ArrayList<GiftCard> giftCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            GiftCard addedCard = (GiftCard) extras.get("key");
            giftCards.add(addedCard);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void createNewCard(View view) {
        // Start the NewCardActivity
        Intent intent = new Intent(this, NewCardActivity.class);
        startActivity(intent);
    }

}
