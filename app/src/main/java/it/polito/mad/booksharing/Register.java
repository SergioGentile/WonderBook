package it.polito.mad.booksharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity {

    Button btnRegister;
    EditText email;

    private String clean_email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnRegister = (Button) findViewById(R.id.buttonRegister);
        email = (EditText) findViewById(R.id.edtLoginName);


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clean_email = email.getText().toString().replace(" ","");
                check_email(clean_email);

                    //Start MainPage Activity


            }
        });
    }

    private void check_email(String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("users").orderByChild("email/value").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Boolean flag = false;
                    for (DataSnapshot bookSnap : dataSnapshot.getChildren()) {
                       flag=true;
                    }
                    if(flag==false){
                        goToEdit();
                    }else{

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Register.this);
                        alertDialog.setTitle(getString(R.string.alert_title))
                                .setMessage(R.string.registerMessage)
                                .setNeutralButton(getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Nothing to do
                                    }
                                }).show();
                    }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

}

    private void goToEdit() {


        User u = new User();
        u.setEmail(new User.MyPair(clean_email, "public"));
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users");
        DatabaseReference instanceReference = databaseReference.push();
        u.setKey(instanceReference.getKey().toString());
        instanceReference.setValue(u);
        Bundle bundle = new Bundle();
        Intent intent = new Intent(Register.this, EditProfile.class);
        bundle.putParcelable("user", u);
        bundle.putString("from", "Register");
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
