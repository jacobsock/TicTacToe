package com.example.tictactoe.ui.home;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.tictactoe.R;
import com.example.tictactoe.databinding.FragmentHomeBinding;
import com.example.tictactoe.databinding.ContentActivityBinding;
import com.example.tictactoe.ui.RulesActivity.RulesActivity;
import com.example.tictactoe.ui.history.HistoryActivity;
import com.example.tictactoe.ui.strategy.StrategyActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.location.LocationListener;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
public class HomeFragment extends Fragment implements LocationListener {

    // Constants for location permission request
    private static final String LATITUDE_KEY = "latitude";
    private static final String LONGITUDE_KEY = "longitude";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String LOCATION_PERMISSION_DENIED_KEY = "locationPermissionDenied";
    // Location related variables
    private LocationManager locationManager;
    private TextView locationTextView;
    private TextView stateTextView;
    private boolean locationPermissionDenied = false; // Added variable
    private static boolean initialLoad = true;
    private FragmentHomeBinding binding;
    private int currentPlayer = 1; // Initial player is 1
    private String buttonBackgroundColor = "LightButtons"; // Default mode
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Initialize LocationManager
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        //Getting necessary views
        locationTextView = root.findViewById(R.id.location_text_view);
        stateTextView = root.findViewById(R.id.state_text_view);

        // Find all the buttons
        Button topLeftButton = root.findViewById(R.id.TopLeft);
        Button topMiddleButton = root.findViewById(R.id.TopMiddle);
        Button topRightButton = root.findViewById(R.id.TopRight);
        Button middleLeftButton = root.findViewById(R.id.MiddleLeft);
        Button middleMiddleButton = root.findViewById(R.id.MiddleMiddle);
        Button middleRightButton = root.findViewById(R.id.MiddleRight);
        Button bottomLeftButton = root.findViewById(R.id.BottomLeft);
        Button bottomMiddleButton = root.findViewById(R.id.BottomMiddle);
        Button bottomRightButton = root.findViewById(R.id.BottomRight);
        Button rulesButton = root.findViewById(R.id.rules_button);
        Button historyButton = root.findViewById(R.id.history_button);
        Button strategyButton = root.findViewById(R.id.strategy_button);
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
                Intent i = new Intent(getActivity(), RulesActivity.class);
                startActivity(i);
            }
        });
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), HistoryActivity.class);
                startActivity(i);
            }
        });
        strategyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), StrategyActivity.class);
                startActivity(i);
            }
        });


        if (initialLoad) {
            if (hasLocationPermission()) {
                startLocationUpdates();
                //locationRequestInitiated = true;
            } else {
                requestLocationPermission();
            }
            initialLoad = false;
        }

        // Initial player turn text
        updatePlayerTurnText();

        return root;
    }
    // Request location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }
    // Request location updates
    private void startLocationUpdates() {
        // Check for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000, // 1 second interval
                    1,    // 1 meter distance change
                    locationListener
            );
        } else {
            // Handle the case where permission is not granted
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }


    // LocationListener to receive location updates
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
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
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            // Handle provider enabled
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            // Handle provider disabled
        }
    };
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Handle location updates here
        // The 'location' parameter contains the updated location information
    }

    // Helper method to save the location to SharedPreferences
    private void saveLocationToPreferences(double latitude, double longitude) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = preferences.edit();


        editor.putFloat(LATITUDE_KEY, (float) latitude);
        editor.putFloat(LONGITUDE_KEY, (float) longitude);
        editor.apply();
    }
    // Helper method to restore the location from SharedPreferences
    private void restoreLocationFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
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
            return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    private boolean hasDeniedPermission() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED;
    }

        // Stop location updates when the fragment is destroyed
        @Override
        public void onDestroyView() {
            super.onDestroyView();
            // Remove location updates when the fragment is destroyed
            if (locationManager != null && locationListener != null) {
                locationManager.removeUpdates(locationListener);
            }
            binding = null;
            //locationManager = null;
        }
        // Helper method to set OnClickListener for a button
        private void setButtonClickListener(Button button) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                        return;  // Exit the method to prevent further moves after a win
                    } else if (allButtonsFilled()) {
                        binding.hiddenTextView.setText(R.string.game_draw_text);
                        binding.hiddenTextView.setVisibility(View.VISIBLE);
                        return; // Exit the method if it's a draw
                    }

                    // Toggle the player for the next turn
                    currentPlayer = (currentPlayer == 1) ? 2 : 1;

                    // Update the player turn text
                    updatePlayerTurnText();
                }
            });
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

        // Helper method to update player turn text
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

        // Helper method to set the background color for all buttons
        private void setButtonBackgrounds() {
            int backgroundColor = buttonBackgroundColor.equals("DarkButtons") ? R.color.black : R.color.gray;
            int textColor = buttonBackgroundColor.equals("DarkButtons") ? R.color.white : R.color.black;

            Button[] buttons = {
                    binding.TopLeft, binding.TopMiddle, binding.TopRight,
                    binding.MiddleLeft, binding.MiddleMiddle, binding.MiddleRight,
                    binding.BottomLeft, binding.BottomMiddle, binding.BottomRight
            };

            for (Button button : buttons) {
                button.setBackgroundColor(getResources().getColor(backgroundColor));
                button.setTextColor(getResources().getColor(textColor));
            }
        }

//    @Override
@Override
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
    private void saveAllValuesToSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
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

        editor.apply();
    }

    private void restoreAllValuesFromSharedPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Restore button text
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
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();

        // Save all relevant values to SharedPreferences, including button text
        saveAllValuesToSharedPreferences();
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



    }






