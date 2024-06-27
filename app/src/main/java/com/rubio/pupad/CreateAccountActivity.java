package com.rubio.pupad;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {

    // UI elements
    EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText;
    Button createAccountBtn;
    ProgressBar progressBar;
    TextView loginBtnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display (if supported)
        setContentView(R.layout.activity_create_account); // Set layout for this activity

        // Initialize UI elements
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        createAccountBtn = findViewById(R.id.create_account_btn);
        progressBar = findViewById(R.id.progrss_bar);
        loginBtnTextView = findViewById(R.id.login_text_view_btn);
        nameEditText = findViewById(R.id.name_edit_text);

        // Set click listeners for buttons
        createAccountBtn.setOnClickListener(v -> createAccount());
        loginBtnTextView.setOnClickListener(v -> finish()); // Finish the activity (back to login)
    }

    // Method to initiate account creation process
    void createAccount() {
        // Retrieve input data
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Validate user input
        boolean isValidated = validateData(name, email, password, confirmPassword);
        if (!isValidated) {
            return; // Exit method if input is invalid
        }

        // Proceed to create account in Firebase
        createAccountInFirebase(name, email, password);
    }

    // Method to create account in Firebase Authentication
    void createAccountInFirebase(String name, String email, String password) {
        // Show progress bar and hide create account button
        changeInProgress(true);

        // Initialize Firebase Authentication instance
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        // Attempt to create user with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CreateAccountActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Hide progress bar
                        changeInProgress(false);
                        // If account creation is successful
                        if (task.isSuccessful()) {
                            // Get current user and update display name
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates);

                            // Show success message and send email verification
                            Utility.showToast(CreateAccountActivity.this, "Successfully create account. Check your email to verify");
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            firebaseAuth.signOut(); // Sign out user after account creation
                            finish(); // Finish activity and return to previous screen
                        } else {
                            // Show error message if account creation fails
                            Utility.showToast(CreateAccountActivity.this, task.getException().getLocalizedMessage());
                        }
                    }
                });

    }

    // Method to toggle progress UI elements
    void changeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar
            createAccountBtn.setVisibility(View.GONE); // Hide create account button
        } else {
            progressBar.setVisibility(View.GONE); // Hide progress bar
            createAccountBtn.setVisibility(View.VISIBLE); // Show create account button
        }
    }

    // Method to validate user input data
    boolean validateData(String name, String email, String password, String confirmPassword) {
        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is invalid");
            return false;
        }
        // Validate password length
        if (password.length() < 6) {
            passwordEditText.setError("Password must consist of 6 characters above");
            return false;
        }
        // Validate password complexity
        if (!isValidPassword(password)) {
            passwordEditText.setError("Password must contain uppercase, lowercase, number, and special character");
            return false;
        }
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Password not matched");
            return false;
        }
        return true; // Return true if all validations pass
    }

    // Method to check if password meets complexity requirements
    private boolean isValidPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[~!@#$%^&*()_+=|<>?{}\\[\\]~-].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}
