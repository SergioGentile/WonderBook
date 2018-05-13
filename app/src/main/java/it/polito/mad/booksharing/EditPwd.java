package it.polito.mad.booksharing;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class EditPwd extends AppCompatActivity {

    Button btnPwd;
    EditText edtPassword;
    User user;
    private String fromActivity;
    private ProgressBar progress;
    private LinearLayout container;

    private static void keepDialog(AlertDialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(lp);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pwd);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set all the variable
        user = getIntent().getParcelableExtra("user");
        fromActivity = getIntent().getStringExtra("from");
        btnPwd = findViewById(R.id.confirm_new_password);
        edtPassword = findViewById(R.id.changePwd);
        progress = findViewById(R.id.editPwd_progress);
        container = findViewById(R.id.edtPwdContainer);


        btnPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String pwd = edtPassword.getText().toString();
                //If the new password isn't too weak I try to update it else I do nothing and notify the problem to the user
                if (pwd.length() > 5) {

                    tryUpdatePwd();

                } else {
                    edtPassword.setError(getString(R.string.weak_pwd), null);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putString("pass", edtPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        edtPassword.setText(inState.getString("pass"));

    }

    private void tryUpdatePwd() {

        String pwd = edtPassword.getText().toString();

        //Now I need to re-authenticate the user

        LayoutInflater inflater = EditPwd.this.getLayoutInflater();
        //this is what I did to added the layout to the alert dialog
        final View layout = inflater.inflate(R.layout.my_alert_pwd, null);

        final AlertDialog dialog = new AlertDialog.Builder(EditPwd.this)
                .setTitle(getString(R.string.alert_title))
                .setMessage(getString(R.string.editpwd))
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText edittext = layout.findViewById(R.id.my_pwd_edit);
                        String current_pwd = edittext.getText().toString();
                        if (!current_pwd.isEmpty() && !edtPassword.getText().toString().equals(current_pwd)) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                            showProgress(true);
                            try {
                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail().getValue(), current_pwd);
                                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            //If the re-authetication is successful we can update the password
                                            updatePassword();
                                        }
                                        showProgress(false);
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(EditPwd.this, getString(R.string.error_auth_failed),
                                                Toast.LENGTH_SHORT).show();
                                        showProgress(false);
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                    }
                                });

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        } else if (current_pwd.equals(edtPassword.getText().toString())) {
                            Toast.makeText(EditPwd.this, getString(R.string.use_new_pwd),
                                    Toast.LENGTH_SHORT).show();
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
                if (task.isComplete()) {
                    //Reload of the current user credential
                    FirebaseAuth.getInstance().getCurrentUser().reload();
                    Toast.makeText(EditPwd.this, getString(R.string.update_pwd),
                            Toast.LENGTH_LONG).show();
                    showProgress(false);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    finish();
                } else {
                    showProgress(false);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditPwd.this, getString(R.string.update_pwd_fail),
                        Toast.LENGTH_LONG).show();
                Log.d("updatePswFail", e.getMessage());
            }
        });
    }

    private void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        container.setVisibility(show ? View.GONE : View.VISIBLE);
    }

}
