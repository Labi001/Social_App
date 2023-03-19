package com.labinot.bajrami.social_app.activities;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;
import static com.labinot.bajrami.social_app.Helper.Utils.isEmailValid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.Utils;
import com.labinot.bajrami.social_app.MainActivity;
import com.labinot.bajrami.social_app.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText username;
    private EditText name;
    private EditText email;
    private EditText password;
    private Button register;
    private TextView login_user;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        login_user = findViewById(R.id.login_user);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        login_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txtUsername = username.getText().toString();
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();

                if (TextUtils.isEmpty(txtUsername)) {

                    username.setError("Enter Username");

                } else if (TextUtils.isEmpty(txtName)) {

                    name.setError("Enter Name");

                } else if (TextUtils.isEmpty(txtEmail) || !isEmailValid(txtEmail)) {

                    if (!isEmailValid(txtEmail) && !TextUtils.isEmpty(txtEmail)) {
                        email.setError("Your Email is Invalid.");
                    } else {
                        email.setError("Enter Email");
                    }

                } else if (TextUtils.isEmpty(txtPassword) || txtPassword.length() < 6) {

                    if (txtPassword.length() < 6 && !TextUtils.isEmpty(txtPassword)) {
                        password.setError("Password to short!");
                    } else {
                        password.setError("Enter Password");
                    }
                } else {
                    registerUser(txtUsername, txtName, txtEmail, txtPassword);
                }



            }
        });

    }

    private void registerUser(String txtUsername, String txtName, String txtEmail, String txtPassword) {

        progressDialog.setMessage("Please Wait");
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                HashMap<String, Object> userObject = new HashMap<>();
                userObject.put(Constants.NAME, txtName);
                userObject.put(Constants.EMAIL, txtEmail);
                userObject.put(Constants.USERNAME, txtUsername);
                userObject.put(Constants.ID, mAuth.getCurrentUser().getUid());
                userObject.put(Constants.BIO, "");
                userObject.put(Constants.IMAGEURL, Constants.IMAGE_DEFAULT_URL);

                mRootRef.child(Constants.USERS).child(mAuth.getCurrentUser().getUid()).setValue(userObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            progressDialog.dismiss();

                            Log.d(LOG_TAG, "User created in firestore database with success");

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Utils.showMessageSnackBar(RegisterActivity.this,findViewById(R.id.register_layout),e.getMessage());

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Utils.showMessageSnackBar(RegisterActivity.this,findViewById(R.id.register_layout),e.getMessage());
            }
        });

    }
}