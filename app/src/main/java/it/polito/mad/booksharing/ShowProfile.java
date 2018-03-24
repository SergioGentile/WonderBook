package it.polito.mad.booksharing;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowProfile extends AppCompatActivity {
    private static final int MODIFY_PROFILE = 1;
    ImageButton btnModify;
    Toolbar toolbar;
    TextView tvDescription, tvName, tvStreet, tvPhone, tvMail;
    User user;
    LinearLayout llParent, llPhone, llMail, llDescription;
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

        llMail = (LinearLayout)findViewById(R.id.llMail);
        llPhone = (LinearLayout)findViewById(R.id.llPhone);
        llParent = (LinearLayout)findViewById(R.id.llParent);
        llDescription = (LinearLayout)findViewById(R.id.llDescription);

        toolbar.setTitle("Book Sharing");
        toolbar.setTitleTextColor(Color.WHITE);
        //Initialize the user (must be removed an replace with data stored previously)

        getUserInfo();

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
                getUserInfo();
                setUser(user);
            }
        }
    }

    private void setUser(User user){

        tvName.setText(user.getName().first + " " +  user.getSurname().first);

        tvPhone.setText(user.getPhone().first);
        tvMail.setText(user.getEmail().first);
        tvDescription.setText(user.getDescription().first);

        if(user.checkStreet() && !user.getStreet().first.equals("")){
            tvStreet.setText(user.getStreet().first + " (" + user.getCity().first+")");
        }
        else{
            tvStreet.setText(user.getCity().first);
        }

        if (!user.checkPhone() || user.getPhone().first.equals("") ) {
            llPhone.setVisibility(View.GONE);
        }
        else{
            llPhone.setVisibility(View.VISIBLE);
        }

        if (!user.checkMail() || user.getEmail().first.equals("") ) {
            llMail.setVisibility(View.GONE);
        }
        else{
            llMail.setVisibility(View.VISIBLE);
        }

        showUserPictureProfile(user);
    }

    private void showUserPictureProfile(User user) {
        Bitmap image = null;

        if (user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
            CircleImageView circleImageView = (CircleImageView) findViewById(R.id.profileImage);
            circleImageView.setImageBitmap(image);
        }
    }

    protected void getUserInfo(){
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

            String defaultString = "";
            String userName = sharedPref.getString("user", defaultString);
            if (userName.equals(defaultString)){
                user =  new User();
                return;
            }
            Gson json = new Gson();
            user= json.fromJson(userName, User.class);
            if(user.getDescription().first.equals("")){

                user.setDescription(new Pair<>(getString(R.string.description_value),"public"));
            }
        }
}
