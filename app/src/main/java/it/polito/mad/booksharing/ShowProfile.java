package it.polito.mad.booksharing;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;



public class ShowProfile extends AppCompatActivity {
    private static final int MODIFY_PROFILE = 1;
    ImageButton btnModify;
    Toolbar toolbar;
    TextView tvDescription, tvName, tvStreet, tvPhone, tvMail;
    User user;
    LinearLayout llStreet, llParent, llPhone, llMail, llDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Start the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        //Take all the references to the fields
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        tvName = (TextView) findViewById(R.id.tvName);
        tvStreet = (TextView) findViewById(R.id.tvStreet);
        tvPhone = (TextView) findViewById(R.id.tvPhone);
        tvMail = (TextView) findViewById(R.id.tvMail);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        btnModify = (ImageButton) findViewById(R.id.btnModify);

        llStreet = (LinearLayout)findViewById(R.id.llStreet);
        llMail = (LinearLayout)findViewById(R.id.llMail);
        llPhone = (LinearLayout)findViewById(R.id.llPhone);
        llParent = (LinearLayout)findViewById(R.id.llParent);
        llDescription = (LinearLayout)findViewById(R.id.llDescription);

        toolbar.setTitle("Book Sharing");
        //Initialize the user (must be removed an replace with data stored previously)
        user = new User("Sergio", "Gentile", "3277984218", "sergiogentile@gmail.com", "Vivo a Torino e sono disponibile a scambi il pomeriggio. "
                +"Cerco persone con cui scambiare libri nella mia stessa zona. "
                + "Mi piacciono i libri fantasy e mi piacerebbe confrontarmi con altri utenti.", "Torino", "10125", "Via Galliari 30", 1, 1 ,1 );
        //Set the user
        setUser(user);


        //Catch when the button modify it's pressed
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Put the user in a bundle and send it to the activity EditProfile
                Intent intent = new Intent(ShowProfile.this, EditProfile.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                intent.putExtras(bundle);
                //The costant MODIFY_PROFILE is useful when onActivityResult will be called.
                //In this way we can understand that the activity that finish will be associate with that constant
                //(See later)
                startActivityForResult(intent, MODIFY_PROFILE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Here we understand that the activity that produce a result is the one associated with the constant MODIFY_PROFILE.
        //The activity return an user that contains all the modification done
        if (requestCode == MODIFY_PROFILE) {
            if(resultCode == Activity.RESULT_OK){
                Bundle result= data.getExtras();
                user = (User)result.getParcelable("user");
                setUser(user);
            }
        }
    }

    private void setUser(User user){

        tvName.setText(user.getName() + " " +  user.getSurname());
        tvStreet.setText(user.getStreet() + " (" + user.getCity()+")");
        tvPhone.setText(user.getPhone());
        tvMail.setText(user.getEmail());
        tvDescription.setText(user.getDescription());

        //Remove all
        if (llStreet.getVisibility() == View.VISIBLE) {
            llParent.removeView(llStreet);
        }
        if (llPhone.getVisibility() == View.VISIBLE) {
            llParent.removeView(llPhone);
        }
        if (llMail.getVisibility() == View.VISIBLE) {
            llParent.removeView(llMail);
        }
        if (llDescription.getVisibility() == View.VISIBLE) {
            llParent.removeView(llDescription);
        }

        //Add all in order
        if(user.isCheckStreet()==1){
            llParent.addView(llStreet);
        }
        if(user.isCheckPhone()==1){
            llParent.addView(llPhone);
        }
        if(user.isCheckMail()==1){
            llParent.addView(llMail);
        }
        llParent.addView(llDescription);
    }
}
