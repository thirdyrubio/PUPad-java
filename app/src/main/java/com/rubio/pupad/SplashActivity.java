package com.rubio.pupad;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for immersive experience
        EdgeToEdge.enable(this);

        // Set layout for splash screen
        setContentView(R.layout.activity_splash);

        // Delayed handler to start next activity after a short delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if user is authenticated
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser == null){
                    // If user is not logged in, navigate to LoginActivity
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                } else {
                    // If user is logged in, navigate to main activity (replace with your main activity class)
                    startActivity(new Intent(SplashActivity.this, ewan.class));
                }
                // Finish splash activity to prevent returning to it with back button
                finish();
            }
        }, 1000); // Delay in milliseconds (1 second)
    }
}
