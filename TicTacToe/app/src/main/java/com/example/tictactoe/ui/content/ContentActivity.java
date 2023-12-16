package com.example.tictactoe.ui.content;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.Nullable;
import com.google.firebase.database.ValueEventListener;
import com.example.tictactoe.MainActivity;
import com.example.tictactoe.R;
import com.example.tictactoe.ui.RulesActivity.RulesActivity;
import com.example.tictactoe.ui.history.HistoryActivity;
import com.example.tictactoe.ui.strategy.StrategyActivity;
import com.example.tictactoe.databinding.ContentActivityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;


public class ContentActivity extends AppCompatActivity implements LocationListener  {
    private FirebaseAuth firebaseAuth;
    // Constants for location permission request
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String LOCATION_PERMISSION_DENIED_KEY = "locationPermissionDenied";
    // Location related variables
    private LocationManager locationManager;
    private TextView locationTextView;
    private TextView totalUserGamesPlayedTextView;
    private TextView totalGamesPlayedTextView;
    private TextView stateTextView;
    private boolean locationPermissionDenied = false; // Added variable
    private static boolean initialLoad = true;
    private ContentActivityBinding binding;
    private int currentPlayer = 1; // Initial player is 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_activity);
        binding = ContentActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        // Initialize LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Getting necessary views
        totalUserGamesPlayedTextView = findViewById(R.id.total_user_games_played_text_view);
        totalGamesPlayedTextView = findViewById(R.id.total_games_played_text_view);
        updateUserGamesPlayedTextView();
        locationTextView = findViewById(R.id.location_text_view);
        stateTextView = findViewById(R.id.state_text_view);
        Button topLeftButton = findViewById(R.id.TopLeft);
        Button topMiddleButton = findViewById(R.id.TopMiddle);
        Button topRightButton = findViewById(R.id.TopRight);
        Button middleLeftButton = findViewById(R.id.MiddleLeft);
        Button middleMiddleButton = findViewById(R.id.MiddleMiddle);
        Button middleRightButton = findViewById(R.id.MiddleRight);
        Button bottomLeftButton = findViewById(R.id.BottomLeft);
        Button bottomMiddleButton = findViewById(R.id.BottomMiddle);
        Button bottomRightButton = findViewById(R.id.BottomRight);
        Button rulesButton = findViewById(R.id.rules_button);
        Button historyButton = findViewById(R.id.history_button);
        Button strategyButton = findViewById(R.id.strategy_button);
        // Set OnClickListener for each button
        setButtonClickListener(topLeftButton);
        setButtonClickListener(topMiddleButton);
        setButtonClickListener(topRightButton);
        setButtonClickListener(middleLeftButton);
        setButtonClickListener(middleMiddleButton);
        setButtonClickListener(middleRightButton);
        setButtonClickListener(bottomLeftButton);
        setButtonClickListener(bottomMiddleButton);
        setButtonClickListener(bottomRightButton);
        rulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContentActivity.this, RulesActivity.class);
                startActivity(i);
            }
        });
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContentActivity.this, HistoryActivity.class);
                startActivity(i);
            }
        });
        strategyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ContentActivity.this, StrategyActivity.class);
                startActivity(i);
            }
        });

        Button signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
        Button resetGameButton = findViewById(R.id.reset_game_button);
        resetGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
        if (initialLoad) {
            if (hasLocationPermission()) {
                startLocationUpdates();
            } else {
                requestLocationPermission();
            }
            initialLoad = false;
        }
        updatePlayerTurnText();
    }

    //FIREBASE METHODS
    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            //user not logged in return to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            //user logged in update text with their email
            String email = firebaseUser.getEmail();
            binding.emailTextView.setText(email);
        }
    }
    //This method gets the current user and updates their specific games played, and also the total games played to date on the app
    private void updateGamesPlayedInFirebase() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user's UID
            String uid = currentUser.getUid();
            // Reference to the Firebase node for the user
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
            // Reference to the Firebase "Master" node for the total games played
            DatabaseReference masterRef = FirebaseDatabase.getInstance().getReference().child("Master");


            //We add a listener for when totalGamesPlayed is changed we update the text
            masterRef.child("totalGamesPlayed").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        long updatedTotalGamesPlayed = (long) snapshot.getValue();
                        totalGamesPlayedTextView.setText("Total Games Played: " + updatedTotalGamesPlayed);

                        // Call the method to update the user's games played TextView
                        updateUserGamesPlayedTextView();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                    Toast.makeText(ContentActivity.this, "Failed to fetch totalGamesPlayed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


            // Increment the "gamesPlayed" property by 1 or set it to 1 if it doesn't exist
            userRef.child("gamesPlayed").runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    // Get the current value of gamesPlayed
                    Long gamesPlayed = mutableData.getValue(Long.class);
                    if (gamesPlayed == null) {
                        // If gamesPlayed is null, it means the property doesn't exist yet, set it to 1
                        mutableData.setValue(1);
                    } else {
                        // Increment gamesPlayed by 1
                        mutableData.setValue(gamesPlayed + 1);
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        // Handle the error
                        Toast.makeText(ContentActivity.this, "Failed to update gamesPlayed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Increment the "totalGamesPlayed" property in the "Master" node
                        masterRef.child("totalGamesPlayed").runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                // Get the current value of totalGamesPlayed
                                Long totalGamesPlayed = mutableData.getValue(Long.class);
                                if (totalGamesPlayed == null) {
                                    // If totalGamesPlayed is null, it means the property doesn't exist yet, set it to 1
                                    mutableData.setValue(1);
                                } else {
                                    // Increment totalGamesPlayed by 1
                                    mutableData.setValue(totalGamesPlayed + 1);
                                }
                                return Transaction.success(mutableData);
                            }
                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                if (error != null) {
                                    // Handle the error
                                    Toast.makeText(ContentActivity.this, "Failed to update totalGamesPlayed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    // Fetch the updated value of totalGamesPlayed from Firebase and update the TextView immediately
                                    masterRef.child("totalGamesPlayed").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                long updatedTotalGamesPlayed = (long) snapshot.getValue();
                                                totalGamesPlayedTextView.setText("Total Games Played: " + updatedTotalGamesPlayed);

                                                // Call the method to update the user's games played TextView
                                                updateUserGamesPlayedTextView();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle the error
                                            Toast.makeText(ContentActivity.this, "Failed to fetch totalGamesPlayed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    // This method updates the user's games played TextView
    private void updateUserGamesPlayedTextView() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

            DatabaseReference masterRef = FirebaseDatabase.getInstance().getReference().child("Master");

            // Get the updated value of gamesPlayed from Firebase and update the TextView
            userRef.child("gamesPlayed").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        long gamesPlayed = (long) snapshot.getValue();
                        totalUserGamesPlayedTextView.setText("Current User Games Played: " + gamesPlayed);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                    Toast.makeText(ContentActivity.this, "Failed to fetch gamesPlayed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            masterRef.child("totalGamesPlayed").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        long totalGamesPlayed = (long) snapshot.getValue();
                        totalGamesPlayedTextView.setText("Games Played For All Users: " + totalGamesPlayed);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                    Toast.makeText(ContentActivity.this, "Failed to fetch gamesPlayed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    //STATE METHODS:
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        // Save all relevant values to SharedPreferences, including button text
        saveAllValuesToSharedPreferences();
        // Print to console
        System.out.println("Paused");
    }
    public void onResume() {
        super.onResume();
        // Restore all relevant values from SharedPreferences, including button text
        restoreAllValuesFromSharedPreferences();
        if (hasLocationPermission()) {
            startLocationUpdates();
            // Restore location from SharedPreferences
            restoreLocationFromPreferences();
        } else {
            locationTextView.setText("N/A");
            stateTextView.setText("N/A");
            // requestLocationPermission(); // Consider requesting permission again here
        }
    }

    protected void onDestroy() {
        super.onDestroy();

//        // Remove location updates when the activity is destroyed
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }

    }

    //DATA STORAGE METHODS:
    private void saveAllValuesToSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ContentActivity.this);
        SharedPreferences.Editor editor = preferences.edit();
        // Save button text
        editor.putString("TopLeft", binding.TopLeft.getText().toString());
        editor.putString("TopMiddle", binding.TopMiddle.getText().toString());
        editor.putString("TopRight", binding.TopRight.getText().toString());
        editor.putString("MiddleLeft", binding.MiddleLeft.getText().toString());
        editor.putString("MiddleMiddle", binding.MiddleMiddle.getText().toString());
        editor.putString("MiddleRight", binding.MiddleRight.getText().toString());
        editor.putString("BottomLeft", binding.BottomLeft.getText().toString());
        editor.putString("BottomMiddle", binding.BottomMiddle.getText().toString());
        editor.putString("BottomRight", binding.BottomRight.getText().toString());
        editor.putString("HiddenTextView", binding.hiddenTextView.getText().toString());
        editor.putBoolean("hiddenTextViewVisible", binding.hiddenTextView.getVisibility() == View.VISIBLE);
        // Save other relevant values
        editor.putInt("currentPlayer", currentPlayer);
        editor.putBoolean(LOCATION_PERMISSION_DENIED_KEY, locationPermissionDenied);
        editor.putBoolean("initialLoad", initialLoad);
        editor.putString("StateText", binding.stateTextView.getText().toString());
        editor.putBoolean("locationPermissionGranted", hasLocationPermission());
        editor.putString("playerTurnText", binding.playerTurnText.getText().toString());

        editor.apply();
    }
    //
    private void restoreAllValuesFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ContentActivity.this);
        binding.TopLeft.setText(preferences.getString("TopLeft", ""));
        binding.TopMiddle.setText(preferences.getString("TopMiddle", ""));
        binding.TopRight.setText(preferences.getString("TopRight", ""));
        binding.MiddleLeft.setText(preferences.getString("MiddleLeft", ""));
        binding.MiddleMiddle.setText(preferences.getString("MiddleMiddle", ""));
        binding.MiddleRight.setText(preferences.getString("MiddleRight", ""));
        binding.BottomLeft.setText(preferences.getString("BottomLeft", ""));
        binding.BottomMiddle.setText(preferences.getString("BottomMiddle", ""));
        binding.BottomRight.setText(preferences.getString("BottomRight", ""));
        binding.hiddenTextView.setText(preferences.getString("HiddenTextView", ""));
        // Restore other relevant values
        currentPlayer = preferences.getInt("currentPlayer", 1);
        locationPermissionDenied = preferences.getBoolean(LOCATION_PERMISSION_DENIED_KEY, false);
        initialLoad = preferences.getBoolean("initialLoad", false);
        binding.stateTextView.setText(preferences.getString("StateText", ""));
        boolean hiddenTextViewVisible = preferences.getBoolean("hiddenTextViewVisible", false);
        binding.hiddenTextView.setVisibility(hiddenTextViewVisible ? View.VISIBLE : View.INVISIBLE);

        String playerTurnText = preferences.getString("playerTurnText", "");
        binding.playerTurnText.setText(playerTurnText);
        // Restore location permission
        boolean locationPermissionGranted = preferences.getBoolean("locationPermissionGranted", false);

    }
    //LOCATION METHODS:
    // Request location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(ContentActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
    // Request location updates
    private void startLocationUpdates() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(ContentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ContentActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 1 second interval
                    1,    // 1 meter distance change
                    locationListener
            );
        } else {
            // Handle the case where permission is not granted
            Toast.makeText(ContentActivity.this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
//    // LocationListener to receive location updates
    private final LocationListener locationListener = new LocationListener() {
    public void onLocationChanged(Location location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getFloat(LATITUDE_KEY, 0.0f) > 1.0) {
            restoreLocationFromPreferences();
        } else {
            // Update the UI with latitude and longitude
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                locationTextView.setText(String.format("Latitude: %f\nLongitude: %f", latitude, longitude));
                // Check if the location is within Michigan's bounds
                updateStateTextView(location);

                // Save the location information to SharedPreferences
                saveLocationToPreferences(latitude, longitude);
                saveAllValuesToSharedPreferences();
            } else {
                //locationTextView.setText("N/A");
            }
        }
    }

    @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Handle status changes if needed
      //  locationTextView.setText("Changed");
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            // Handle provider enabled
          //  locationTextView.setText("enabled");
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            // Handle provider disabled
           // locationTextView.setText("disabled");
        }
    };
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Handle location updates here
        // The 'location' parameter contains the updated location information
      //  locationTextView.setText("loc Changed");
    }

    // Helper method to save the location to SharedPreferences
    private void saveLocationToPreferences(double latitude, double longitude) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ContentActivity.this);
        SharedPreferences.Editor editor = preferences.edit();


        editor.putFloat(LATITUDE_KEY, (float) latitude);
        editor.putFloat(LONGITUDE_KEY, (float) longitude);
        editor.apply();
    }
    // Helper method to restore the location from SharedPreferences
    private void restoreLocationFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ContentActivity.this);
        float latitude = preferences.getFloat(LATITUDE_KEY, 0.0f);
        float longitude = preferences.getFloat(LONGITUDE_KEY, 0.0f);
        // Update the UI with latitude and longitude

        if(latitude < 1.0 && latitude > -1.0 && longitude < 1.0 && longitude > -1.0 ) {
            locationTextView.setText("Loading..");
        }else {
            locationTextView.setText(String.format("Latitude: %f\nLongitude: %f", latitude, longitude));
        }

    }

    // Check if location permission is granted
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(ContentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean hasDeniedPermission() {
        return ActivityCompat.checkSelfPermission(ContentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED;
    }

        private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }


    private boolean isLocationInMichigan(Location location) {
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Michigan's bounds
            double minLatitude = 41.6833;
            double maxLatitude = 48.3;
            double minLongitude = -90.4167;
            double maxLongitude = -82.1167;

            // Check if the location is within Michigan's bounds
            return (latitude >= minLatitude && latitude <= maxLatitude) &&
                    (longitude >= minLongitude && longitude <= maxLongitude);
        }

        return false;
    }
    // Add this method to your HomeFragment class
    private void updateStateTextView(Location location) {
        if (isLocationInMichigan(location)) {
            stateTextView.setText("Location is in Michigan");
        } else {
            stateTextView.setText("Location is outside Michigan");
        }
    }

    //TIC TAC TOE FUNCTIONALITY METHODS:
    // Helper method to set OnClickListener for a button
    // Helper method to set OnClickListener for a button
    // Helper method to set OnClickListener for a button
    private void setButtonClickListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the game is already over
                if (binding.hiddenTextView.getVisibility() == View.VISIBLE) {
                    // Game is over, show alert and return
                    showGameOverAlert();
                    return;
                }

                // Check if the button is already filled
                if (!button.getText().toString().isEmpty()) {
                    // Button is already taken, show alert
                    showCellTakenAlert();
                    return;
                }

                // Set the text to the current player's symbol
                String symbol = (currentPlayer == 1) ? "X" : "O";
                button.setText(symbol);

                // Check for a win
                if (checkForWin()) {
                    if (currentPlayer == 1) {
                        binding.hiddenTextView.setText(R.string.player_1_won_text);
                    } else {
                        binding.hiddenTextView.setText(R.string.player_2_won_text);
                    }
                    // Show the hiddenTextView if there is a win
                    binding.hiddenTextView.setVisibility(View.VISIBLE);
                    showGameOverAlert();
                    return;  // Exit the method to prevent further moves after a win
                } else if (allButtonsFilled()) {
                    binding.hiddenTextView.setText(R.string.game_draw_text);
                    binding.hiddenTextView.setVisibility(View.VISIBLE);
                    showGameOverAlert();
                    return; // Exit the method if it's a draw
                }

                // Toggle the player for the next turn
                currentPlayer = (currentPlayer == 1) ? 2 : 1;

                // Update the player turn text
                updatePlayerTurnText();
            }
        });
    }

    // Helper method to show an alert when the cell is already taken
    private void showCellTakenAlert() {
        new AlertDialog.Builder(ContentActivity.this)
                .setTitle("Cell Taken")
                .setMessage("Select another cell, already taken.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with the action or dismiss the dialog
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    // Helper method to show a game over alert
    private void showGameOverAlert() {
        new AlertDialog.Builder(ContentActivity.this)
                .setTitle("Game Over")
                .setMessage("Reset if you want to play again")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with the action
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Helper method to check for a win
    private boolean checkForWin() {
        // Check for horizontal win
        if (checkRowForWin(0, 1, 2) || checkRowForWin(3, 4, 5) || checkRowForWin(6, 7, 8)) {
            return true;
        }

        // Check for vertical win
        if (checkRowForWin(0, 3, 6) || checkRowForWin(1, 4, 7) || checkRowForWin(2, 5, 8)) {
            return true;
        }

        // Check for diagonal win
        return checkRowForWin(0, 4, 8) || checkRowForWin(2, 4, 6);
    }
    // Helper method to check a row for a win
    private boolean checkRowForWin(int first, int second, int third) {
        String[] buttonsText = {
                binding.TopLeft.getText().toString(), binding.TopMiddle.getText().toString(), binding.TopRight.getText().toString(),
                binding.MiddleLeft.getText().toString(), binding.MiddleMiddle.getText().toString(), binding.MiddleRight.getText().toString(),
                binding.BottomLeft.getText().toString(), binding.BottomMiddle.getText().toString(), binding.BottomRight.getText().toString()
        };
        return buttonsText[first].equals(buttonsText[second]) && buttonsText[second].equals(buttonsText[third]) && !buttonsText[first].isEmpty();
    }
//    // Helper method to update player turn text
    private void updatePlayerTurnText() {
        String playerTurnText;
        if (currentPlayer == 1) {
            playerTurnText = getString(R.string.player_1_turn_text);
        } else {
            playerTurnText = getString(R.string.player_2_turn_text);
        }
        TextView textView = binding.playerTurnText;
        textView.setText(playerTurnText);
    }
    //Helper method to determine if all the buttons are filled
    private boolean allButtonsFilled() {
        Button[] buttons = {
                binding.TopLeft, binding.TopMiddle, binding.TopRight,
                binding.MiddleLeft, binding.MiddleMiddle, binding.MiddleRight,
                binding.BottomLeft, binding.BottomMiddle, binding.BottomRight
        };
        for (Button button : buttons) {
            if (button.getText().toString().isEmpty()) {
                return false; // If any button is empty, return false
            }
        }
        return true; // All buttons are filled
    }

    private void resetGame() {
        // Clear the text of all buttons
        binding.TopLeft.setText("");
        binding.TopMiddle.setText("");
        binding.TopRight.setText("");
        binding.MiddleLeft.setText("");
        binding.MiddleMiddle.setText("");
        binding.MiddleRight.setText("");
        binding.BottomLeft.setText("");
        binding.BottomMiddle.setText("");
        binding.BottomRight.setText("");

        // Hide the hiddenTextView
        binding.hiddenTextView.setVisibility(View.INVISIBLE);

        // Reset current player to 1
        currentPlayer = 1;

        // Update the player turn text
        updatePlayerTurnText();
        // Update gamesPlayed in Firebase
        updateGamesPlayedInFirebase();
    }





}