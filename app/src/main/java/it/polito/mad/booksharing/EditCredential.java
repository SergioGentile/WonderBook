package it.polito.mad.booksharing;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditCredential extends AppCompatActivity {

    Button btnOk;
    EditText edtMail, edtPassword;
    User user;
    private String fromActivity;
    private String clean_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_credential);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = getIntent().getParcelableExtra("user");
        fromActivity = getIntent().getStringExtra("from");

        btnOk = (Button) findViewById(R.id.confirm_new_access_credential);
        edtMail = (EditText) findViewById(R.id.changeMail);
        edtPassword = (EditText) findViewById(R.id.changePwd);

        //Set text value
        edtMail.setText(user.getEmail().getValue());
        //Need to set PasswordField

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (fromActivity.equals("Edit") && checkForm(edtPassword.getText().toString())) {

                    LayoutInflater inflater=EditCredential.this.getLayoutInflater();
                    //this is what I did to added the layout to the alert dialog
                    final View layout=inflater.inflate(R.layout.my_alert_pwd,null);

                    AlertDialog dialog = new AlertDialog.Builder(EditCredential.this)
                            .setTitle(getString(R.string.alert_title))
                            .setMessage(getString(R.string.editpwd))
                            .setView(layout)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TextInputEditText edittext = (TextInputEditText) layout.findViewById(R.id.my_pwd_edit);
                                    String current_pwd = edittext.getText().toString();
                                    try {
                                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().getValue(), current_pwd);
                                        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                updateCredential();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(EditCredential.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();


                }else{
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putString("mail", edtMail.getText().toString());
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

            }
        });

    }

    private void updateCredential() {

        clean_mail = edtMail.getText().toString().toLowerCase().replace(" ", "");

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("mail", edtMail.getText().toString());
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);

        if (!user.checkMailFormat(clean_mail)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditCredential.this);
            alertDialog.setTitle(getString(R.string.alert_title))
                    .setMessage(R.string.mail_not_valid)
                    .setNeutralButton(getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Nothing to do
                        }
                    }).show();
            return;
        }

        if (!edtPassword.getText().toString().equals("")) {
            FirebaseAuth.getInstance().getCurrentUser().updatePassword(edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(EditCredential.this, "Password cambiata a "+FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditCredential.this, "Password non cambiata",
                            Toast.LENGTH_LONG).show();
                    Log.d("updatePswFail",e.getMessage());
                }
            });


        }

        //Remember to verify new email
        if (!user.getEmail().getValue().equals(edtMail.getText().toString())) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditCredential.this);
            alertDialog.setTitle(getString(R.string.alert_title))
                    .setMessage(getString((R.string.changeMail)))
                    .setPositiveButton(getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            user.setEmail(new User.MyPair(edtMail.getText().toString(), user.getEmail().getStatus()));
                            FirebaseAuth.getInstance().getCurrentUser().updateEmail(user.getEmail().getValue());
                            FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
                            dbref.child("users").child(user.getKey()).child("email/value").setValue(clean_mail);
                            finish();
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            }).show();
        }else{
            finish();
        }

    }

    private boolean checkForm(String pwd){
        if (!pwd.equals("") && pwd.length()<6) {
            edtPassword.setError(getString(R.string.error_invalid_password));
            return false;
        }
        else if(pwd.equals("") && edtMail.getText().toString().equals(user.getEmail().getValue()))
        {
            return false;
        }
        return true;
    }

}






