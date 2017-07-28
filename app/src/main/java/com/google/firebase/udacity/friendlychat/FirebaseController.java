package com.google.firebase.udacity.friendlychat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.udacity.friendlychat.MainActivity.DEFAULT_MSG_LENGTH_LIMIT;
import static com.google.firebase.udacity.friendlychat.MainActivity.FRIENDLY_MSG_LENGTH_KEY;

/**
 * Developed by : kawnayeen
 * Creation Date : 7/27/17
 */
public class FirebaseController {
    private static final String MESSAGES = "messages";
    private FirebaseRemoteConfig firebaseRemoteConfig;


    private FriendlyChatView view;

    private DatabaseReference msgDatabaseReference;
    private MessageEventListener msgEventListener;

    public FirebaseController(FriendlyChatView view) {
        this.view = view;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
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

    public void fetchConfig() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        firebaseRemoteConfig.setDefaults(defaultConfigMap);
        long cacheExpiration = 10;
        firebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(ignored -> {
            firebaseRemoteConfig.activateFetched();
            view.applyRetrievedLengthLimit((int) firebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY));
        });
    }
}
