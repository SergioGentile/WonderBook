package it.polito.mad.booksharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    Button btnRegister;
    EditText email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = (Button) findViewById(R.id.buttonRegister);
        email = (EditText) findViewById(R.id.edtLoginName);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Start MainPage Activity
                User u = new User();
                u.setEmail(new User.MyPair(email.getText().toString(),"public"));
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("users");
                DatabaseReference instanceReference = databaseReference.push();
                u.setKey(instanceReference.getKey().toString());
                instanceReference.setValue(u);
                Bundle bundle = new Bundle();

                Intent intent = new Intent(Register.this, EditProfile.class);
                bundle.putParcelable("user", u);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }
}
