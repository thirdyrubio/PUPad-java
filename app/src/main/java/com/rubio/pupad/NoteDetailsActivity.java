package com.rubio.pupad;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class NoteDetailsActivity extends AppCompatActivity {

    EditText titleEditText, contentEditText;
    ImageButton saveNoteBtn, deleteNoteBtn;
    TextView pageTitleTextView;
    String title, content, docId;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_details);

        // Initialize UI elements
        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteBtn = findViewById(R.id.delete_note_text_view_btn);

        // Retrieve intent extras
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        // Determine if in edit mode
        if (docId != null && !docId.isEmpty()) {
            isEditMode = true;
        }

        // Populate UI with note data
        titleEditText.setText(title);
        contentEditText.setText(content);

        // Update UI based on edit mode
        if (isEditMode) {
            pageTitleTextView.setText("Edit your note");
            deleteNoteBtn.setVisibility(View.VISIBLE);
        } else {
            pageTitleTextView.setText("Add a new note");
            deleteNoteBtn.setVisibility(View.GONE);
        }

        // Save button click listener
        saveNoteBtn.setOnClickListener((v) -> saveNote());

        // Delete button click listener
        deleteNoteBtn.setOnClickListener((v) -> deleteNoteFromFirebase());
    }

    // Method to save or update a note in Firestore
    void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();

        // Validate input
        if (noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        // Create a new Note object
        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());

        // Save the note to Firestore
        saveNoteToFirebase(note);
    }

    // Method to save a note to Firestore
    void saveNoteToFirebase(Note note) {
        DocumentReference documentReference;
        if (isEditMode) {
            // Update existing document if in edit mode
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        } else {
            // Create a new document if not in edit mode
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }

        // Set the note object to Firestore document
        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, "Saved");
                    finish(); // Finish activity after successful save
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed to save note");
                }
            }
        });
    }

    // Method to delete a note from Firestore
    void deleteNoteFromFirebase() {
        // Get the reference to the document
        DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document(docId);

        // Delete the document from Firestore
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, "Deleted");
                    finish(); // Finish activity after successful delete
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed to delete note");
                }
            }
        });
    }
}
