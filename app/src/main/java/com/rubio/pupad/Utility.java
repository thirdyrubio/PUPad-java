package com.rubio.pupad;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;

public class Utility {

    /**
     * Displays a short toast message.
     * @param context The context from which the toast should be displayed.
     * @param message The message to display in the toast.
     */
    static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Retrieves the Firestore collection reference for notes belonging to the current user.
     * @return CollectionReference pointing to the "my_notes" collection under the current user's UID in Firestore.
     */
    static CollectionReference getCollectionReferenceForNotes(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance().collection("notes")
                .document(currentUser.getUid()).collection("my_notes");
    }

    /**
     * Converts a Firestore Timestamp object to a formatted string.
     * @param timestamp The Timestamp object to convert.
     * @return A string representing the date formatted as "MM/dd/yyyy".
     */
    static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate());
    }
}
