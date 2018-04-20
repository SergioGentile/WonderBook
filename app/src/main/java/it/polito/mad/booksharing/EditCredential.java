package it.polito.mad.booksharing;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
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

    Button btnEmail,btnPwd;
    EditText edtMail, edtPassword;
    User user;
    private String fromActivity;
    private String clean_mail;
    private Switch swMail;
    private String email_status;
    private ProgressBar progress;
    private LinearLayout container;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_credential);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set all the variable
        user = getIntent().getParcelableExtra("user");
        fromActivity = getIntent().getStringExtra("from");
        email_status=user.getEmail().getStatus();
        btnPwd= (Button) findViewById(R.id.confirm_new_password);
        btnEmail = (Button) findViewById(R.id.confirm_new_email);
        edtMail = (EditText) findViewById(R.id.changeMail);
        edtPassword = (EditText) findViewById(R.id.changePwd);
        swMail = (Switch) findViewById(R.id.email_switch);
        progress = (ProgressBar) findViewById(R.id.editCred_progress);
        container = (LinearLayout) findViewById(R.id.editCredContainer);
        //Set text value
        edtMail.setText(user.getEmail().getValue());
        //Need to set PasswordField


        if(user.getEmail().getStatus().equals("private")){
            swMail.setChecked(false);
        }

        btnPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pwd = edtPassword.getText().toString();
                //If the new password isn't too weak I try to update it else I do nothing and notify the problem to the user
                if(pwd.length()>5){

                    tryUpdatePwd();

                }else{
                    edtPassword.setError(getString(R.string.weak_pwd),null);
                }
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clean_mail = edtMail.getText().toString().toLowerCase().replace(" ","");
                if(checkMailFormat()){
                    //The mail is in a valid format
                    tryUpdateMail();
                }else if(!email_status.equals(user.getEmail().getStatus())){
                    //update status only
                    user.setEmail(new User.MyPair(user.getEmail().getValue(),email_status));
                    returnToEdit(true);
                }
                showProgress(false);
            }
        });

        swMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Verify the status of the field.
                //If it's public, it will become private, otherwise will become public.
                //In both cases, change also the state of the lock.
                if (swMail.isChecked()) {
                    email_status="public";
                } else {
                    email_status="private";
                }


            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putString("mail", edtMail.getText().toString());
        outState.putString("pass", edtPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        edtMail.setText(inState.getString("mail"));
        edtPassword.setText(inState.getString("pass"));

    }


    private void tryUpdateMail() {

        //Now I need to re-authenticate the user

        LayoutInflater inflater=EditCredential.this.getLayoutInflater();
        //this is what I did to added the layout to the alert dialog
        final View layout=inflater.inflate(R.layout.my_alert_pwd,null);

        final AlertDialog dialog = new AlertDialog.Builder(EditCredential.this)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.editpwd))
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText edittext = (TextInputEditText) layout.findViewById(R.id.my_pwd_edit);
                        String current_pwd = edittext.getText().toString();
                        if(!current_pwd.isEmpty()) {
                            //I need to re-authenticate the user using the current_pwd
                            showProgress(true);
                            try {
                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().getValue(), current_pwd);
                                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //If the user has insert the correct password I update the mail
                                            updateMail();
                                        } else {
                                            //I undo all the changes and exit
                                            restoreValue();
                                            showProgress(false);
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditCredential.this, getString(R.string.authentication_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }else{
                            Toast.makeText(EditCredential.this,getString(R.string.error_auth_failed),Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
        //Used to allow the dialog to survive during an orientation change
        keepDialog(dialog);
    }

    private void restoreValue() {

        //I restore the initial value
        if(user.getEmail().getStatus().equals("private")){
            swMail.setChecked(false);
        }else{
            swMail.setChecked(true);
        }

        edtMail.setText(user.getEmail().getValue());


    }

    private void updateMail() {


        Toast.makeText(EditCredential.this, getString(R.string.changeMail),
                Toast.LENGTH_LONG).show();
        //Update of user object
        user.setEmail(new User.MyPair(edtMail.getText().toString(), email_status));
        //Update of the Authentication DB of Firebase + send a verification email at the new email
        FirebaseAuth.getInstance().getCurrentUser().updateEmail(user.getEmail().getValue());
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();

        //Update of the field mail in the user DB
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        dbref.child("users").child(user.getKey()).child("email").setValue(user.getEmail());
        FirebaseAuth.getInstance().getCurrentUser().reload();
        returnToEdit(true);
    }

    private boolean checkMailFormat() {
        if (clean_mail.isEmpty() || !user.checkMailFormat(clean_mail)) {
            edtMail.setError(getString(R.string.mail_not_valid));
            return false;
        }else if(user.getEmail().getValue().equals(clean_mail)){
            //No changes
            return false;
        }else{
            return true;
        }
    }

    private void tryUpdatePwd() {

        String pwd = edtPassword.getText().toString();

        //Now I need to re-authenticate the user

        LayoutInflater inflater=EditCredential.this.getLayoutInflater();
        //this is what I did to added the layout to the alert dialog
        final View layout=inflater.inflate(R.layout.my_alert_pwd,null);

        final AlertDialog dialog = new AlertDialog.Builder(EditCredential.this)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.editpwd))
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText edittext = (TextInputEditText) layout.findViewById(R.id.my_pwd_edit);
                        String current_pwd = edittext.getText().toString();
                        if(!current_pwd.isEmpty()) {
                            showProgress(true);
                            try {
                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().getValue(), current_pwd);
                                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //If the re-authetication is successful we can update the password
                                            updatePassword();
                                        }else{
                                            showProgress(false);
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditCredential.this, getString(R.string.error_auth_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

        //Used to allow the dialog to survive an orientation change
        keepDialog(dialog);


    }

    private void updatePassword() {
        //update of Authenication DB
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isComplete()) {
                    //Reload of the current user credential
                    FirebaseAuth.getInstance().getCurrentUser().reload();
                    Toast.makeText(EditCredential.this, getString(R.string.update_pwd),
                            Toast.LENGTH_LONG).show();
                    returnToEdit(false);
                }else {
                    showProgress(false);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditCredential.this, getString(R.string.update_pwd_fail),
                        Toast.LENGTH_LONG).show();
                Log.d("updatePswFail",e.getMessage());
            }
        });
    }

    private void returnToEdit(Boolean isUserMailChanged) {

        Bundle bundle = new Bundle();
        if(isUserMailChanged==true){
            //The user has update the email (value || status)
            bundle.putParcelable("mail", new User.MyPair(user.getEmail()));
        }else{
            //The user has update the password
            bundle.putString("mail",null);
        }


        Intent intent = new Intent();
        intent.putExtras(bundle);
        showProgress(false);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private static void keepDialog(Dialog dialog){
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

    }

    private void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        container.setVisibility(show ? View.GONE : View.VISIBLE);
    }

}





