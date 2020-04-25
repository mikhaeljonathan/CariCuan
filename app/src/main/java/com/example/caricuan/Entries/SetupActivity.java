package com.example.caricuan.Entries;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.caricuan.MainActivity;
import com.example.caricuan.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    // The UI attributes
    private ImageButton mSetupImageBtn;
    private EditText mNameField;
    private Spinner mSpinner;
    private ImageButton mSetupQrbtn;
    private TextView mErrorText;
    private Button mSetupBtn;

    // The variables used in this class
    private String classes;
    private Uri mImageUri = null;
    private Uri mQrUri = null;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageUserImage;
    private StorageReference mStorageQr;

    // Progress Dialog
    private ProgressDialog mProgress;

    // For final attributes (you can choose the value arbitrarily)
    private static final int QR_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Make the XML components to java objects
        mSetupImageBtn = findViewById(R.id.edit_profile_image);
        mNameField = findViewById(R.id.edit_profile_name);
        mSpinner = findViewById(R.id.setup_class_spinner);
        mSetupQrbtn = findViewById(R.id.edit_profile_qr);
        mErrorText = findViewById(R.id.setup_error_text);
        mErrorText.setVisibility(View.GONE);
        mSetupBtn = findViewById(R.id.edit_profile_button);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageUserImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mStorageQr = FirebaseStorage.getInstance().getReference().child("Qr");

        // Set the progress dialog
        mProgress = new ProgressDialog(this);

        //Set up the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.classes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);

        //Set up the buttons
        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //To make the image 1:1 (square)
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SetupActivity.this);

            }
        });

        mSetupQrbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Open the gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, QR_REQUEST);

            }
        });

        mSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });

    }

    private void startSetupAccount() {
        // Get the text of name from EditText
        final String name = mNameField.getText().toString().trim();

        //Get the Uid of current user
        final String userId = mAuth.getCurrentUser().getUid();

        if (TextUtils.isEmpty(name)){

            // Set the error message
            mNameField.requestFocus();
            mNameField.setError("Empty name");

        } else if (mQrUri == null){

            // Set the error message
            mErrorText.setVisibility(View.VISIBLE);

        } else {

            // Show the progress dialog
            mProgress.setMessage("Finishing setup ...");
            mProgress.show();

            if (mImageUri == null){ // User doesn't set up the profile image

                // Set the user's profile image to defaultpic from Firebase storage
                mStorageUserImage.child("defaultpic.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // Add them to the database
                        mDatabaseUsers.child(userId).child("image").setValue(uri.toString());
                        mDatabaseUsers.child(userId).child("name").setValue(name);
                        mDatabaseUsers.child(userId).child("classes").setValue(classes);
                        setUpQr(userId);

                    }
                });

            } else { // User sets up the profile image

                // Make the space in the Firebase storage
                final StorageReference filepath = mStorageUserImage.child(mImageUri.getLastPathSegment());
                // Upload the image by the Uri
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //Get the Uri
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                // Add them to the database
                                mDatabaseUsers.child(userId).child("image").setValue(uri.toString());
                                mDatabaseUsers.child(userId).child("name").setValue(name);
                                mDatabaseUsers.child(userId).child("classes").setValue(classes);
                                setUpQr(userId);

                            }
                        });

                    }
                });
            }
        }
    }

        private void setUpQr(final String userId) {

        // Make the space in the Firebase storage
        final StorageReference filepath2 = mStorageQr.child(mQrUri.getLastPathSegment());
        // Upload the image by the Uri
        filepath2.putFile(mQrUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //Get the Uri
                filepath2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // Add it to the database
                        mDatabaseUsers.child(userId).child("qr").setValue(uri.toString())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                // Go to MainActivity
                                finish();
                                startActivity(new Intent(SetupActivity.this, MainActivity.class));

                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) { // This is called from mSetupImageBtn

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mImageUri = result.getUri(); // Get the Uri
                Glide.with(this).load(mImageUri)
                        .apply(RequestOptions.circleCropTransform()).into(mSetupImageBtn); // Make the image round

            }

        }

        if (requestCode == QR_REQUEST && resultCode == RESULT_OK){ // This is called from mSetupQrBtn

            mQrUri = data.getData(); // Get the Uri
            mSetupQrbtn.setImageURI(mQrUri);

        }
    }

    // For the adapter for spinner
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        classes = adapterView.getItemAtPosition(i).toString();

    }

    // It's the overrided method from the AdapterView.OnItemSelectedListener interface (for spinner)
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    // To prevent going back to MainActivity
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
