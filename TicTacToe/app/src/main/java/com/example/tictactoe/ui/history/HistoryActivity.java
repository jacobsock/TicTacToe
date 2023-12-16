package com.example.tictactoe.ui.history;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tictactoe.MainActivity;
import com.example.tictactoe.R;
import android.content.Intent;
import android.widget.Button;
import com.example.tictactoe.ui.content.ContentActivity;

public class HistoryActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity);
        Button backButton = findViewById(R.id.history_activity_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, ContentActivity.class);
                startActivity(intent);
                finish(); // Optional: Finish the HistoryActivity to remove it from the back stack
            }
        });
    }


}
