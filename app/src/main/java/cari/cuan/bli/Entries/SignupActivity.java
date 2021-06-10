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

import cari.cuan.bli.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    // The UI attributes
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmField;
    private TextView mSignupError;
    private Button mSignupBtn;

    // The Firebase attributes
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUser;

    // Progress Dialog
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Make the XML components to java objects
        mEmailField = findViewById(R.id.signup_email_field);
        mPasswordField = findViewById(R.id.signup_password_field);
        mConfirmField = findViewById(R.id.signup_confirm_field);
        mSignupError = findViewById(R.id.signup_error);
        mSignupError.setVisibility(View.GONE);
        mSignupBtn = findViewById(R.id.signup_button);

        // Set up the firebase attributes
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");

        // Set the progress dialog
        mProgress = new ProgressDialog(this);

        // Set up the button
        mSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });
    }

    private void startRegister() {
        // Get the text of email, password, and confirm password from EditText
        String email = mEmailField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();
        String confirm = mConfirmField.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {

            // Set the error message
            mEmailField.requestFocus();
            mEmailField.setError("Empty email");

        } else if (TextUtils.isEmpty(password)){

            // Set the error message
            mPasswordField.requestFocus();
            mPasswordField.setError("Empty password");

        } else if (TextUtils.isEmpty(confirm)) {

            // Set the error message
            mConfirmField.requestFocus();
            mConfirmField.setError("Empty password");

        } else {

            if (!password.equals(confirm)) { // Password and confirm aren't equal

                // Set the error message
                mSignupError.setVisibility(View.VISIBLE);

            } else {

                // Show the progress dialog
                mProgress.setMessage("Signing up ...");
                mProgress.show();

                // Start signing up
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mProgress.dismiss();
                        if (task.isSuccessful()) {

                            //Go to SetupActivity (The user must be the first user)
                            finish();
                            startActivity(new Intent(SignupActivity.this, SetupActivity.class));

                        } else {

                            // Set the error message
                            try {

                                throw task.getException();

                            } catch (FirebaseAuthWeakPasswordException e) {

                                mPasswordField.setError(e.getMessage());
                                mPasswordField.requestFocus();

                            } catch (FirebaseAuthInvalidCredentialsException e) {

                                mEmailField.setError(e.getMessage());
                                mEmailField.requestFocus();

                            } catch (FirebaseAuthUserCollisionException e) {

                                mEmailField.setError(e.getMessage());
                                mEmailField.requestFocus();

                            } catch (Exception e) {

                                mSignupError.setVisibility(View.VISIBLE);
                                mSignupError.setText("Sign up Error");

                            }

                        }
                    }
                });
            }

        }

    }

    @Override
    public void onBackPressed() {

        // Go to FirstActivity
        finish();
        startActivity(new Intent(SignupActivity.this, FirstActivity.class));

    }

}
