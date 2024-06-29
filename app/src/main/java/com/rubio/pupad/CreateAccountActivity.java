package com.rubio.pupad;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class CreateAccountActivity extends AppCompatActivity {

    // UI elements
    private EditText emailEditText, passwordEditText, confirmPasswordEditText, nameEditText;
    private Button createAccountBtn;
    private ProgressBar progressBar;
    private TextView loginBtnTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

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
    private void createAccount() {
        // Retrieve input data
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validate user input
        boolean isValidated = validateData(name, email, password, confirmPassword);
        if (!isValidated) {
            return; // Exit method if input is invalid
        }

        // Proceed to create account in Firebase
        createAccountInFirebase(name, email, password);
    }

    // Method to create account in Firebase Authentication
    private void createAccountInFirebase(String name, String email, String password) {
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
                        if (task.isSuccessful()) {
                            // Get current user and update display name
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Show success message and send email verification
                                            Toast.makeText(CreateAccountActivity.this, "Successfully created account. Check your email to verify.", Toast.LENGTH_LONG).show();
                                            firebaseAuth.getCurrentUser().sendEmailVerification();
                                            firebaseAuth.signOut(); // Sign out user after account creation
                                            finish(); // Finish activity and return to previous screen
                                        }
                                    }
                                });
                            }
                        } else {
                            // Show error message if account creation fails
                            Toast.makeText(CreateAccountActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    // Method to toggle progress UI elements
    private void changeInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar
            createAccountBtn.setVisibility(View.GONE); // Hide create account button
        } else {
            progressBar.setVisibility(View.GONE); // Hide progress bar
            createAccountBtn.setVisibility(View.VISIBLE); // Show create account button
        }
    }

    // Method to validate user input data
    private boolean validateData(String name, String email, String password, String confirmPassword) {
        // Validate name
        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be blank");
            return false;
        }
        if (!isValidName(name)) {
            nameEditText.setError("Name is invalid. It must not contain numbers, special characters, or emojis.");
            return false;
        }
        // Validate email
        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be blank");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is invalid");
            return false;
        }
        // Validate password
        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be blank");
            return false;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Password must consist of 6 characters above");
            return false;
        }
        if (!isValidPassword(password)) {
            passwordEditText.setError("Password must contain uppercase, lowercase, number, and special character");
            return false;
        }
        // Validate password confirmation
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return false;
        }
        return true; // Return true if all validations pass
    }

    // Method to check if the name meets requirements
    private boolean isValidName(String name) {
        String regex = "^[\\p{L} .'-]+$";
        return Pattern.matches(regex, name);
    }

    // Method to check if password meets complexity requirements
    private boolean isValidPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}
