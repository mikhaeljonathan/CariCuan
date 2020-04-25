package com.example.caricuan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.caricuan.MainFragment.FeedsFragment;
import com.example.caricuan.Entries.FirstActivity;
import com.example.caricuan.Entries.SetupActivity;
import com.example.caricuan.MainFragment.MoreFragment;
import com.example.caricuan.MainFragment.TransactionsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    // The UI attributes
    private BottomNavigationView bottomNav;
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() { // Set the bottomNavigationView
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch(item.getItemId()){
                case R.id.nav_feed:
                    selectedFragment = new FeedsFragment();
                    break;
                case R.id.nav_transactions:
                    selectedFragment = new TransactionsFragment();
                    break;
                case R.id.nav_more:
                    selectedFragment = new MoreFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, selectedFragment).commit();
            return true;
        }
    };

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseCatalog;
    private DatabaseReference mDatabaseUsers;

    // The variable used in this class
    private int goToFragment;
    private Fragment selectedFragment;

    // Static variable to go to certain fragment
    public static final int FEEDS = 0;
    public static final int TRANSACTION = 1;
    public static final int MORE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the goToFragment from any activities
        if (getIntent().getExtras() != null) {

            goToFragment = getIntent().getExtras().getInt("fragment");

        } else { // Default

            goToFragment = 0;

        }

        // Make the XML components to java objects
        // Setup the Bottom Nav Bar
        bottomNav = findViewById(R.id.main_nav_bottom);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        // Set go to fragment
        switch (goToFragment){
            case 0:
                bottomNav.setSelectedItemId(R.id.nav_feed);
                selectedFragment = new FeedsFragment();
                break;
            case 1:
                bottomNav.setSelectedItemId(R.id.nav_transactions);
                selectedFragment = new TransactionsFragment();
                break;
            case 2:
                bottomNav.setSelectedItemId(R.id.nav_more);
                selectedFragment = new MoreFragment();
                break;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, selectedFragment).commit();

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null){ // User has't logged in yet

                    // Go to FirstActivity
                    finish();
                    startActivity(new Intent(MainActivity.this, FirstActivity.class));

                }
            }
        };
        mDatabaseCatalog = FirebaseDatabase.getInstance().getReference().child("Catalogues");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        // Keep them offline
        mDatabaseCatalog.keepSynced(true);
        mDatabaseUsers.keepSynced(true);

        // Check the logged user is a first time user or not
        checkUserExist();

    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null){

            //Check the user's UID is in the database or not
            final String userId = mAuth.getCurrentUser().getUid(); //Get the current user's UID
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(userId)) { //Database has the current user's UID value

                        startActivity(new Intent(MainActivity.this, SetupActivity.class)); //Go to SetupActivity

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
