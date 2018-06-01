package it.polito.mad.booksharing;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditEmail extends AppCompatActivity {

    Button btnEmail;
    EditText edtMail;
    User user;
    private String clean_mail;
    private Switch swMail;
    private String email_status;
    private ProgressBar progress;
    private LinearLayout container;

    private static void keepDialog(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set all the variable
        user = getIntent().getParcelableExtra("user");
        email_status = user.getEmail().getStatus();
        btnEmail = findViewById(R.id.confirm_new_email);
        edtMail = findViewById(R.id.changeMail);
        swMail = findViewById(R.id.email_switch);
        progress = findViewById(R.id.editCred_progress);
        container = findViewById(R.id.editCredContainer);
        //Set text value
        edtMail.setText(user.getEmail().getValue());
        //Need to set PasswordField


        if (user.getEmail().getStatus().equals("private")) {
            swMail.setChecked(false);
        }

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clean_mail = edtMail.getText().toString().toLowerCase().replace(" ", "");
                if (checkMailFormat()) {
                    //The mail is in a valid format
                    tryUpdateMail();
                } else if (!email_status.equals(user.getEmail().getStatus())) {
                    //update status only
                    user.setEmail(new User.MyPair(user.getEmail().getValue(), email_status));
                    returnToEdit(true);
                }
                showProgress(false);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        });

        swMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Verify the status of the field.
                //If it's public, it will become private, otherwise will become public.
                //In both cases, change also the state of the lock.
                if (swMail.isChecked()) {
                    email_status = "public";
                } else {
                    email_status = "private";
                }


            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putString("mail", edtMail.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        edtMail.setText(inState.getString("mail"));

    }

    private void tryUpdateMail() {

        //Now I need to re-authenticate the user

        LayoutInflater inflater = EditEmail.this.getLayoutInflater();
        //this is what I did to added the layout to the alert dialog
        final View layout = inflater.inflate(R.layout.my_alert_pwd, null);

        final AlertDialog dialog = new AlertDialog.Builder(EditEmail.this)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.editpwd))
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText edittext = layout.findViewById(R.id.my_pwd_edit);
                        String current_pwd = edittext.getText().toString();
                        if (!current_pwd.isEmpty()) {
                            //I need to re-authenticate the user using the current_pwd
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
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
                                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                            showProgress(false);
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditEmail.this, getString(R.string.authentication_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(EditEmail.this, getString(R.string.error_auth_failed), Toast.LENGTH_SHORT).show();
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
        if (user.getEmail().getStatus().equals("private")) {
            swMail.setChecked(false);
        } else {
            swMail.setChecked(true);
        }

        edtMail.setText(user.getEmail().getValue());


    }

    private void updateMail() {


        Toast.makeText(EditEmail.this, getString(R.string.changeMail),
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
        } else return !user.getEmail().getValue().equals(clean_mail);
    }

    private void returnToEdit(Boolean isUserMailChanged) {

        Bundle bundle = new Bundle();
        if (isUserMailChanged == true) {
            //The user has update the email (value || status)
            bundle.putParcelable("mail", new User.MyPair(user.getEmail()));
        } else {
            //The user has update the password
            bundle.putString("mail", null);
        }


        Intent intent = new Intent();
        intent.putExtras(bundle);
        showProgress(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        container.setVisibility(show ? View.GONE : View.VISIBLE);
    }

}





