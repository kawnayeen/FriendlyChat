package com.google.firebase.udacity.friendlychat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Developed by : kawnayeen
 * Creation Date : 7/27/17
 */
public class FirebaseController {
    private static final String MESSAGES = "messages";
    private FirebaseDatabase firebaseDatabase;


    private FriendlyChatView view;

    private DatabaseReference msgDatabaseReference;
    private MessageEventListener msgEventListener;

    public FirebaseController(FriendlyChatView view) {
        this.view = view;
        firebaseDatabase = FirebaseDatabase.getInstance();
        msgDatabaseReference = firebaseDatabase.getReference().child(MESSAGES);
        msgEventListener = null;
    }

    public void insertMessage(FriendlyMessage friendlyMessage) {
        msgDatabaseReference.push().setValue(friendlyMessage);
    }

    public void singedIn() {
        if (msgEventListener == null) {
            msgEventListener = (snapShot, value) -> view.addMessage(snapShot.getValue(FriendlyMessage.class));
            msgDatabaseReference.addChildEventListener(msgEventListener);
        }
    }

    public void signedOut() {
        if (msgEventListener != null) {
            msgDatabaseReference.removeEventListener(msgEventListener);
            msgEventListener = null;
        }
    }
}
