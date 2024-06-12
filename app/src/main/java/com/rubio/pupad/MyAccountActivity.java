package com.rubio.pupad;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class MyAccountActivity extends AppCompatActivity {

    TextView nameTextView;
    EditText nameEditText, newPasswordEditText, confirmPasswordEditText;
    Button updateNameButton, updatePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_account);

        // Initialize views
        nameTextView = findViewById(R.id.name_text_view);
        nameEditText = findViewById(R.id.name_edit_text);
        updateNameButton = findViewById(R.id.update_name_button);
        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        updatePasswordButton = findViewById(R.id.update_password_button);

        // Fetch and display user's name
        fetchAndDisplayUserName();

        updateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserName();
            }
        });

        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserPassword();
            }
        });
    }

    private void fetchAndDisplayUserName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userName = user.getDisplayName();
            if (userName != null && !userName.isEmpty()) {
                nameTextView.setText(userName);
            } else {
                nameTextView.setText("Name not available");
            }
        } else {
            // User is not signed in, handle this case if needed
            nameTextView.setText("User not signed in");
        }
    }

    private void updateUserName() {
        String newName = nameEditText.getText().toString();
        if (newName.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    nameTextView.setText(newName);
                    nameEditText.setText("");
                    nameEditText.setHint("Enter new name");
                    Toast.makeText(MyAccountActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyAccountActivity.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MyAccountActivity.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserPassword() {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("Password cannot be empty");
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Confirm password cannot be empty");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters long");
            return;
        }

        if (!isValidPassword(newPassword)) {
            newPasswordEditText.setError("Password must contain uppercase, lowercase, number, and special character");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    newPasswordEditText.setText("");
                    confirmPasswordEditText.setText("");
                    newPasswordEditText.setHint("Enter new password");
                    confirmPasswordEditText.setHint("Confirm new password");
                    Toast.makeText(MyAccountActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyAccountActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MyAccountActivity.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[~!@#$%^&*()_+=|<>?{}\\[\\]~-].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}
