package it.polito.mad.booksharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {

    private EditText loginEmail , loginPassword, loginConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putString("mail", loginEmail.getText().toString());
        outState.putString("pass", loginPassword.getText().toString());
        outState.putString("passConf", loginConfirmPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        loginEmail.setText(inState.getString("mail"));
        loginPassword.setText(inState.getString("pass"));
        loginConfirmPassword.setText(inState.getString("passConf"));
    }


    Button btnRegister;

    private String clean_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = (Button) findViewById(R.id.buttonRegister);
        loginEmail = (EditText) findViewById(R.id.edtLoginName);
        loginPassword = (EditText) findViewById(R.id.edtLoginPassword);
        loginConfirmPassword = (EditText)findViewById(R.id.edtLoginPassword2);

        mAuth = FirebaseAuth.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmail.setError(null);
                loginPassword.setError(null);

                clean_email = loginEmail.getText().toString().toLowerCase().replace(" ","");
                String password = loginPassword.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid password, if the user entered one.
                if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                    loginPassword.setError(getString(R.string.error_invalid_password));
                    focusView = loginPassword;
                    cancel = true;
                }

                // Check for a valid email address.
                if (TextUtils.isEmpty(clean_email)) {
                    loginEmail.setError(getString(R.string.error_field_required));
                    focusView = loginEmail;
                    cancel = true;
                } else if (!isEmailValid(clean_email)) {
                    loginEmail.setError(getString(R.string.error_invalid_email));
                    focusView = loginEmail;
                    cancel = true;
                }

                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {

                    mAuth.createUserWithEmailAndPassword(clean_email, password)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        mAuth.getCurrentUser().sendEmailVerification();
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, getString(R.string.email_verif_toast1) +clean_email+ getString(R.string.email_verif_toast2),
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Register.this, MainPage.class);
                                        startActivity(intent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    // ...
                                }
                            });
                }

            }
        });
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }
}
