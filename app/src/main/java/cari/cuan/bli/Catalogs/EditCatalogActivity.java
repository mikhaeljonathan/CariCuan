package cari.cuan.bli.Catalogs;

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
import android.widget.Toast;

import cari.cuan.bli.MainActivity;
import cari.cuan.bli.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class EditCatalogActivity extends AppCompatActivity {

    // The UI attributes
    private ImageButton mEditImage;
    private EditText mEditTitle;
    private EditText mEditDesc;
    private EditText mEditStock;
    private TextView mEditPrice;
    private Button mEditBtn;
    private Button mDeleteBtn;

    // The Firebase attributes
    private DatabaseReference mDatabaseCurrentCatalog;
    private DatabaseReference mDatabaseTransaction;
    private StorageReference mStorage;

    // Dialogs
    private ProgressDialog mProgress;
    private AlertDialog.Builder builder;

    // The variables used in this class
    private String catalogKey = null;
    private Uri mImageUri = null;

    private String newTitle;
    private String newDesc;
    private String newStockString;

    // For final attributes (you can choose the value arbitrarily)
    private static final int GALLERY_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiry_edit_catalog);

        // Get the catalogKey from SingleCatalogActivity
        catalogKey = getIntent().getExtras().getString("catalogId");

        // Make the XML components to java objects
        mEditImage = findViewById(R.id.edit_image_btn);
        mEditTitle = findViewById(R.id.edit_title);
        mEditDesc = findViewById(R.id.edit_desc);
        mEditStock = findViewById(R.id.edit_stock);
        mEditPrice = findViewById(R.id.edit_price);
        mEditBtn = findViewById(R.id.edit_btn);
        mDeleteBtn = findViewById(R.id.edit_del_btn);

        // Set the dialogs
        mProgress = new ProgressDialog(this);
        builder = new AlertDialog.Builder(EditCatalogActivity.this);

        // Set up the firebase attributes
        mStorage = FirebaseStorage.getInstance().getReference().child("Catalog_images");
        mDatabaseCurrentCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues").child(catalogKey);
        mDatabaseTransaction = FirebaseDatabase.getInstance().getReference().child("Transactions");

        // Set up the buttons
        mEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Open the gallery
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);

            }
        });

        mEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prepareEdit();

            }
        });

        mDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkTransaction();

            }
        });

        retrieveDataAndSetupTheViews();

    }

    private void retrieveDataAndSetupTheViews() {

        // Use SingleValueEvent because current catalog's stocks could be deleted,
        // otherwise, it would be looping forever
        mDatabaseCurrentCatalog.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve
                String title = dataSnapshot.child("title").getValue().toString();
                String desc = dataSnapshot.child("desc").getValue().toString();
                String images = dataSnapshot.child("images").getValue().toString();
                int stock = Integer.parseInt(dataSnapshot.child("stocks").getValue().toString());
                double price = Double.parseDouble(dataSnapshot.child("price").getValue().toString());

                // Set up the views
                mEditTitle.setText(title);
                mEditDesc.setText(desc);
                mEditStock.setText("" + stock);
                mEditPrice.setText("" + price);
                Picasso.get().load(Uri.parse(images)).into(mEditImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

    }

    private void prepareEdit() {

        // Get the text of title, desc, and stockString from EditText
        newTitle = mEditTitle.getText().toString().trim();
        newDesc = mEditDesc.getText().toString().trim();
        newStockString = mEditStock.getText().toString();

        if (TextUtils.isEmpty(newTitle)){

            // Set the error message
            mEditTitle.requestFocus();
            mEditTitle.setError("Empty title");

        } else if (TextUtils.isEmpty(newDesc)){

            // Set the error message
            mEditDesc.requestFocus();
            mEditDesc.setError("Empty description");

        } else if (TextUtils.isEmpty(newStockString)){

            // Set the error message
            mEditStock.requestFocus();
            mEditStock.setError("Empty stock");

        } else {

            int stock = Integer.parseInt(newStockString);

            // The constraint
            if (stock <= 0 || stock > 1000) {

                // Set the error message
                mEditStock.requestFocus();
                mEditStock.setError("Stock must be in between 1 to 1000");

            } else {

                confirmEdit();

            }

        }

    }

    private void confirmEdit() {

        // Make the alert dialog
        builder.setTitle("Confirm Edit")
                .setMessage("Are you sure to edit this catalog?")
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
        mProgress.setMessage("Editing...");
        mProgress.show();

        if (mImageUri != null){ // User edits the catalog image

            // Make the space in the Firebase storage
            final StorageReference filePath = mStorage.child(mImageUri.getLastPathSegment());
            // Upload the image by the Uri
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // Get the Uri
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            deleteReplacedPictureInStorageAndFinish(uri);

                        }
                    });

                }
            });

        } else { // User doesn't edit the catalog image

            // Change them to database
            mDatabaseCurrentCatalog.child("title").setValue(newTitle);
            mDatabaseCurrentCatalog.child("desc").setValue(newDesc);
            mDatabaseCurrentCatalog.child("stocks").setValue(Integer.parseInt(newStockString))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Go to MainActivity and FeedsFragment
                            finish();
                            Intent mainIntent = new Intent(EditCatalogActivity.this, MainActivity.class);
                            mainIntent.putExtra("fragment", MainActivity.FEEDS);
                            startActivity(mainIntent);

                        }
                    });

        }

    }

    private void deleteReplacedPictureInStorageAndFinish(final Uri uri) {

        mDatabaseCurrentCatalog.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Go to the storage where replaced image was stored and delete it
                FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("images").getValue().toString()).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Change them to database
                        mDatabaseCurrentCatalog.child("title").setValue(newTitle);
                        mDatabaseCurrentCatalog.child("images").setValue(uri.toString());
                        mDatabaseCurrentCatalog.child("desc").setValue(newDesc);
                        mDatabaseCurrentCatalog.child("stocks").setValue(Integer.parseInt(newStockString))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        // Go to MainActivity and FeedsFragment
                                        mProgress.dismiss();
                                        finish();
                                        Intent mainIntent = new Intent(EditCatalogActivity.this, MainActivity.class);
                                        mainIntent.putExtra("fragment", MainActivity.FEEDS);
                                        startActivity(mainIntent);

                                    }
                                });

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkTransaction() {

        mDatabaseTransaction.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Can be deleted if there is no uncompleted transaction with the current catalog
                Boolean canBeDeleted = true;
                for (DataSnapshot data : dataSnapshot.getChildren()) { // Loop through the database

                    if (data.child("catalog_id").getValue().toString().equals(catalogKey)
                            && !Boolean.parseBoolean(data.child("confirmed").getValue().toString())){

                        // Could be deleted
                        canBeDeleted = false;
                        break;

                    }

                }

                if (canBeDeleted){

                    confirmDelete();

                } else {

                    // Set the error message
                    Toast.makeText(EditCatalogActivity.this, "There is uncompleted transaction with corresponding catalog",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void confirmDelete() {

        // Make the alert dialog
        builder.setTitle("Confirm Delete")
                .setMessage("Are you sure to delete this catalog?\n\nOnce you delete it can't be undone\n\n corresponding completed transaction with this catalog would be deleted too")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startDeleting();

                    }
                });
        builder.show();
    }

    private void startDeleting() {

        // Show the progress dialog
        mProgress.setMessage("Deleting...");
        mProgress.show();

        // Use SingleValueEvent because because few transactions are deleted (considered as onDataChange)
        // otherwise, it would be looping forever
        mDatabaseTransaction.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data : dataSnapshot.getChildren()){ // Loop through the database

                    // Delete the completed transaction of current catalog
                    // Although it doesn't check it's completed or not but it has already checked in checkTransaction
                    if (data.child("catalog_id").getValue().toString().equals(catalogKey)){

                        // Go to the storage where completed transactions' receipts were stored and delete it
                        // Completed transactions' receipt definitely not "nothing"
                        FirebaseStorage.getInstance().getReferenceFromUrl(data.child("receipt").getValue().toString()).delete();
                        data.getRef().removeValue(); // Remove the database

                    }

                }

                // Use SingleValueEvent because because the current catalog are deleted (considered as onDataChange)
                // otherwise, it would be looping forever
                mDatabaseCurrentCatalog.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        // Go to the storage where edited image was stored and delete it
                        FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("images").getValue().toString()).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mDatabaseCurrentCatalog.removeValue(); // Remove the database

                                        // Go to MainActivity and FeedsFragment
                                        mProgress.dismiss();
                                        finish();
                                        Intent mainIntent = new Intent(EditCatalogActivity.this, MainActivity.class);
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
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData(); // Get the Uri
            mEditImage.setImageURI(mImageUri);

        }
    }

    @Override
    public void onBackPressed() {

        // Go to MainActivity and FeedsFragment
        finish();
        Intent mainIntent = new Intent(EditCatalogActivity.this, MainActivity.class);
        mainIntent.putExtra("fragment", MainActivity.FEEDS);
        startActivity(mainIntent);

    }
}
