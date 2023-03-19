package com.labinot.bajrami.social_app.activities;

import static com.labinot.bajrami.social_app.Helper.Utils.isEmailValid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.labinot.bajrami.social_app.Helper.Utils;
import com.labinot.bajrami.social_app.MainActivity;
import com.labinot.bajrami.social_app.R;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView registerUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login);
        registerUser = findViewById(R.id.register_user);

        mAuth = FirebaseAuth.getInstance();

        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if(TextUtils.isEmpty(txt_email) || !isEmailValid(txt_email)){

                    if (!isEmailValid(txt_email) && !TextUtils.isEmpty(txt_email))
                        email.setError("Your Email is Invalid.");
                     else
                        email.setError("Enter Email");


                }else if(TextUtils.isEmpty(txt_password) || txt_password.length() < 6) {

                    if (txt_password.length() < 6 && !TextUtils.isEmpty(txt_password))
                        password.setError("Password to short!");
                     else
                        password.setError("Enter Password");


                } else {

                    loginUser(txt_email,txt_password);
                }


            }
        });


    }

    private void loginUser(String txt_email, String txt_password) {

        mAuth.signInWithEmailAndPassword(txt_email,txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof FirebaseAuthInvalidUserException)
                    email.setError("Your email is wrong!");
                 else if (e instanceof FirebaseAuthInvalidCredentialsException)
                    password.setError("Your password is wrong!");

                 Utils.showMessageSnackBar(LoginActivity.this,findViewById(R.id.login_layout),e.getMessage());

            }
        });

    }
}