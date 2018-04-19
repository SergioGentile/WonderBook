package it.polito.mad.booksharing;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Start extends AppCompatActivity {

    private Button btnRegister;
    private Button btnLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        btnRegister = (Button) findViewById(R.id.buttonRegister);
        btnLogin = (Button) findViewById(R.id.buttonLogin);
        mAuth = FirebaseAuth.getInstance();


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start Login Activity
                Intent intent = new Intent(Start.this, Login.class);
                Bundle bundle = new Bundle();
                bundle.putString("from","Start");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start Register Activity
                Intent intent = new Intent(Start.this, Register.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //Start Login Activity if logged in
        if (currentUser != null) {
            Intent intent = new Intent(Start.this, MainPage.class);
            startActivity(intent);
            finish();
        }
    }

}