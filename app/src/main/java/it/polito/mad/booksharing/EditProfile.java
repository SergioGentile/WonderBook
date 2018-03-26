package it.polito.mad.booksharing;


import android.Manifest;
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
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class EditProfile extends AppCompatActivity {

    //All declarations
    Toolbar toolbar;
    EditText edtName, edtSurname, edtCity, edtCap, edtStreet, edtPhone, edtMail, edtDescription;
    ImageButton btnDone, btnEditImg;
    ImageView profileImg, lockStreet, lockPhone, lockMail;
    Bitmap profileBitmap, originalBitmapNormal, originalBitmapCrop;
    Bundle extras;
    User user;
    Switch swPhone, swStreet, swMail;
    Uri imageCameraUri;

    private static final int IMAGE_GALLERY = 0, IMAGE_CAMERA = 1, IMAGE_CROP = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Start the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileBitmap = null;
        originalBitmapCrop = null;
        originalBitmapNormal = null;

        //Ask permission for editing photo
        ActivityCompat.requestPermissions(EditProfile.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);


        toolbar = (Toolbar)findViewById(R.id.toolbar);

        //It's useful to avoid showing keyboard when the activity start(By default keybard
        //is open automatically and the cursor is located in the first field)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Get all the references to the component
        edtName = (EditText) findViewById(R.id.edtName);
        edtSurname = (EditText) findViewById(R.id.edtSurname);
        edtCity = (EditText) findViewById(R.id.edtCity);
        edtCap = (EditText) findViewById(R.id.edtCap);
        edtStreet = (EditText) findViewById(R.id.edtStreet);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtMail = (EditText) findViewById(R.id.edtMail);
        edtDescription = (EditText) findViewById(R.id.description);
        btnDone = (ImageButton) findViewById(R.id.btnDone);
        btnEditImg = (ImageButton)findViewById(R.id.btnEditImg);
        profileImg = (ImageView) findViewById(R.id.profileImage);
        swPhone = (Switch) findViewById(R.id.swPhone);
        swStreet = (Switch) findViewById(R.id.swStreet);
        swMail = (Switch) findViewById(R.id.swMail);
        lockStreet = (ImageView)findViewById(R.id.lockStreet);
        lockPhone = (ImageView)findViewById(R.id.lockPhine);
        lockMail = (ImageView)findViewById(R.id.lockMail);
        //edtBirth = (MaterialEditText) findViewById(R.id.edtBirth);

        //Get the user object coming from the activity ShowProfile in order to initialize all the fields
        extras = getIntent().getExtras();
        user = getUserInfo();
        //Set all the fields of the user in edtName, edtSurname...
        setUser(user);

        setUpPictureAction();

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user.getImagePath() != null){
                    Intent intent = new Intent(EditProfile.this, Cropper.class);
                    intent.putExtra("user-path", user.getImagePath());
                    startActivityForResult(intent, IMAGE_CROP);
                }

            }
        });


        swMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swMail.isChecked()){
                    user.setCheckMail("public");
                    lockMail.setImageResource(R.drawable.ic_lock_open_black_24dp);
                }
                else{
                    user.setCheckMail("private");
                    lockMail.setImageResource(R.drawable.ic_lock_outline_black_24dp);
                }
            }
        });

        swStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swStreet.isChecked()){
                    user.setCheckStreet("public");
                    lockStreet.setImageResource(R.drawable.ic_lock_open_black_24dp);
                }
                else{
                    user.setCheckStreet("private");
                    lockStreet.setImageResource(R.drawable.ic_lock_outline_black_24dp);
                }
            }
        });


        swPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swPhone.isChecked()){
                    user.setCheckPhone("public");
                    lockPhone.setImageResource(R.drawable.ic_lock_open_black_24dp);
                }
                else{
                    user.setCheckPhone("private");
                    lockPhone.setImageResource(R.drawable.ic_lock_outline_black_24dp);
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
                String alertMessage  = user.checkInfo(getApplicationContext());
                if(alertMessage!=null){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditProfile.this);
                    alertDialog.setTitle(getString(R.string.alert_title))
                            .setMessage(alertMessage)
                            .setNeutralButton(getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Nothing to do
                                }
                            }).show();
                }else {
                    setUserInfo(user);
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user", user);
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(originalBitmapCrop!=null){
            saveToInternalStorageOriginalImage(originalBitmapCrop);
        }
        if(originalBitmapNormal!=null){
            saveToInternalStorage(originalBitmapNormal);
        }
    }

    private void setUpPictureAction() {

        if(user.getImagePath()==null){
            //First Access
            Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.profile);
            saveToInternalStorage(image);
            saveToInternalStorageOriginalImage(image);

        }

    }

    private void openGallery(){

        //Show a popup where the user can choose to pick a new image from the camera or from the gallery
        CharSequence chooses[] = new CharSequence[] {getString(R.string.gallery), getString(R.string.camera)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.uploadImage));
        builder.setItems(chooses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int activity) {
                //Depends on the result, i call a different activity
                if(activity == IMAGE_CAMERA){

                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if(takePicture.resolveActivity(getPackageManager())!=null) {
                        imageCameraUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/phototmp.jpeg"));
                        takePicture.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                imageCameraUri);
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


                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(
                        pictureUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds =true;
                BitmapFactory.decodeFile(filePath, opt);

                //Calculate inSampleSize
                opt.inSampleSize = calculateInSampleSize(opt,256,256);

                opt.inJustDecodeBounds = false;
                Bitmap img = BitmapFactory.decodeFile(filePath,opt);

                Bitmap rotateImg = rotateBitmap(getOrientation(filePath),img);
                profileImg.setImageBitmap(rotateImg);
                profileBitmap = rotateImg;
                saveToInternalStorageOriginalImage(rotateImg);

            } else if(requestCode == IMAGE_CAMERA){

                String filePath = imageCameraUri.getPath();

                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, opt);

                //Calculate inSampleSize
                opt.inSampleSize = calculateInSampleSize(opt, 256, 256);

                opt.inJustDecodeBounds = false;
                Bitmap img = BitmapFactory.decodeFile(filePath, opt);
                Bitmap rotateImg = rotateBitmap(getOrientation(filePath), img);

                profileImg.setImageBitmap(rotateImg);
                profileBitmap = rotateImg;
                saveToInternalStorageOriginalImage(rotateImg);
            }
            if (requestCode == IMAGE_CROP){
                Bitmap bitmap = BitmapFactory.decodeFile(user.getImagePath());
                profileImg.setImageBitmap(bitmap);
                profileBitmap = bitmap;
            }
        }

    }


    private int getOrientation(String path) {
        int orientationExif, orientation = 0;

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        orientationExif = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        if (ExifInterface.ORIENTATION_ROTATE_270 == orientationExif) {
            orientation = 270;
        }
        if (ExifInterface.ORIENTATION_ROTATE_180 == orientationExif) {
            orientation = 180;
        }
        if (ExifInterface.ORIENTATION_ROTATE_90 == orientationExif) {
            orientation = 90;
        }
        return orientation;
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
            lockMail.setImageResource(R.drawable.ic_lock_open_black_24dp);
        }
        else{
            swMail.setChecked(false);
            lockMail.setImageResource(R.drawable.ic_lock_outline_black_24dp);
        }

        if(user.checkPhone()){
            lockPhone.setImageResource(R.drawable.ic_lock_open_black_24dp);
            swPhone.setChecked(true);
        }
        else{
            lockPhone.setImageResource(R.drawable.ic_lock_outline_black_24dp);
            swPhone.setChecked(false);
        }

        if(user.checkStreet()){
            lockStreet.setImageResource(R.drawable.ic_lock_open_black_24dp);
            swStreet.setChecked(true);
        }
        else{
            lockStreet.setImageResource(R.drawable.ic_lock_outline_black_24dp);
            swStreet.setChecked(false);
        }

        Bitmap imageNormal = loadImageFromStorage();
        if (imageNormal != null) {
            originalBitmapNormal = imageNormal;
            profileImg.setImageBitmap(imageNormal);
        }

        Bitmap imageCropper = loadImageFromStorageOriginal();
        if (imageCropper != null) {
            originalBitmapCrop = imageCropper;
        }
    }

    protected void setUserInfo(User user){

        if(profileBitmap!=null){
            saveToInternalStorage(profileBitmap);
        }

        SharedPreferences sharedPref = getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        Gson json = new Gson();
        String toStore = json.toJson(user);
        edit.putString("user",toStore).commit();
        edit.commit();
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
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        if(!directory.exists()){
            directory.mkdir();
        }

        File mypath=new File(directory,User.profileImgName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            user.setImagePath(new String(directory + "/" + User.profileImgName));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    private String saveToInternalStorageOriginalImage(Bitmap bitmapImage){


        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        if(!directory.exists()){
            directory.mkdir();
        }

        File mypath=new File(directory,User.profileImgNameCrop);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
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

    private Bitmap loadImageFromStorageOriginal() {
        Bitmap image = null;
        if (user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath().replace("profile", "profile_cropper"));
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
                    Toast.makeText(EditProfile.this, R.string.permission_ext_storage_denied, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap rotateBitmap(int orientation, Bitmap source){
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        if(source != null){
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }
        return null;
    }

}
