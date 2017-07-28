package com.google.firebase.udacity.friendlychat;

import android.app.Activity;
import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private static final String CHAT_PHOTOS = "chat_photos";
    private FirebaseRemoteConfig firebaseRemoteConfig;
    private FriendlyChatView view;
    private DatabaseReference msgDatabaseReference;
    private StorageReference chatPhotoReference;
    private MessageEventListener msgEventListener;
    private FirebaseAuth firebaseAuth;
    private AuthStateListener authStateListener;
    private Activity activity;

    public FirebaseController(FriendlyChatView view, Activity activity) {
        this.view = view;
        this.activity = activity;
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        msgDatabaseReference = FirebaseDatabase.getInstance().getReference().child(MESSAGES);
        chatPhotoReference = FirebaseStorage.getInstance().getReference().child(CHAT_PHOTOS);
        msgEventListener = null;
        authStateListener = null;
    }

    public void insertMessage(FriendlyMessage friendlyMessage) {
        msgDatabaseReference.push().setValue(friendlyMessage);
    }

    public void uploadPhoto(Uri imageUri) {
        StorageReference photoRef = chatPhotoReference.child(imageUri.getLastPathSegment());
        photoRef.putFile(imageUri).addOnSuccessListener(activity, taskSnapshot -> view.imageUploaded(taskSnapshot.getDownloadUrl().toString()));
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

    public void attachAuthListener() {
        if (authStateListener == null) {
            authStateListener = fbAuth -> {
                if (fbAuth.getCurrentUser() != null)
                    view.userSignedIn(fbAuth.getCurrentUser().getDisplayName());
                else
                    view.promptSignIn();
            };
            firebaseAuth.addAuthStateListener(authStateListener);
        }
    }

    public void removeAuthListener() {
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
            authStateListener = null;
        }
    }
}
