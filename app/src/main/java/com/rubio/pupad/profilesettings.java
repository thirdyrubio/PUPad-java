package com.rubio.pupad;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class profilesettings extends AppCompatActivity {

    private StorageReference storageReference;

    private Uri imageUri;
    private ImageView imageView;
    private MaterialButton uploadImage;
    private TextView nameTextView;
    private ImageButton selectImage;

    EditText nameEditText, newPasswordEditText, confirmPasswordEditText;
    Button updateNameButton, updatePasswordButton;

    // Activity result launcher for picking an image from gallery
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            if (imageUri != null) {
                                uploadImage.setEnabled(true);
                                // Load selected image into ImageView using Glide
                                Glide.with(profilesettings.this).load(imageUri).into(imageView);
                            }
                        }
                    } else {
                        Toast.makeText(profilesettings.this, "Please select an image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilesettings);

        // Firebase storage reference for uploading images
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize views
        imageView = findViewById(R.id.imageView);
        selectImage = findViewById(R.id.selectImage);
        uploadImage = findViewById(R.id.uploadImage);
        nameTextView = findViewById(R.id.name_text_view);
        nameEditText = findViewById(R.id.name_edit_text);
        updateNameButton = findViewById(R.id.update_name_button);
        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        updatePasswordButton = findViewById(R.id.update_password_button);

        // Fetch and display the current user's name
        fetchAndDisplayUserName();

        // Select image button click listener
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch gallery to select an image
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        // Upload image button click listener
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    // Upload selected image to Firebase storage
                    uploadImageToFirebase(imageUri);
                } else {
                    Toast.makeText(profilesettings.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Update name button click listener
        updateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update user display name
                updateUserName();
            }
        });

        // Update password button click listener
        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update user password
                updateUserPassword();
            }
        });

        // Load and display user profile picture
        loadUserProfilePicture();
    }

    // Upload image to Firebase storage
    private void uploadImageToFirebase(Uri imageUri) {
        // Reference to Firebase storage location for profile images
        StorageReference imageRef = storageReference.child("profile_images/" + UUID.randomUUID().toString());
        // Upload image to Firebase storage
        imageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // On successful upload, get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Update user profile with the new image URL
                                updateUserProfile(uri);
                            }
                        });
                        Toast.makeText(profilesettings.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle upload failure
                        Toast.makeText(profilesettings.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Update user profile with new image URL
    private void updateUserProfile(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Build profile update request with new photo URL
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(imageUri)
                    .build();

            // Update user profile
            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // On success, refresh the displayed profile picture
                            Toast.makeText(profilesettings.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                            loadUserProfilePicture(); // Refresh the profile picture after update
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle profile update failure
                            Toast.makeText(profilesettings.this, "Failed to update profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Fetch and display current user's name
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
            nameTextView.setText("User not signed in");
        }
    }

    // Load user profile picture into ImageView
    private void loadUserProfilePicture() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getPhotoUrl() != null) {
            // Load profile picture using Glide library
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(imageView); // Load the profile picture into imageView
        } else {
            // If no profile picture is available, set a placeholder image
            imageView.setImageResource(R.drawable.baseline_person_24);
        }
    }

    // Update user display name
    private void updateUserName() {
        String newName = nameEditText.getText().toString();
        if (newName.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Build profile update request with new display name
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            // Update user profile
            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // On success, update displayed name and clear input
                    nameTextView.setText(newName);
                    nameEditText.setText("");
                    nameEditText.setHint("Enter new name");
                    Toast.makeText(profilesettings.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle update failure
                    Toast.makeText(profilesettings.this, "Failed to update name", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(profilesettings.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    // Update user password
    private void updateUserPassword() {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Validate new password and confirmation
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
            // Update user password
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // On success, clear input fields
                    newPasswordEditText.setText("");
                    confirmPasswordEditText.setText("");
                    newPasswordEditText.setHint("Enter new password");
                    confirmPasswordEditText.setHint("Confirm new password");
                    Toast.makeText(profilesettings.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle update failure
                    Toast.makeText(profilesettings.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(profilesettings.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    // Validate password strength
    private boolean isValidPassword(String password) {
        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[~!@#$%^&*()_+=|<>?{}\\[\\]~-].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}
