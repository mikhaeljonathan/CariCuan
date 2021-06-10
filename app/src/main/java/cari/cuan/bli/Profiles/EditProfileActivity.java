package cari.cuan.bli.Profiles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import cari.cuan.bli.MainActivity;
import cari.cuan.bli.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class EditProfileActivity extends AppCompatActivity {

    // The UI attributes
    private ImageButton mImage;
    private EditText mName;
    private ImageButton mQr;
    private Button mButton;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private StorageReference mStorageProfile;
    private StorageReference mStorageQr;
    private DatabaseReference mDatabaseUser;

    // The variables used in this class
    private Uri mImageUri = null;
    private Uri mQrUri = null;

    private String newName;

    // Progress Dialog
    private ProgressDialog mProgress;
    private AlertDialog.Builder builder;

    // For final attributes (you can choose the value arbitrarily)
    private static final int QR_REQUEST = 2;

    // Other final attribute
    private final String DEFAULT_PIC_URL = "https://firebasestorage.googleapis.com/v0/b/cari-cuan-df730.appspot.com/o/Profile_images%2Fdefaultpic.png?alt=media&token=a629ae2b-96ef-4d40-bd07-3bd7ee9449bb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Make the XML components to java objects
        mImage = findViewById(R.id.edit_profile_image);
        mName = findViewById(R.id.edit_profile_name);
        mQr = findViewById(R.id.edit_profile_qr);
        mButton = findViewById(R.id.edit_profile_button);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mStorageProfile = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mStorageQr = FirebaseStorage.getInstance().getReference().child("Qr");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

        // Set the dialogs
        mProgress = new ProgressDialog(EditProfileActivity.this);
        builder = new AlertDialog.Builder(EditProfileActivity.this);

        // Set up the buttons
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //To make the image 1:1 (square)
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(EditProfileActivity.this);

            }
        });

        mQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Open the gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, QR_REQUEST);

            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prepareEdit();

            }
        });

        retrieveDataAndSetupTheViews();

    }

    private void retrieveDataAndSetupTheViews() {

        mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve
                String image = dataSnapshot.child("image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String qr = dataSnapshot.child("qr").getValue().toString();

                // Set up the views
                Glide.with(EditProfileActivity.this).load(image)
                        .apply(RequestOptions.circleCropTransform()).into(mImage); // Make the image round
                mName.setText(name);
                Picasso.get().load(Uri.parse(qr)).into(mQr);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void prepareEdit() {

        // Get the text of name from EditText
        newName = mName.getText().toString().trim();

        if (TextUtils.isEmpty(newName)){

            // Set the error message
            mName.requestFocus();
            mName.setError("Empty title");

        } else {

            confirmEdit();

        }

    }

    private void confirmEdit() {

        // Make the alert dialog
        builder.setTitle("Confirm Edit")
                .setMessage("Are you sure to edit your profile?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startEditing();

                    }
                });
        builder.show();
    }

    private void startEditing() {

        // Show the progress dialog
        mProgress.setMessage("Editing profile...");
        mProgress.show();

        if (mImageUri != null){ // User changes their profile photo

            if (mQrUri != null){ // User changes their profile photo and changes their qr

                // Make the space in the Firebase storage
                final StorageReference imageFilePath = mStorageProfile.child(mImageUri.getLastPathSegment());
                // Upload the image by the Uri
                imageFilePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Get the Uri
                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {

                                deleteReplacedImageInStorageAndFinish(uri);

                            }
                        });
                    }
                });

            } else { // User changes their profile photo but doesn't change their qr

                // Make the space in the Firebase storage
                final StorageReference imageFilePath = mStorageProfile.child(mImageUri.getLastPathSegment());
                // Upload the image by the Uri
                imageFilePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        // Get the Uri
                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                deleteReplacedImageInStorageAndFinish(uri);

                            }
                        });
                    }
                });

            }


        } else { // User doesn't change their profile photo

            if (mQrUri != null){ // User doesn't change their profile photo but changes their qr

                setUpQr(); // Set the qr only (without image)

            } else { // User doesn't change

                setUpNameAndFinish(); // Skip set up the image and set up the qr

            }

        }

    }

    private void deleteReplacedImageInStorageAndFinish(final Uri uri) {

        // Delete replaced image
        mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Current user doesn't use the defaultpic.png
                if (!dataSnapshot.child("image").getValue().toString().equals(DEFAULT_PIC_URL)) {

                    // Go to the storage where replaced image was stored and delete it
                    FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("image").getValue().toString())
                            .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Change it to the database
                            mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("image").setValue(uri.toString());

                            if (mQrUri != null) {

                                setUpQr(); // Set the qr

                            } else {

                                setUpNameAndFinish(); // Skip setup the qr

                            }

                        }
                    });

                } else { // The current user use the defaultpic.png, don't delete it

                    // Change it to the database
                    mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("image").setValue(uri.toString());

                    if (mQrUri != null) {

                        setUpQr(); // Set the qr

                    } else {

                        setUpNameAndFinish(); // Skip setup the qr

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setUpQr() {

        // Make the space in the Firebase storage
        final StorageReference qrFilePath = mStorageQr.child(mQrUri.getLastPathSegment());
        // Upload the image by the Uri
        qrFilePath.putFile(mQrUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // Get the Uri
                qrFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        deleteReplacedQrInStorageAndFinish(uri);

                    }
                });
            }
        });

    }

    private void deleteReplacedQrInStorageAndFinish(final Uri uri) {

        mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Go to the storage where replaced image was stored and delete it
                FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("qr").getValue().toString())
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Change it to the database
                        mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("qr").setValue(uri.toString());

                        setUpNameAndFinish(); // Skip setup the qr

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setUpNameAndFinish() {

        mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("name").setValue(newName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // Go to MainActivity and MoreFragment
                mProgress.dismiss();
                finish();
                Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
                mainIntent.putExtra("fragment", MainActivity.MORE);
                startActivity(mainIntent);

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
                        .apply(RequestOptions.circleCropTransform()).into(mImage); // Make the image round

            }

        }

        if (requestCode == QR_REQUEST && resultCode == RESULT_OK){ // This is called from mSetupQrBtn

            mQrUri = data.getData(); // Get the Uri
            mQr.setImageURI(mQrUri);

        }
    }

    @Override
    public void onBackPressed() {

        // Go to MainActivity and MoreFragment
        finish();
        Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
        mainIntent.putExtra("fragment", MainActivity.MORE);
        startActivity(mainIntent);

    }
}
