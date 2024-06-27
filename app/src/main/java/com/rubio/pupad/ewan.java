package com.rubio.pupad;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class ewan extends AppCompatActivity {

    FloatingActionButton addNoteBtn;
    RecyclerView recyclerView;
    ImageButton menuBtn;
    NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set layout for this activity

        // Initialize UI elements
        addNoteBtn = findViewById(R.id.add_note_btn);
        recyclerView = findViewById(R.id.recycler_view);
        menuBtn = findViewById(R.id.menu_btn);

        // Set click listeners for buttons
        addNoteBtn.setOnClickListener((v) -> startActivity(new Intent(ewan.this, NoteDetailsActivity.class)));
        menuBtn.setOnClickListener((v) -> showMenu());

        // Setup RecyclerView
        setupRecyclerView();
    }

    // Method to show popup menu for options like My Account, Chatbot, and Logout
    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(ewan.this, menuBtn);
        popupMenu.getMenu().add("My Account");
        popupMenu.getMenu().add("Chatbot");
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getTitle().toString()) {
                    case "Logout":
                        // Logout user and navigate to LoginActivity
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(ewan.this, LoginActivity.class));
                        finish(); // Finish current activity
                        return true;
                    case "Chatbot":
                        // Navigate to Chatbot activity
                        startActivity(new Intent(ewan.this, openaiActivity.class));
                        return true;
                    case "My Account":
                        // Navigate to My Account settings
                        startActivity(new Intent(ewan.this, profilesettings.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    // Method to setup RecyclerView for displaying notes
    void setupRecyclerView() {
        // Query Firestore for notes ordered by timestamp descending
        Query query = Utility.getCollectionReferenceForNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(options, this);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening(); // Start listening for Firestore updates
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening(); // Stop listening for Firestore updates
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged(); // Notify adapter of data changes
    }
}
