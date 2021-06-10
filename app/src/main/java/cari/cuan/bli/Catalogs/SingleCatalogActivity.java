package cari.cuan.bli.Catalogs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cari.cuan.bli.MainActivity;
import cari.cuan.bli.R;
import cari.cuan.bli.Transactions.SingleTransactionActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SingleCatalogActivity extends AppCompatActivity {

    // The UI attributes
    // The container
    private RelativeLayout mStockContainer;
    // The other attributes
    private ImageView mImageView;
    private TextView mTitle;
    private TextView mDesc;
    private ImageView mUserImage;
    private TextView mUsername;
    private TextView mClasses;
    private TextView mStock;
    private TextView mPrice;
    private Button mDecrement;
    private TextView mStockField;
    private Button mIncrement;
    private Button mButton;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseCatalog;
    private DatabaseReference mDatabaseTransaction;
    private DatabaseReference mDatabaseUser;

    // Dialogs
    private ProgressDialog mProgress;
    private AlertDialog.Builder builder;

    // The variables used in this class
    private String catalogKey = null;
    private String transactionKey = null;
    private double totalPrice;
    private int mStockBuy = 0; // The stock user choose
    // Retrieved from database
    // Declared here to avoid passing the variables from one method to other methods
    private String title;
    private String desc;
    private String images;
    private String user;
    private String classes;
    private int stock;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_catalog);

        // Get the catalogKey from FeedsFragment
        catalogKey = getIntent().getExtras().getString("catalogId");

        // Make the XML components to java objects
        // The container
        mStockContainer = findViewById(R.id.single_catalog_stock_container);
        // The other attributes
        mImageView = findViewById(R.id.single_catalog_image);
        mTitle = findViewById(R.id.single_catalog_title);
        mDesc = findViewById(R.id.single_catalog_desc);
        mUserImage = findViewById(R.id.single_catalog_user_pic);
        mUsername = findViewById(R.id.single_catalog_username);
        mClasses = findViewById(R.id.single_catalog_class);
        mStock = findViewById(R.id.single_catalog_stock);
        mPrice = findViewById(R.id.single_catalog_price);
        mDecrement = findViewById(R.id.single_catalog_decrement_stock);
        mStockField = findViewById(R.id.single_catalog_stock_field);
        mIncrement = findViewById(R.id.single_catalog_increment_stock);
        mButton = findViewById(R.id.single_catalog_action_btn);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseTransaction = FirebaseDatabase.getInstance().getReference().child("Transactions");
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

        // Set the dialogs
        mProgress = new ProgressDialog(this);
        builder = new AlertDialog.Builder(SingleCatalogActivity.this);

        // Set up the buttons
        mDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStockBuy > 0){ // chosen stocks cannot below zero

                    mStockBuy--;
                    mStockField.setText("" + mStockBuy);

                }
            }
        });

        mIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStockBuy < stock){ // chosen stocks cannot exceed current catalog's stock

                    mStockBuy++;
                    mStockField.setText("" + mStockBuy);

                }
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prepareBuyOrEdit();

            }
        });

        retrieveDataAndSetupTheViews();

    }

    private void retrieveDataAndSetupTheViews() {

        // Use SingleValueEvent because current catalog's stocks could be edited,
        // otherwise, it would be looping forever
        mDatabaseCatalog.child(catalogKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Retrieve
                title = dataSnapshot.child("title").getValue().toString();
                desc = dataSnapshot.child("desc").getValue().toString();
                images = dataSnapshot.child("images").getValue().toString();
                user = dataSnapshot.child("user").getValue().toString();
                classes = dataSnapshot.child("classes").getValue().toString();
                stock = Integer.parseInt(dataSnapshot.child("stocks").getValue().toString());
                price = Double.parseDouble(dataSnapshot.child("price").getValue().toString());

                // Set up the views
                if (mAuth.getCurrentUser().getUid().equals(user)){ // Current catalog is owned by current user

                    mButton.setText("Edit Catalog");
                    mStockContainer.setVisibility(View.GONE);

                }
                mTitle.setText(title);
                mDesc.setText(desc);
                mClasses.setText(classes);
                mStock.setText(stock + " stock(s) left");
                mPrice.setText("Rp " + price);
                mStockField.setText("" + mStockBuy);
                Picasso.get().load(Uri.parse(images)).into(mImageView);
                // Retrieve data from database users
                mDatabaseUser.child(user).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Picasso.get().load(Uri.parse(dataSnapshot.child("image").getValue().toString())).into(mUserImage);
                        mUsername.setText(dataSnapshot.child("name").getValue().toString());

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

    private void prepareBuyOrEdit() {

        // Retrieve the data from catalog database
        // Use SingleValueEvent because current catalog's stocks would be subtracted,
        // otherwise, it would be looping forever
        mDatabaseCatalog.child(catalogKey).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                // Retrieve the user id from current catalog
                String user = dataSnapshot.child("user").getValue().toString();

                if (mAuth.getCurrentUser().getUid().equals(user)){ // current user owns current catalog

                    // Go to EditActivity
                    finish();
                    Intent editIntent = new Intent(SingleCatalogActivity.this, EditCatalogActivity.class);
                    editIntent.putExtra("catalogId", catalogKey);
                    startActivity(editIntent);

                } else {

                    if (mStockBuy <= 0) {

                        // Set the error message
                        Toast.makeText(SingleCatalogActivity.this, "Stock couldn't be 0", Toast.LENGTH_LONG).show();

                    } else { // Chosen stocks have to be greater than zero

                        calculateTotalPriceAndConfirmBuy(dataSnapshot);

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void calculateTotalPriceAndConfirmBuy(final DataSnapshot dataSnapshot) {

        // Calculate the totalPrice
        totalPrice = mStockBuy * price;

        // Make the alert dialog
        builder.setTitle("Confirm Buy")
                .setMessage("Are you sure to buy " + title + " with total price Rp " + totalPrice + " ?\nYou can cancel it later")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        startBuying(dataSnapshot);

                    }
                });
        builder.show();
    }

    private void startBuying(final DataSnapshot dataSnapshot) {

        // Show the progress dialog
        mProgress.setMessage("Buying");
        mProgress.show();

        DatabaseReference newTransaction = mDatabaseTransaction.push(); // Make new transaction database
        transactionKey = newTransaction.getKey();

        // Set up the database transaction
        newTransaction.child("catalog_id").setValue(catalogKey);
        // Date
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy, HH:MM");
        String currentDate = dateFormat.format(currentTime);
        newTransaction.child("date").setValue(currentDate);

        newTransaction.child("buyer").setValue(mAuth.getCurrentUser().getUid());
        newTransaction.child("seller").setValue(dataSnapshot.child("user").getValue().toString());
        newTransaction.child("stocks").setValue(mStockBuy);
        newTransaction.child("total_price").setValue(totalPrice);
        newTransaction.child("confirmed").setValue(false);
        newTransaction.child("rejected").setValue(false);
        newTransaction.child("receipt").setValue("nothing").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                // Update the stocks of current catalog bought
                updateStocksAndFinish(dataSnapshot);

            }
        });

    }

    private void updateStocksAndFinish(DataSnapshot dataSnapshot) {

        mDatabaseCatalog.child(catalogKey).child("stocks")
                .setValue(Integer.parseInt(dataSnapshot.child("stocks").getValue().toString()) - mStockBuy)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Go to SingleTransactionActivity
                        mProgress.dismiss();
                        finish();
                        Intent singleTransactionIntent = new Intent(SingleCatalogActivity.this, SingleTransactionActivity.class);
                        singleTransactionIntent.putExtra("transactionId", transactionKey);
                        startActivity(singleTransactionIntent);

                    }
                });

    }

    @Override
    public void onBackPressed() {

        // Go to MainActivity and FeedsFragment
        finish();
        Intent mainIntent = new Intent(SingleCatalogActivity.this, MainActivity.class);
        mainIntent.putExtra("fragment", MainActivity.FEEDS);
        startActivity(mainIntent);

    }
}