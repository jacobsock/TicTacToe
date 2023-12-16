package com.example.tictactoe.ui.strategy;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tictactoe.MainActivity;
import com.example.tictactoe.R;
import android.content.Intent;
import android.widget.Button;
import com.example.tictactoe.ui.content.ContentActivity;

public class StrategyActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.strategy_activity);
        Button backButton = findViewById(R.id.strategy_activity_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use an Intent to navigate back to HomeActivity (or HomeFragment)
                Intent intent = new Intent(com.example.tictactoe.ui.strategy.StrategyActivity.this, ContentActivity.class);
                startActivity(intent);
                finish(); // Optional: Finish the StrategyActivity to remove it from the back stack
            }
        });
    }

}

