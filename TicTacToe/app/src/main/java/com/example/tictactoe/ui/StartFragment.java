package com.example.tictactoe.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation; // Add this import

import com.example.tictactoe.R; // Make sure to import your R class with the correct package name
import com.example.tictactoe.databinding.FragmentStartBinding;

public class StartFragment extends Fragment {

    public StartFragment() {
        // Required empty public constructor
    }

    private boolean authBool = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);

        Button startButton = rootView.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to the HomeFragment when the button is clicked
                if(authBool) {
                    Navigation.findNavController(view)
                            .navigate(R.id.action_startFragment_to_homeFragment);
                }
            }
        });

        return rootView;
    }

    // Method to get the authBool value
    public boolean getAuthBool() {
        return authBool;
    }
}