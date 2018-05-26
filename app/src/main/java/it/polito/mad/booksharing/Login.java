package it.polito.mad.booksharing;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private Button button;
    private EditText loginEmail, loginPassword;
    private ProgressBar progress;
    private LinearLayout container;
    private TextView login_message, resend_pwd;
    private FirebaseUser user;
    private String fromActivity;
    private Spannable spannable;
    private MyNotificationManager nManager;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private FirebaseAuth mAuth;

    // UI references.
    private View mProgressView;
    private View mLoginFormView;
    private TextView resend_mail;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putString("mail", loginEmail.getText().toString());
        outState.putString("pass", loginPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        loginEmail.setText(inState.getString("mail"));
        loginPassword.setText(inState.getString("pass"));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        button = findViewById(R.id.LoginButton);
        loginEmail = findViewById(R.id.edtLoginName);
        loginPassword = findViewById(R.id.edtLoginPassword);
        progress = findViewById(R.id.login_progress);
        container = findViewById(R.id.LoginContainer);
        login_message = findViewById(R.id.login_message);
        resend_mail = findViewById(R.id.resend_mail);
        user = null;
        resend_pwd = findViewById(R.id.restore_psw);

        String message = getString(R.string.confirm_mail_msg);

        String sendMail = getString(R.string.tap_here_to_resend);

        spannable = new SpannableString(message + '\n' + sendMail);

        int colorAccent = ResourcesCompat.getColor(getResources(), R.color.colorAccent, null);

        //Change color to string send mail
        spannable.setSpan(new ForegroundColorSpan(colorAccent), message.length(),
                (message + '\n' + sendMail).length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        //Underline String send Mail
        spannable.setSpan(new UnderlineSpan(), message.length(),
                (message + '\n' + sendMail).length(), Spannable.SPAN_COMPOSING);

        resend_mail.setText(spannable);

        resend_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                Toast.makeText(Login.this,
                        getString(R.string.resend_mail) + " " + FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                        Toast.LENGTH_LONG).show();
            }
        });

        fromActivity = getIntent().getExtras().getString("from");
        if (fromActivity.equals("Edit")) {

            //The text became clickable only if we ae in the registration process
            //User not null, coming from registration process
            FirebaseAuth.getInstance().getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if (checkUserCredential(user)) {
                        startMain(user.getEmail());
                    }
                }
            });

            login_message.setVisibility(View.GONE);
            resend_mail.setVisibility(View.VISIBLE);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        resend_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mail = loginEmail.getText().toString().toLowerCase().replace(" ", "");
                if (!mail.isEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(mail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, getString(R.string.reset_mail),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Login.this, getString(R.string.reset_mail_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(Login.this, getString(R.string.insert_email),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        showProgress(false);
    }


    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(loginEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mAuth = FirebaseAuth.getInstance();
        loginEmail.setError(null);
        loginPassword.setError(null);

        // Store values at the time of the login attempt.
        final String email = loginEmail.getText().toString().toLowerCase().replace(" ", "");
        final String password = loginPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            loginPassword.setError(getString(R.string.error_invalid_password));
            focusView = loginPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            loginEmail.setError(getString(R.string.error_field_required));
            focusView = loginEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            loginEmail.setError(getString(R.string.mail_not_valid));
            focusView = loginEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            loginPassword.setError(getString(R.string.error_field_required));
            focusView = loginPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            //reload Firebase user
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
            showProgress(true);
            if (checkUserCredential(user)) {
                startMain(user.getEmail());
            } else {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        if (task.isSuccessful() && user.isEmailVerified()) {
                            startMain(user.getEmail());
                        } else if (!task.isSuccessful()) {
                            Toast.makeText(Login.this, getString(R.string.authentication_failed),
                                    Toast.LENGTH_SHORT).show();
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                            showProgress(false);
                        } else {
                            //reload user to check if it has verified e-mail
                            mAuth.getCurrentUser().reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    user = FirebaseAuth.getInstance().getCurrentUser();
                                    if (checkUserCredential(user)) {
                                        startMain(user.getEmail());
                                    } else {

                                        Toast.makeText(Login.this, getString(R.string.please_verify_email),
                                                Toast.LENGTH_LONG).show();
                                        login_message.setVisibility(View.GONE);
                                        resend_mail.setVisibility(View.VISIBLE);
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                        showProgress(false);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private boolean checkUserCredential(FirebaseUser currentUser) {
        return currentUser != null && currentUser.isEmailVerified();
    }

    private boolean isEmailValid(String email) {
        Pattern emailPatter = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcherMail = emailPatter.matcher(email);
        return matcherMail.find();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    private void startMain(String userEmail) {
        //Start MainPage Activity
        SharedPreferences sharedPreferences = getSharedPreferences("notificationPref", Context.MODE_PRIVATE);
        Integer messageCounter = sharedPreferences.getInt("messageCounter",-1);
        if(messageCounter== -1) {
            nManager = MyNotificationManager.getInstance(this);
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            ref.child("notificationCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("setCounterLogin","");
                    if(dataSnapshot.exists()) {
                        nManager.setMessageCounter(dataSnapshot.getValue(Integer.class));

                        ref.child("notificationMap").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){

                                    nManager.setMap((HashMap<String,Long>)dataSnapshot.getValue());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }else {
                        nManager.setMessageCounter(0);
                    }
                    Intent intent = new Intent(Login.this, MainPage.class);
                    setResult(Activity.RESULT_OK, intent);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            ref.child("pendingRequestCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        nManager.setPendingRequestCounter(dataSnapshot.getValue(Integer.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            ref.child("changeStatusRequestCounter").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        nManager.setChangeLendingStatusCounter(dataSnapshot.getValue(Integer.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return;
        }

        Intent intent = new Intent(Login.this, MainPage.class);
        setResult(Activity.RESULT_OK, intent);
        startActivity(intent);
        finish();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        container.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Login.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}
