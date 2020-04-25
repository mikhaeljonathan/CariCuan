package com.example.caricuan.Catalogs;

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
import android.widget.TextView;

import com.example.caricuan.MainActivity;
import com.example.caricuan.R;
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

public class SellActivity extends AppCompatActivity {

    // The UI attributes
    private ImageButton mSelectImage;
    private TextView mError;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private EditText mPostStock;
    private EditText mPostPrice;
    private Button mUploadBtn;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseCountCatalog;
    private DatabaseReference mDatabaseCatalog;
    private DatabaseReference mDatabaseUser;
    private StorageReference mStorage;

    // Dialogs
    private ProgressDialog mProgress;
    private AlertDialog.Builder builder;

    // The variables used in this class
    private Uri mImageUri = null;

    private String title;
    private String desc;
    private String stockString;
    private String priceString;
    private int stock;
    private double price;

    // For final attributes (you can choose the value arbitrarily)
    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        // Make the XML components to java objects
        mSelectImage = findViewById(R.id.edit_image_btn);
        mError = findViewById(R.id.edit_error);
        mError.setVisibility(View.GONE);
        mPostTitle = findViewById(R.id.edit_title);
        mPostDesc = findViewById(R.id.edit_desc);
        mPostStock = findViewById(R.id.edit_stock);
        mPostPrice = findViewById(R.id.edit_price);
        mUploadBtn = findViewById(R.id.edit_btn);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseCountCatalog = FirebaseDatabase.getInstance().getReference().child("CountCatalog");
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mStorage = FirebaseStorage.getInstance().getReference().child("Catalog_images");

        // Set the dialogs
        mProgress = new ProgressDialog(this);
        builder = new AlertDialog.Builder(SellActivity.this);

        // Set up the buttons
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Open the gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prepareUploading();

            }
        });

    }

    private void prepareUploading() {

        // Get the text of title, desc, stockString, and priceString from EditText
        title = mPostTitle.getText().toString().trim();
        desc = mPostDesc.getText().toString().trim();
        stockString = mPostStock.getText().toString();
        priceString = mPostPrice.getText().toString();

        if (TextUtils.isEmpty(title)){

            // Set the error message
            mPostTitle.requestFocus();
            mPostTitle.setError("Empty title");

        } else if (TextUtils.isEmpty(desc)){

            // Set the error message
            mPostDesc.requestFocus();
            mPostDesc.setError("Empty description");

        } else if (TextUtils.isEmpty(stockString)){

            // Set the error message
            mPostStock.requestFocus();
            mPostStock.setError("Empty stock");

        } else if (TextUtils.isEmpty(priceString)){

            // Set the error message
            mPostPrice.requestFocus();
            mPostPrice.setError("Empty price");

        } else if (mImageUri == null){

            // Set the error message
            mError.setVisibility(View.VISIBLE);

        } else {

            // Parse the stockString and priceString to actual int and double data type
            stock = Integer.parseInt(stockString);
            price = Double.parseDouble(priceString);

            // The constraint
            if (stock <= 0 || stock > 1000){

                // Set the error message
                mPostStock.requestFocus();
                mPostStock.setError("Stock must be in between 1 to 1000");

            } else if (price <= 0 || price > 1000000){

                // Set the error message
                mPostPrice.requestFocus();
                mPostPrice.setError("Price must be in between 1 to 1000000");

            } else {

                confirmSelling();

            }

        }

    }

    private void confirmSelling() {

        // Make the alert dialog
        builder.setTitle("Confirm Sell")
                .setMessage("Are you sure to sell this catalog?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startUploading();

                    }
                });
        builder.show();
    }

    private void startUploading(){

        // Show the progress dialog
        mProgress.setMessage("Uploading catalog...");
        mProgress.show();

        // Make the space in the Firebase storage
        final StorageReference filePath = mStorage.child(mImageUri.getLastPathSegment());
        // Upload the image by the Uri
        filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // Get the Uri
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        final DatabaseReference newPost = mDatabaseCatalog.push(); // Make new catalog database

                        newPost.child("images").setValue(uri.toString());

                        setUpDatabaseCatalog(newPost);

                    }
                });
            }
        });
    }

    private void setUpDatabaseCatalog(final DatabaseReference newPost) {
        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Add them to database
                newPost.child("title").setValue(title);
                newPost.child("desc").setValue(desc);
                newPost.child("user").setValue(mAuth.getCurrentUser().getUid());
                newPost.child("classes").setValue(dataSnapshot.child("classes").getValue().toString());
                newPost.child("stocks").setValue(stock);
                newPost.child("price").setValue(Double.parseDouble(priceString))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // The last added catalog on the top of feeds
                        makeDescendingPost(newPost);

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeDescendingPost(final DatabaseReference newPost) {

        mDatabaseCountCatalog.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int order = Integer.parseInt(dataSnapshot.getValue().toString());
                newPost.child("order").setValue(order); // Set the order

                // Decrement it because the query sort ascending
                mDatabaseCountCatalog.setValue(order - 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Go to MainActivity and FeedsFragment
                        mProgress.dismiss();
                        finish();
                        Intent mainIntent = new Intent(SellActivity.this, MainActivity.class);
                        mainIntent.putExtra("fragment", MainActivity.FEEDS);
                        startActivity(mainIntent);

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData(); // Get the Uri
            mSelectImage.setImageURI(mImageUri);

        }
    }

    @Override
    public void onBackPressed() {

        // Go to MainActivity and FeedsFragment
        finish();
        Intent mainIntent = new Intent(SellActivity.this, MainActivity.class);
        mainIntent.putExtra("fragment", MainActivity.FEEDS);
        startActivity(mainIntent);

    }
}