package it.polito.mad.booksharing;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;


public class EditProfile extends AppCompatActivity {

    //All declarations
    Toolbar toolbar;
    MaterialEditText edtName, edtSurname, edtCity, edtCap, edtStreet, edtPhone, edtMail;
    TextInputEditText edtDescription;
    ImageButton btnStreet, btnDone, btnEditImg;
    ImageView profileImg;
    Uri imageUri;
    Bundle extras;
    User user;
    Switch swPhone, swStreet, swMail;

    private static final int IMAGE_GALLERY = 0, IMAGE_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Start the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);



        //Get the toolbar and set the title
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Book Sharing");

        //It's useful to avoid showing keyboard when the activity start(By default keybard
        //is open automatically and the cursor is located in the first field)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Get all the references to the component
        edtName = (MaterialEditText) findViewById(R.id.edtName);
        edtSurname = (MaterialEditText) findViewById(R.id.edtSurname);
        edtCity = (MaterialEditText) findViewById(R.id.edtCity);
        edtCap = (MaterialEditText) findViewById(R.id.edtCap);
        edtStreet = (MaterialEditText) findViewById(R.id.edtStreet);
        edtPhone = (MaterialEditText) findViewById(R.id.edtPhone);
        edtMail = (MaterialEditText) findViewById(R.id.edtMail);
        edtDescription = (TextInputEditText) findViewById(R.id.description);
        btnDone = (ImageButton) findViewById(R.id.btnDone);
        btnEditImg = (ImageButton)findViewById(R.id.btnEditImg);
        profileImg = (ImageView) findViewById(R.id.profileImage);
        swPhone = (Switch) findViewById(R.id.swPhone);
        swStreet = (Switch) findViewById(R.id.swStreet);
        swMail = (Switch) findViewById(R.id.swMail);

        //Get the user object coming from the activity ShowProfile in order to initialize all the fields
        extras = getIntent().getExtras();
        user = (User)extras.getParcelable("user");
        //Set all the fields of the user in edtName, edtSurname...
        setUser(user);


        swMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swMail.isChecked()){
                    user.setCheckMail(1);
                    edtMail.setTextColor(Color.BLACK);
                    edtMail.setUnderlineColor(Color.BLACK);
                }
                else{
                    user.setCheckMail(0);
                    edtMail.setTextColor(Color.parseColor("#A2A0A0"));
                    edtMail.setUnderlineColor(Color.parseColor("#A2A0A0"));
                }
            }
        });

        swStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swStreet.isChecked()){
                    user.setCheckStreet(1);
                    edtStreet.setTextColor(Color.BLACK);
                    edtStreet.setUnderlineColor(Color.BLACK);
                }
                else{
                    user.setCheckStreet(0);
                    edtStreet.setTextColor(Color.parseColor("#A2A0A0"));
                    edtStreet.setUnderlineColor(Color.parseColor("#A2A0A0"));
                }
            }
        });

        swPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swPhone.isChecked()){
                    user.setCheckPhone(1);
                    edtPhone.setTextColor(Color.BLACK);
                    edtPhone.setUnderlineColor(Color.BLACK);
                }
                else{
                    user.setCheckPhone(0);
                    edtPhone.setTextColor(Color.parseColor("#A2A0A0"));
                    edtPhone.setUnderlineColor(Color.parseColor("#A2A0A0"));
                }
            }
        });

        //The following listener are useful to understand when some modification on the text happens.
        //I created one listener for each field, in order to modify it and propagate the result to the activity "ShowProfile"
        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setName(edtName.getText().toString());
            }
        });


        edtSurname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setSurname(edtSurname.getText().toString());
            }
        });

        edtMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setEmail(edtMail.getText().toString());
            }
        });

        edtSurname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setSurname(edtSurname.getText().toString());
            }
        });

        edtCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setCity(edtCity.getText().toString());
            }
        });

        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setPhone(edtPhone.getText().toString());
            }
        });

        edtCap.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setCap(edtCap.getText().toString());
            }
        });

        edtStreet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setStreet(edtStreet.getText().toString());
            }
        });

        edtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                user.setDescription(edtDescription.getText().toString());
            }
        });

        //Catch when the button "done" is pressed
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create a new intent in order to restore the activity ShowProfile.
                //I put in a bundle the object user, with all the modification that the user have done previously
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        //Catch when the user would change the user image
        btnEditImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private void openGallery(){

        //Show a popup where the user can choose to pick a new image from the camera or from the gallery
        CharSequence chooses[] = new CharSequence[] {"Gallery", "Camera"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload image...");
        builder.setItems(chooses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int activity) {
                //Depends on the result, i call a different activity
                if(activity == IMAGE_CAMERA){
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(takePicture, IMAGE_CAMERA);//zero can be replaced with any action code
                }
                else if(IMAGE_GALLERY == activity){
                    Intent takePicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(takePicture, IMAGE_GALLERY);//zero can be replaced with any action code
                }
            }
        });
        builder.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        //Set a new image on the profile
        if(resultCode == RESULT_OK && (requestCode == IMAGE_GALLERY || requestCode == IMAGE_CAMERA)){
            //Now i take the image from the URI.
            //It must be changed because if the user delete the image from the gallery we loose the reference.
            imageUri = data.getData();
            profileImg.setImageURI(imageUri);
        }
    }

    private void setUser(User user){
        edtName.setText(user.getName());
        edtSurname.setText(user.getSurname());
        edtCity.setText(user.getCity());
        edtCap.setText(user.getCap());
        edtStreet.setText(user.getStreet());
        edtPhone.setText(user.getPhone());
        edtMail.setText(user.getEmail());
        edtDescription.setText(user.getDescription());

        if(user.isCheckMail()==1){
            swMail.setChecked(true);
        }
        else{
            swMail.setChecked(false);
        }

        if(user.isCheckPhone()==1){
            swPhone.setChecked(true);
        }
        else{
            swPhone.setChecked(false);
        }

        if(user.isCheckStreet()==1){
            swStreet.setChecked(true);
        }
        else{
            swStreet.setChecked(false);
        }
    }
}
