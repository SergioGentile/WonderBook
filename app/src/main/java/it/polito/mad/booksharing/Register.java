package it.polito.mad.booksharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private EditText loginEmail , loginPassword, loginConfirmPassword;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = (Button) findViewById(R.id.buttonRegister);
        loginEmail = (EditText) findViewById(R.id.edtLoginName);
        loginPassword = (EditText) findViewById(R.id.edtLoginPassword);
        loginConfirmPassword = (EditText)findViewById(R.id.edtLoginPassword2);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Start MainPage Activity
                User u = new User();
                u.setEmail(new User.MyPair(loginEmail.getText().toString(),"public"));
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("users");
                DatabaseReference instanceReference = databaseReference.push();
                u.setKey(instanceReference.getKey().toString());
                instanceReference.setValue(u);
                Bundle bundle = new Bundle();
                Intent intent = new Intent(Register.this, EditProfile.class);
                bundle.putParcelable("user", u);
                bundle.putString("from","Register");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
