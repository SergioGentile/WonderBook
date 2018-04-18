package it.polito.mad.booksharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditCredential extends AppCompatActivity {

    Button btnOk;
    EditText edtMail,edtPassword;
    User user;
    private String fromActivity;

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
                Intent intent = new Intent();
                Bundle bundle = new Bundle();

                if(fromActivity.equals("Edit")){
                    user.setEmail(new User.MyPair(edtMail.getText().toString(),user.getEmail().getStatus()));
                    /* Remember to set on DB and save on sharedPref also Password*/
                    bundle.putString("mail", edtMail.getText().toString());
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                }
                finish();
            }
        });

    }

}



