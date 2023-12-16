package com.example.tictactoe.ui.RulesActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tictactoe.MainActivity;
import com.example.tictactoe.R;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tictactoe.R;
import com.example.tictactoe.ui.home.HomeFragment; // Make sure to import the correct package for HomeActivity
import com.example.tictactoe.ui.content.ContentActivity;

public class RulesActivity extends AppCompatActivity {

    private TextView rulesActivityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_activity);

        rulesActivityTextView = findViewById(R.id.rules_activity_text);

        Button backButton = findViewById(R.id.rules_activity_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use an Intent to navigate back to HomeActivity (or HomeFragment)
                Intent intent = new Intent(RulesActivity.this, ContentActivity.class);
                startActivity(intent);
                finish(); // Optional: Finish the RulesActivity to remove it from the back stack
            }
        });
    }
}

