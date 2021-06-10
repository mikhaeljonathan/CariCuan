package cari.cuan.bli.Entries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cari.cuan.bli.MainActivity;
import cari.cuan.bli.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // The UI attributes
    private EditText mLoginEmailField;
    private EditText mLoginPasswordField;
    private TextView mLoginError;
    private Button mLoginBtn;
    private SignInButton mGoogleBtn;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private GoogleSignInClient mGoogleSignInClient;

    // Progress Dialog
    private ProgressDialog mProgress;

    // For final attributes (you can choose the value arbitrarily)
    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Make the XML components to java objects
        mLoginEmailField = findViewById(R.id.login_email_field);
        mLoginPasswordField = findViewById(R.id.login_password_field);
        mLoginError = findViewById(R.id.login_error);
        mLoginBtn = findViewById(R.id.login_button);
        mGoogleBtn = findViewById(R.id.login_google_button);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);  //To keep it offline
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build(); //Set up the GoogleSignInOptions
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the progress dialog
        mProgress = new ProgressDialog(this);

        // Set up the buttons
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin();
            }
        });
        mGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // Set the error TextView
        mLoginError.setVisibility(View.GONE);
    }

    // Login with email
    private void checkLogin() {
        // Get the text of email and password from EditText
        String email = mLoginEmailField.getText().toString().trim();
        String password = mLoginPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(email)){ // Empty email field

            // Set the error message
            mLoginEmailField.requestFocus();
            mLoginEmailField.setError("Empty email");

        } else if (TextUtils.isEmpty(password)){ //Empty password field

            // Set the error message
            mLoginPasswordField.requestFocus();
            mLoginPasswordField.setError("Empty password");

        } else {

            // Show the progress dialog
            mProgress.setMessage("Checking Login ...");
            mProgress.show();

            // Start signing in
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){ // Email and password match

                        checkUserExist(); // Check if first user or not

                    } else {

                        // Set the error message
                        mProgress.dismiss();
                        mLoginError.setVisibility(View.VISIBLE);
                        mLoginError.setText("Error login");

                    }
                }
            });

        }
    }

    // Login with google
    private void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    // Do something when startActivityForResult executed
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) { // The requestCode is the same as we pass to the parameter

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            // Show the progress dialog
            mProgress.setMessage("Starting sign in ...");
            mProgress.show();

            try {

                // Google Sign In is successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                // Google Sign In failed, update UI appropriately
                mProgress.dismiss();
                Toast.makeText(LoginActivity.this, "Error occurred", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            checkUserExist();

                        } else {

                            // If sign in fails, display a message to the user.
                            Snackbar.make(findViewById(R.id.login_google_button), "Authentication Failed.", Snackbar.LENGTH_LONG).show();

                        }

                    }
                });
    }

    //This method is used both by google sign in and email sign in
    private void checkUserExist() {

        if (mAuth.getCurrentUser() != null){

            //Check the user's UID is in the database or not
            final String userId = mAuth.getCurrentUser().getUid(); //Get the current user's UID
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) { //Database has the current user's UID value

                        //Go to MainActivity
                        mProgress.dismiss();
                        finish();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));

                    } else {

                        //Go to SetupActivity
                        mProgress.dismiss();
                        finish();
                        startActivity(new Intent(LoginActivity.this, SetupActivity.class));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {

        // Go to FirstActivity
        finish();
        startActivity(new Intent(LoginActivity.this, FirstActivity.class));

    }

}
