package com.google.firebase.udacity.friendlychat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.kawnayeen.compressor.Compressor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements FriendlyChatView {

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int RC_SIGN_IN = 123;
    private static final int RC_PHOTO_PICKER = 2;
    private static final int REQUEST_READ_STORAGE = 3;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    private MessageAdapter mMessageAdapter;
    @BindView(R.id.messageListView)
    ListView mMessageListView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.photoPickerButton)
    ImageButton mPhotoPickerButton;
    @BindView(R.id.messageEditText)
    EditText mMessageEditText;
    @BindView(R.id.sendButton)
    Button mSendButton;

    private String mUsername;
    private FirebaseController firebaseController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsername = ANONYMOUS;
        ButterKnife.bind(this);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                startPhotoPicker();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(view -> {
            FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
            firebaseController.insertMessage(friendlyMessage);
            mMessageEditText.setText("");
        });

        firebaseController = new FirebaseController(this, this);
        firebaseController.fetchConfig();
    }

    private void startPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_PHOTO_PICKER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startPhotoPicker();
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void onSingedInInitialize(String displayName) {
        mUsername = displayName;
        attachDatabaseListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    private void attachDatabaseListener() {
        firebaseController.singedIn();
    }

    private void detachDatabaseReadListener() {
        firebaseController.signedOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseController.attachAuthListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseController.removeAuthListener();
        detachDatabaseReadListener();
        mMessageAdapter.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign In canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                String absolutePath = IntentUtil.getAbsolutePathFromUri(data.getData(), this);
                File compressedFile;
                try {
                    compressedFile = new Compressor(this).setMaxWidth(512).setMaxHeight(512).setQuality(75).compressToFile(new File(absolutePath));
                } catch (IOException e) {
                    compressedFile = new File(absolutePath);
                }
                firebaseController.uploadPhoto(compressedFile);
            }
        }
    }

    public void applyRetrievedLengthLimit(int messageLength) {
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(messageLength)});
    }

    @Override
    public void imageUploaded(String imageUrl) {
        firebaseController.insertMessage(new FriendlyMessage(null, mUsername, imageUrl));
    }

    @Override
    public void userSignedIn(String displayName) {
        onSingedInInitialize(displayName);
    }

    @Override
    public void promptSignIn() {
        onSignedOutCleanup();
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void addMessage(FriendlyMessage friendlyMessage) {
        mMessageAdapter.add(friendlyMessage);
    }
}
