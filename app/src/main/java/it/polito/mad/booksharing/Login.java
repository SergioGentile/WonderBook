package it.polito.mad.booksharing;

import android.*;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.icu.text.UnicodeSet;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private Button button;
    private EditText loginEmail, loginPassword;
    private ProgressBar progress;
    private LinearLayout container;
    private TextView login_message;
    private FirebaseUser user;
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private FirebaseAuth mAuth;

    // UI references.
    private View mProgressView;
    private View mLoginFormView;


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

        button = (Button) findViewById(R.id.LoginButton);
        loginEmail = (EditText) findViewById(R.id.edtLoginName);
        loginPassword = (EditText) findViewById(R.id.edtLoginPassword);
        progress = (ProgressBar) findViewById(R.id.login_progress);
        container = (LinearLayout) findViewById(R.id.LoginContainer);
        login_message = (TextView) findViewById(R.id.login_message);
        user = null;

        String fromActivity = getIntent().getExtras().getString("from");
        if (fromActivity.equals("Edit")) {

            //The text became clickable only if we ae in the registration process
            if (checkUserCredential()) {
                startMain(user.getEmail());
            }


            String message = getString(R.string.confirm_mail_msg);

            String sendMail = getString(R.string.tap_here_to_resend);
            
            Spannable spannable = new SpannableString(message + '\n' +sendMail);

            int colorAccent = ResourcesCompat.getColor(getResources(),R.color.colorAccent,null);

            //Change color to string send mail
            spannable.setSpan(new ForegroundColorSpan(colorAccent),message.length(),
                    (message + '\n' + sendMail).length(),Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            //Underline String send Mail
            spannable.setSpan(new UnderlineSpan(),message.length(),
                    (message + '\n' + sendMail).length(),Spannable.SPAN_COMPOSING);

            login_message.setText(spannable);

            login_message.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                    Toast.makeText(Login.this,
                            getString(R.string.resend_mail) + " " + FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Disable autoorientation otherwise during the process the activity ma be killed
                attemptLogin();
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
        String email = loginEmail.getText().toString().toLowerCase().replace(" ", "");
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
        if (TextUtils.isEmpty(email)) {
            loginEmail.setError(getString(R.string.error_field_required));
            focusView = loginEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            loginEmail.setError(getString(R.string.mail_not_valid));
            focusView = loginEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (checkUserCredential()) {
                startMain(user.getEmail());
            } else if (user == null) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                showProgress(true);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if(mAuth.getCurrentUser().isEmailVerified()){

                                        String clean_email = loginEmail.getText().toString().toLowerCase().replace(" ", "");
                                        startMain(clean_email);
                                    }
                                    else{
                                        Toast.makeText(Login.this, getString(R.string.please_verify_email),
                                                Toast.LENGTH_LONG).show();
                                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                        showProgress(false);
                                        mAuth.signOut();
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(Login.this, getString(R.string.authentication_failed),
                                            Toast.LENGTH_SHORT).show();
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                    showProgress(false);
                                }

                            }
                        });

            }

        }
    }

    private boolean checkUserCredential() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null){
            FirebaseAuth.getInstance().getCurrentUser().reload();
        }
        if (currentUser != null && currentUser.isEmailVerified()) {
            user = currentUser;
            return true;
        }else if(currentUser!=null && !currentUser.isEmailVerified()){
            Toast.makeText(Login.this, getString(R.string.please_verify_email),Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isEmailValid(String email) {
        Pattern emailPatter = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcherMail = emailPatter.matcher(email);
        return  matcherMail.find();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    private void startMain(String userEmail) {
        //Start MainPage Activity
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
