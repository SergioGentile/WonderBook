package it.polito.mad.booksharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
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

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class Login extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private Button button;
    private EditText loginEmail, loginPassword;
    private ProgressBar progress;
    private LinearLayout container;
    private TextView sendMailText;
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

        sendMailText = (TextView) findViewById(R.id.resendEmail);

        String fromActivity = getIntent().getExtras().getString("from");
        if (fromActivity.equals("Edit")) {


            FirebaseAuth.getInstance().getCurrentUser().reload();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (checkUserCredential(user)) {
                startMain(user.getEmail());
            }
        }

            sendMailText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
                    Toast.makeText(Login.this,
                            R.string.resend_mail + " " + mAuth.getCurrentUser().getEmail(),
                            Toast.LENGTH_SHORT).show();
                }
            });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            loginEmail.setError(getString(R.string.error_invalid_email));
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
            FirebaseUser user=null;
            if(mAuth.getCurrentUser()!=null) {
                mAuth.getCurrentUser().reload();
                user = mAuth.getCurrentUser();
            }
            if (checkUserCredential(user)) {
                startMain(user.getEmail());
            } else if (user == null) {
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
                                        sendMailText.setVisibility(View.VISIBLE);
                                        Toast.makeText(Login.this, "Please verify your email address.",
                                                Toast.LENGTH_LONG).show();
                                        showProgress(false);
                                        mAuth.signOut();
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    showProgress(false);
                                }

                            }
                        });

            }

        }
    }

    private boolean checkUserCredential(FirebaseUser currentUser) {

        if (currentUser != null && !currentUser.isEmailVerified()) {
            // If sign in fails, display a message to the user.
            sendMailText.setVisibility(View.VISIBLE);
            Toast.makeText(Login.this, "Please verify your email address.",
                    Toast.LENGTH_LONG).show();
        } else if (currentUser != null && currentUser.isEmailVerified()) {
            return true;
        }

        return false;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void startMain(String userEmail) {
        //Start MainPage Activity
        Intent intent = new Intent(Login.this, MainPage.class);
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
