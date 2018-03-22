package it.polito.mad.booksharing;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class EditProfile extends AppCompatActivity {

    //All declarations
    Toolbar toolbar;
    MaterialEditText edtName, edtSurname, edtCity, edtCap, edtStreet, edtPhone, edtMail;
    TextInputEditText edtDescription;
    ImageButton btnStreet, btnDone, btnEditImg;
    ImageView profileImg;
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
        user = getUserInfo();
        //Set all the fields of the user in edtName, edtSurname...
        setUser(user);


        swMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swMail.isChecked()){
                    user.setCheckMail("public");
                    edtMail.setTextColor(Color.BLACK);
                    edtMail.setUnderlineColor(Color.BLACK);
                }
                else{
                    user.setCheckMail("private");
                    edtMail.setTextColor(Color.parseColor("#A2A0A0"));
                    edtMail.setUnderlineColor(Color.parseColor("#A2A0A0"));
                }
            }
        });

        swStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swStreet.isChecked()){
                    user.setCheckStreet("public");
                    edtStreet.setTextColor(Color.BLACK);
                    edtStreet.setUnderlineColor(Color.BLACK);
                }
                else{
                    user.setCheckStreet("private");
                    edtStreet.setTextColor(Color.parseColor("#A2A0A0"));
                    edtStreet.setUnderlineColor(Color.parseColor("#A2A0A0"));
                }
            }
        });


        swPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swPhone.isChecked()){
                    user.setCheckPhone("public");
                    edtPhone.setTextColor(Color.BLACK);
                    edtPhone.setUnderlineColor(Color.BLACK);
                }
                else{
                    user.setCheckPhone("private");
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
                user.setName(new Pair<>(edtName.getText().toString(),user.getName().second));


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
                user.setSurname(new Pair<>(edtSurname.getText().toString(),user.getSurname().second));
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
                user.setEmail(new Pair<>(edtMail.getText().toString(),user.getEmail().second));
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
                user.setCity(new Pair<>(edtCity.getText().toString(),user.getCity().second));
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
                user.setPhone(new Pair<>(edtPhone.getText().toString(),user.getPhone().second));
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
                user.setCap(new Pair<>(edtCap.getText().toString(),user.getCap().second));
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
                user.setStreet(new Pair<>(edtStreet.getText().toString(),user.getStreet().second));
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
                user.setDescription(new Pair<>(edtDescription.getText().toString(),user.getDescription().second));
            }
        });

        //Catch when the button "done" is pressed
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create a new intent in order to restore the activity ShowProfile.
                //I put in a bundle the object user, with all the modification that the user have done previously
                setUserInfo(user);
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
                    if(takePicture.resolveActivity(getPackageManager())!=null) {

                        startActivityForResult(takePicture, IMAGE_CAMERA);//zero can be replaced with any action code
                    }
                }
                else if(activity == IMAGE_GALLERY){
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
        if(resultCode == RESULT_OK ){
            if(requestCode == IMAGE_GALLERY){

                //return null, I don't know why
                Uri pictureUri = data.getData();

                profileImg.setImageURI(pictureUri);

                user.setUri(pictureUri);



            } else if(requestCode == IMAGE_CAMERA){

                Bundle extra = data.getExtras();
                Bitmap image = (Bitmap) extra.get("data");
                saveToInternalStorage(image);
                Bitmap cropedimage = (Bitmap) Bitmap.createBitmap(image,0,0,image.getWidth(),image.getHeight()-100);

                profileImg.setImageBitmap(cropedimage);
            }
        }
    }

    private void setUser(User user){

        edtName.setText(user.getName().first);
        edtSurname.setText(user.getSurname().first);
        edtCity.setText(user.getCity().first);
        edtCap.setText(user.getCap().first);
        edtStreet.setText(user.getStreet().first);
        edtPhone.setText(user.getPhone().first);
        edtMail.setText(user.getEmail().first);
        edtDescription.setText(user.getDescription().first);


        if(user.checkMail()){
            swMail.setChecked(true);
        }
        else{
            swMail.setChecked(false);
        }

        if(user.checkPhone()){
            swPhone.setChecked(true);
        }
        else{
            swPhone.setChecked(false);
        }

        if(user.checkStreet()){
            swStreet.setChecked(true);
        }
        else{
            swStreet.setChecked(false);
        }

        Bitmap image =loadImageFromStorage();
        if(image!=null){
            profileImg.setImageBitmap(image);
        }
    }

    protected void setUserInfo(User user){

        if(user.getUri()!=null){
            fromGallerytoStorage();
            user.setUri(null);
        }

        SharedPreferences sharedPref = getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        Gson json = new Gson();
        String toStore = json.toJson(user);
        edit.putString("user",toStore).commit();
        edit.commit();
    }

    private void fromGallerytoStorage() {

        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                user.getUri(), filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 2;
        Bitmap img = BitmapFactory.decodeFile(filePath, opt);
        saveToInternalStorage(img);
    }

    public User getUserInfo() {
        SharedPreferences sharedPref = getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        if (userName.equals(defaultString)){
            return new User();
        }
        Gson json = new Gson();
        return json.fromJson(userName, User.class);
    }


    private String saveToInternalStorage(Bitmap bitmapImage){


        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        if(!directory.exists()){
            directory.mkdir();
        }

        File mypath=new File(directory,"profile.png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            user.setImagePath(new String(directory + "/profile.png"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    private Bitmap loadImageFromStorage()
    {
        Bitmap image=null;

        if(user.getImagePath()!=null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
        }
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(EditProfile.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}