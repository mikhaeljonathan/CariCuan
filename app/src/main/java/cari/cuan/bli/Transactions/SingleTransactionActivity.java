package cari.cuan.bli.Transactions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class SingleTransactionActivity extends AppCompatActivity {

    // The UI attributes
    // The container
    private LinearLayout mContainer;
    // The other attributes
    private ImageView mImage;
    private Button mSaveQr;
    private TextView mTitle;
    private TextView mUser;
    private TextView mPrice;
    private TextView mText;
    private ImageView mReceipt;
    private Button mSaveReceipt;
    private TextView mNote;
    private Button mCancel;
    private Button mUploadBtn;
    private Button mReject;
    private Button mAccept;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseCatalog;
    private DatabaseReference mDatabaseTransaction;
    private StorageReference mStorageReceipts;

    private StorageReference qrFilePath;
    private StorageReference receiptFilePath;

    // Dialogs
    private ProgressDialog mProgress;
    private AlertDialog.Builder builder;

    // The variables used in this class
    private String transactionKey = null;
    private Uri mImageUri = null;

    private String catalogId;
    private boolean confirmed;
    private boolean rejected;
    private boolean hasReceipt;
    private String buyer;
    private String seller;
    private int stock;

    // For final attributes (you can choose the value arbitrarily)
    private static final int GALLERY_REQUEST = 1;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_transaction);

        // Get the catalogKey from PayableFragment or ReceivableFragment
        transactionKey = getIntent().getExtras().getString("transactionId");

        // Make the XML components to java objects
        // The container
        mContainer = findViewById(R.id.single_transaction_container);
        // The other attributes
        mImage = findViewById(R.id.single_transaction_image);
        mSaveQr = findViewById(R.id.single_transaction_save);
        mTitle = findViewById(R.id.single_transaction_title);
        mUser = findViewById(R.id.single_transaction_user);
        mPrice = findViewById(R.id.single_transaction_price);
        mText = findViewById(R.id.single_transaction_text);
        mReceipt = findViewById(R.id.single_transaction_receipt);
        mSaveReceipt = findViewById(R.id.single_transaction_save_receipt);
        mNote = findViewById(R.id.single_transaction_note);
        mCancel = findViewById(R.id.single_transaction_cancel_btn);
        mCancel.setVisibility(View.GONE);
        mUploadBtn = findViewById(R.id.single_transaction_upload_btn);
        mReject = findViewById(R.id.single_transaction_reject);
        mAccept = findViewById(R.id.single_transaction_accept);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mStorageReceipts = FirebaseStorage.getInstance().getReference().child("Receipts");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");
        mDatabaseTransaction = FirebaseDatabase.getInstance().getReference().child("Transactions");

        // Set the dialogs
        mProgress = new ProgressDialog(this);
        builder = new AlertDialog.Builder(SingleTransactionActivity.this);

        // Set up the buttons
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmCancel();

            }
        });

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImageUri != null){ // User has set the receipt

                    confirmUpload();

                } else { // User hasn't set the receipt

                    Toast.makeText(SingleTransactionActivity.this, "Choose the receipt", Toast.LENGTH_LONG).show();

                }
            }
        });

        mReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmReject();

            }
        });

        mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmAccept();

            }
        });

        mSaveQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startSaving(qrFilePath, "Qr");

            }
        });

        mSaveReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startSaving(receiptFilePath, "Receipt");

            }
        });

        retrieveDataAndSetupTheViews();

    }

    // Views
    private void retrieveDataAndSetupTheViews() {

        // Use SingleValueEvent because because few transactions are deleted (considered as onDataChange)
        // otherwise, it would be looping forever
        mDatabaseTransaction.child(transactionKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                // Retrieve some variables
                catalogId = dataSnapshot.child("catalog_id").getValue().toString();
                confirmed = Boolean.parseBoolean(dataSnapshot.child("confirmed").getValue().toString());
                rejected = Boolean.parseBoolean(dataSnapshot.child("rejected").getValue().toString());
                hasReceipt = !(dataSnapshot.child("receipt").getValue().toString().equals("nothing"));
                buyer = dataSnapshot.child("buyer").getValue().toString();
                seller = dataSnapshot.child("seller").getValue().toString();
                stock = Integer.parseInt(dataSnapshot.child("stocks").getValue().toString());

                // Retrieve all
                // For TitleTextView
                setTitleTextView(dataSnapshot);
                // For UserTextView and mImage ImageView and qr FilePath
                setUserTextViewAndImageViewAndQrFilePath(dataSnapshot);
                // For price TextView
                setPriceTextView(dataSnapshot);
                // For receipt ImageView and receipt FilePath
                setReceiptImageViewAndReceiptFilePath(dataSnapshot);
                // For note TextView
                setNoteTextView(dataSnapshot);

                // Set all the views
                if (confirmed){

                    mText.setVisibility(View.GONE);
                    mUploadBtn.setVisibility(View.GONE);
                    mSaveQr.setVisibility(View.GONE);
                    mContainer.setVisibility(View.GONE); // Can't reject or accept anymore

                } else if (buyer.equals(mAuth.getCurrentUser().getUid())){ // Current user is a buyer

                    mContainer.setVisibility(View.GONE); // Buyer can't reject or accept

                    if (rejected){ // Seller rejected the buyer's receipt

                        mSaveReceipt.setVisibility(View.GONE);

                    } else { // Seller hasn't rejected the receipt

                        if (hasReceipt) { // The receipt exists

                            mUploadBtn.setVisibility(View.GONE);

                        } else { // The buyer hasn't uploaded any receipts yet

                            mNote.setVisibility(View.GONE);
                            mSaveReceipt.setVisibility(View.GONE);
                            mCancel.setVisibility(View.VISIBLE);

                        }

                    }

                } else { // The current user is a seller

                    mImage.setVisibility(View.GONE); // Don't need to show seller's qr
                    mSaveQr.setVisibility(View.GONE);
                    mText.setVisibility(View.GONE);
                    mUploadBtn.setVisibility(View.GONE); // Don't need to upload receipt

                    if (rejected){ // Seller rejected the receipt

                        mContainer.setVisibility(View.GONE); // Can't reject or accept anymore

                    } else { // Seller hasn't rejected the receipt

                        if (hasReceipt){ // The receipt exists

                            mNote.setVisibility(View.GONE);

                        } else { // The buyer hasn't uploaded any receipts yet

                            mSaveReceipt.setVisibility(View.GONE);
                            mReceipt.setVisibility(View.GONE);
                            mContainer.setVisibility(View.GONE);

                        }
                    }

                }

                // Set the mReceipt ImageView so it can be clickable
                mReceipt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // The buyer hasn't uploaded any receipts yet
                        // or the receipt rejected and the buyer has to resubmit the receipt
                        if (!hasReceipt || (rejected && buyer.equals(mAuth.getCurrentUser().getUid()))){

                            // Open the gallery
                            Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            galleryIntent.setType("image/*");
                            startActivityForResult(galleryIntent, GALLERY_REQUEST);

                        } else {

                            // Do nothing

                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setTitleTextView(DataSnapshot dataSnapshot) {

        mDatabaseCatalog.child(dataSnapshot.child("catalog_id").getValue().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        mTitle.setText(dataSnapshot.child("title").getValue().toString());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void setUserTextViewAndImageViewAndQrFilePath(DataSnapshot dataSnapshot) {

        if (buyer.equals(mAuth.getCurrentUser().getUid())){ // If the current user buys the catalog

            mDatabaseUser.child(seller).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // TextView
                    mUser.setText("Seller:\n" + dataSnapshot.child("name").getValue().toString() + " - "
                            + dataSnapshot.child("classes").getValue().toString());

                    // ImageView
                    if (confirmed){ // Completed transactions set the same ImageView for both buyer and seller

                        mImage.setImageResource(R.drawable.ic_check_green_24dp);
                        mImage.setMinimumWidth(150);
                        mImage.setMaxWidth(150);
                        mImage.setMinimumHeight(150);
                        mImage.setMaxHeight(150);

                    } else {

                        String qrUrl = dataSnapshot.child("qr").getValue().toString();

                        Picasso.get().load(Uri.parse(qrUrl)).into(mImage);
                        // Set the FilePath to be downloaded
                        qrFilePath = FirebaseStorage.getInstance().getReferenceFromUrl(qrUrl);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else { // If the current user sells the catalog

            mDatabaseUser.child(buyer).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // TextView
                    mUser.setText("Buyer:\n" + dataSnapshot.child("name").getValue().toString() + " - "
                            + dataSnapshot.child("classes").getValue().toString());

                    // ImageView
                    if (confirmed) { // Completed transactions set the same ImageView for both buyer and seller

                        mImage.setImageResource(R.drawable.ic_check_green_24dp);
                        mImage.setMinimumWidth(150);
                        mImage.setMaxWidth(150);
                        mImage.setMinimumHeight(150);
                        mImage.setMaxHeight(150);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private void setPriceTextView(DataSnapshot dataSnapshot) {

        mPrice.setText("Rp " + dataSnapshot.child("total_price").getValue().toString());

    }

    private void setReceiptImageViewAndReceiptFilePath(DataSnapshot dataSnapshot) {

        // If the current user is a buyer, the receipt exists, and the receipt was rejected, don't show the receipt
        if (buyer.equals(mAuth.getCurrentUser().getUid()) && rejected){

            // Do nothing

        } else if (hasReceipt){ // The receipt exists

            String receiptUrl = dataSnapshot.child("receipt").getValue().toString();
            // ImageView
            Picasso.get().load(Uri.parse(receiptUrl)).into(mReceipt);
            // Set the FilePath to be downloaded
            receiptFilePath = FirebaseStorage.getInstance().getReferenceFromUrl(receiptUrl);

        }

    }

    private void setNoteTextView(DataSnapshot dataSnapshot) {

        if (confirmed){ // Completed transaction

            mNote.setTextColor(Color.parseColor("#00FF00"));
            mNote.setText("Completed transaction");
            mNote.setTypeface(mNote.getTypeface(), Typeface.BOLD);

        } else { // Uncompleted transaction

            if (buyer.equals(mAuth.getCurrentUser().getUid())) { // Current user is a buyer

                if (rejected) { // Rejected by the seller

                    mNote.setTextColor(Color.parseColor("#FF0000"));
                    mNote.setText("Your receipt has rejected from the seller.\nPlease resubmit your receipt");

                } else if (hasReceipt) { // Buyer has uploaded their receipt and seller hasn't done an action (reject or accept)

                    mNote.setTextColor(Color.parseColor("#000000"));
                    mNote.setText("Waiting for the seller to accept your receipt");

                }

            } else { // Current user is a seller

                if (rejected) { // Seller rejected the receipt

                    mNote.setTextColor(Color.parseColor("#FF0000"));
                    mNote.setText("You has rejected the receipt from the buyer.\nPlease wait the buyer to resubmit their receipt");

                } else if (!hasReceipt){ // Seller hasn't rejected because the buyer hasn't uploaded the receipt

                    mNote.setTextColor(Color.parseColor("#000000"));
                    mNote.setText("Please wait for the buyer to upload their receipt");

                }

            }

        }

    }

    // Buttons
    private void confirmCancel() {

        // Make the alert dialog
        builder.setTitle("Confirm Cancel")
                .setMessage("Are you sure to cancel this order?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                startCancelling();

            }
        });
        builder.show();

    }

    private void startCancelling() {

        // Show the progress dialog
        mProgress.setMessage("Cancelling...");
        mProgress.show();

        mDatabaseCatalog.child(catalogId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Restore the current catalog's stock
                int currentStock = Integer.parseInt(dataSnapshot.child("stocks").getValue().toString());
                mDatabaseCatalog.child(catalogId).child("stocks").setValue(currentStock + stock);

                // Delete current transaction
                mDatabaseTransaction.child(transactionKey).removeValue(); // Remove the database

                // Go to MainActivity and TransactionFragment
                mProgress.dismiss();
                finish();
                Intent mainIntent = new Intent(SingleTransactionActivity.this, MainActivity.class);
                mainIntent.putExtra("fragment", MainActivity.TRANSACTION);
                startActivity(mainIntent);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void confirmUpload() {

        // Make the alert dialog
        builder.setTitle("Confirm Upload")
                .setMessage("Are you sure to upload this receipt?")
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

    private void startUploading() {

        // Show the progress dialog
        mProgress.setMessage("Uploading...");
        mProgress.show();

        // Make the space in the Firebase storage
        final StorageReference filepath = mStorageReceipts.child(mImageUri.getLastPathSegment());
        // Upload the image by the Uri
        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // Get the Uri
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        if (hasReceipt) { // Rejected receipt

                            // Delete the last receipt in the storage before reupload
                            mDatabaseTransaction.child(transactionKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    // Go to the storage where edited image was stored and delete it
                                    FirebaseStorage.getInstance().getReferenceFromUrl(dataSnapshot.child("receipt").getValue().toString())
                                            .delete();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                        // Add them to database
                        mDatabaseTransaction.child(transactionKey).child("receipt").setValue(uri.toString());
                        mDatabaseTransaction.child(transactionKey).child("rejected").setValue(false)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mProgress.dismiss();
                                        restartActivity();

                                    }
                                });

                    }
                });
            }
        });

    }

    private void confirmReject() {

        // Make the alert dialog
        builder.setTitle("Confirm Reject")
                .setMessage("Are you sure to reject this receipt?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mDatabaseTransaction.child(transactionKey).child("rejected").setValue(true);

                restartActivity();

            }
        });
        builder.show();
    }

    private void confirmAccept() {

        // Make the alert dialog
        builder.setTitle("Confirm Accept")
                .setMessage("Are you sure to accept this receipt?\nOnce you accept, can't be undone")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mDatabaseTransaction.child(transactionKey).child("rejected").setValue(false);
                mDatabaseTransaction.child(transactionKey).child("confirmed").setValue(true);

                restartActivity();

            }
        });

        builder.show();
    }

    private void startSaving(StorageReference filePath, final String fileName) {

        // Get the Uri
        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                String url = uri.toString();
                downloadFile(SingleTransactionActivity.this, fileName, ".jpg", DIRECTORY_DOWNLOADS, url);

                // Make the alert dialog
                builder.setTitle("Download QR")
                        .setMessage("Download completed")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // Do nothing

                            }
                        });
                builder.show();

            }
        });

    }

    private void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadManager.enqueue(request);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK){

            mImageUri = data.getData(); // Get the Uri
            mReceipt.setImageURI(mImageUri);

        }
    }

    private void restartActivity() {

        Intent intent = getIntent();
        finish();
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {

        // Go to MainActivity and TransactionFragment
        finish();
        Intent mainIntent = new Intent(SingleTransactionActivity.this, MainActivity.class);
        mainIntent.putExtra("fragment", MainActivity.TRANSACTION);
        startActivity(mainIntent);

    }
}
