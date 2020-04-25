package com.example.caricuan.MainFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.caricuan.Profiles.EditProfileActivity;
import com.example.caricuan.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MoreFragment extends Fragment {

    // The UI attributes
    private ImageView mProfileImage;
    private TextView mUsername;
    private TextView mClasses;
    private TextView mEmail;
    private View mEditProfile;
    private View mLogOut;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_more, container, false);

        // Make the XML components to java objects
        mProfileImage = rootView.findViewById(R.id.profile_pic);
        mUsername = rootView.findViewById(R.id.profile_name);
        mClasses = rootView.findViewById(R.id.profile_classes);
        mEmail = rootView.findViewById(R.id.profile_email);
        mEditProfile = rootView.findViewById(R.id.menu_edit_profile);
        mLogOut = rootView.findViewById(R.id.menu_logout);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

        // Set up the buttons
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Go to EditProfileActivity
                startActivity(new Intent(getActivity(), EditProfileActivity.class));

            }
        });

        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.signOut();

            }
        });

        retrieveDataAndSetupTheViews();

        return rootView;
    }

    private void retrieveDataAndSetupTheViews() {

        // Retrieve data from database user
        mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (getActivity() == null) return;

                // Set up the views
                Glide.with(MoreFragment.this).load(dataSnapshot.child("image").getValue().toString())
                        .apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                mUsername.setText(dataSnapshot.child("name").getValue().toString());
                mClasses.setText(dataSnapshot.child("classes").getValue().toString());
                mEmail.setText(mAuth.getCurrentUser().getEmail());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
