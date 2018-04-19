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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


public class EditProfile extends AppCompatActivity {

    //All declarations
    private EditText edtName, edtSurname, edtCity, edtCap, edtStreet, edtPhone, edtMail, edtDescription;
    private ImageButton btnDone, btnEditImg;
    private ImageView profileImg, lockStreet, lockPhone, lockMail;
    private Bitmap profileBitmap, originalBitmapNormal, originalBitmapCrop;
    private User user;
    private Switch swPhone, swStreet, swMail;
    private Uri imageCameraUri;
    private String imageCameraPath;
    private File photoStorage;
    private String fromActivity;
    private Toolbar toolbar;

    //This int are useful to distinguish the different activities managed on the function onActivityResult
    private static final int IMAGE_GALLERY = 0, IMAGE_CAMERA = 1, IMAGE_CROP = 2;
    private static final int MODIFY_CREDENTIALS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Start the activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        profileBitmap = null;//-profileBitmap: contain the current profile image
        originalBitmapCrop = null; //-contain the original image (cropped version )
        originalBitmapNormal = null; //-contain the original image (image without cropping)

        //Ask permission for editing photo
        ActivityCompat.requestPermissions(EditProfile.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA},
                1);


        //It's useful to avoid showing keyboard when the activity start(By default keybard
        //is open automatically and the cursor is located in the first field)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Get all the references to the component
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        edtName = (EditText) findViewById(R.id.edtName);
        edtSurname = (EditText) findViewById(R.id.edtSurname);
        edtCity = (EditText) findViewById(R.id.edtCity);
        edtCap = (EditText) findViewById(R.id.edtCap);
        edtStreet = (EditText) findViewById(R.id.edtStreet);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtMail = (EditText) findViewById(R.id.edtMail);
        edtDescription = (EditText) findViewById(R.id.description);
        btnDone = (ImageButton) findViewById(R.id.btnDone);
        btnEditImg = (ImageButton) findViewById(R.id.btnEditImg);
        profileImg = (ImageView) findViewById(R.id.profileImage);
        swPhone = (Switch) findViewById(R.id.swPhone);
        swStreet = (Switch) findViewById(R.id.swStreet);
        swMail = (Switch) findViewById(R.id.swMail);
        lockStreet = (ImageView) findViewById(R.id.lockStreet);
        lockPhone = (ImageView) findViewById(R.id.lockPhine);
        lockMail = (ImageView) findViewById(R.id.lockMail);

        fromActivity = getIntent().getStringExtra("from");
        user = getIntent().getParcelableExtra("user");


        if(fromActivity.equals("Register")){

        }
        //Set all the fields of the user in edtName, edtSurname...

        //On the first access it will set up the image to perform the crop operation
        setUpPictureAction();
        setUser(user);

        //catch the ACTION_DOWN event on the user image.
        //In this case a new activity (the one for cropping the image) will be launch
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.getImagePath() != null) {
                    Intent intent = new Intent(EditProfile.this, Cropper.class);
                    //Put the path of the image as extra
                    intent.putExtra("user-path", user.getImagePath());
                    startActivityForResult(intent, IMAGE_CROP);
                }

            }
        });


        //catch when the switch button related to the e-mail field is crushed
        swMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verify the status of the field.
                //If it's public, it will become private, otherwise will become public.
                //In both cases, change also the state of the lock.
                if (swMail.isChecked()) {
                    user.setCheckMail("public");
                    lockMail.setImageResource(R.drawable.ic_lock_open_black_24dp);
                } else {
                    user.setCheckMail("private");
                    lockMail.setImageResource(R.drawable.ic_lock_outline_black_24dp);
                }
            }
        });

        //catch when the switch button related to the street field is crushed
        swStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verify the status of the field.
                //If it's public, it will become private, otherwise will become public.
                //In both cases, change also the state of the lock.
                if (swStreet.isChecked()) {
                    user.setCheckStreet("public");
                    lockStreet.setImageResource(R.drawable.ic_lock_open_black_24dp);
                } else {
                    user.setCheckStreet("private");
                    lockStreet.setImageResource(R.drawable.ic_lock_outline_black_24dp);
                }
            }
        });


        //catch when the switch button related to the phone field is crushed
        swPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verify the status of the field.
                //If it's public, it will become private, otherwise will become public.
                //In both cases, change also the state of the lock.
                if (swPhone.isChecked()) {
                    user.setCheckPhone("public");
                    lockPhone.setImageResource(R.drawable.ic_lock_open_black_24dp);
                } else {
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
                user.setName(new User.MyPair(edtName.getText().toString(), user.getName().getStatus()));
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
                user.setSurname(new User.MyPair(edtSurname.getText().toString(), user.getSurname().getStatus()));
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
                user.setEmail(new User.MyPair(edtMail.getText().toString(), user.getEmail().getStatus()));
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
                user.setCity(new User.MyPair(edtCity.getText().toString(), user.getCity().getStatus()));
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
                user.setPhone(new User.MyPair(edtPhone.getText().toString(), user.getPhone().getStatus()));
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
                user.setCap(new User.MyPair(edtCap.getText().toString(), user.getCap().getStatus()));
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
                user.setStreet(new User.MyPair(edtStreet.getText().toString(), user.getStreet().getStatus()));
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
                user.setDescription(new User.MyPair(edtDescription.getText().toString(), user.getDescription().getStatus()));
            }
        });

        //Catch when the button "done" is pressed
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create a new intent in order to restore the activity ShowProfile.
                //I put in a bundle the object user, with all the modification that the user have done previously
                //But before, check if all the mandatory fields have been completed.
                //If some errors occurs, show an error message to the user that ask for the completion of all the fields
                String alertMessage = user.checkInfo(getApplicationContext());
                if (alertMessage != null) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditProfile.this);
                    alertDialog.setTitle(getString(R.string.alert_title))
                            .setMessage(alertMessage)
                            .setNeutralButton(getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Nothing to do
                                }
                            }).show();
                } else {
                    //Otherwise put the new status of the user in a bundle, and return it to the activity show profile
                    setUserInfo();
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user", user);
                    intent.putExtras(bundle);
                    setResult(Activity.RESULT_OK, intent);

                    if (fromActivity.equals("Register")) {

                        bundle.putString("from", "Edit");
                        Intent intent2 = new Intent(EditProfile.this, Login.class);
                        intent2.putExtras(bundle);
                        startActivity(intent2);
                    }

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

        String alertMessage = user.checkInfo(EditProfile.this);

        if (alertMessage != null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditProfile.this);
            alertDialog.setTitle(getString(R.string.alert_title))
                    .setMessage(alertMessage)
                    .setNeutralButton(getString(R.string.alert_button), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Nothing to do
                        }
                    }).show();
            return;
        }

        //If the back button will be presses, it means that
        //the user will want to cancel all changes made so far.
        super.onBackPressed();
        //For the data no modification are useful, instead it's necessary to restore
        //the two original image profile
        if (originalBitmapCrop != null) {

            String imagePath = saveToInternalStorageOriginalImage(originalBitmapCrop);
            saveOnFireBaseOriginalImage(imagePath);
        }
        if (originalBitmapNormal != null) {
            String imagePath = saveToInternalStorage(originalBitmapNormal);
            saveonFirebase(imagePath);

        }
    }

    private void setUpPictureAction() {
        if (user.getImagePath().equals("")) {
            //On the first access the default image is copied in the internal storage in order to perform the crop operation
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
            String imagePathUser = saveToInternalStorage(image);
            saveonFirebase(imagePathUser);
            user.setImagePath(imagePathUser);
            String imagePathOrigin = saveToInternalStorageOriginalImage(image);
            saveOnFireBaseOriginalImage(imagePathOrigin);

        }

    }

    //Open the gallery or the camera in order to modify the current image profile.
    private void openGallery() {

        //Show a popup where the user can choose to pick a new image from the camera or from the gallery
        CharSequence chooses[] = new CharSequence[]{getString(R.string.gallery), getString(R.string.camera)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.uploadImage));
        builder.setItems(chooses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int activity) {
                //Depends on the result, i call a different activity
                if (activity == IMAGE_CAMERA) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePicture.resolveActivity(getPackageManager()) != null) {
                        //I use this method because the simplest method of taking the uri alone doesn't work from android 7.0.
                        //Moreover the method to take the image from the bundle imply a low quality of the image.
                        //So i create a new image and take it when it will be taken.
                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date()).replace(" ", "_").replace(":", "_");
                        photoStorage = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + getResources().getString(R.string.app_name).replace(" ", "_") + "_" + currentDateTimeString + "." + User.COMPRESS_FORMAT_STR);
                        imageCameraPath = photoStorage.getAbsolutePath();
                        if (!photoStorage.exists()) {
                            try {
                                photoStorage.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        //Take the URI where the image will be stored.
                        imageCameraUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", photoStorage);
                        takePicture.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                imageCameraUri);
                        takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(takePicture, IMAGE_CAMERA);//zero can be replaced with any action code
                    }
                } else if (activity == IMAGE_GALLERY) {
                    //Take the image from the gallery
                    Intent takePicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(takePicture, IMAGE_GALLERY);//zero can be replaced with any action code
                }
            }
        });
        builder.show();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Set a new image on the profile
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY) {
                Uri pictureUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(
                        pictureUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                //In order to avoid to copy big images (eg. 2 Mb) inside the Internal Storage (slowing down the process)
                //we reduce the resolution of the selected image
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, opt);
                //Calculate inSampleSize
                opt.inSampleSize = calculateInSampleSize(opt, 512, 512);
                opt.inJustDecodeBounds = false;
                Bitmap img = BitmapFactory.decodeFile(filePath, opt);

                //Because some version of android return the photos from the gallery with a strange orientation,
                //I rotate the bitmap in order to correct it.
                //So if the image will be return with 90°, i rotate and carry it to 0°
                Bitmap rotateImg = rotateBitmap(getOrientation(filePath), img);
                profileImg.setImageBitmap(rotateImg);
                profileBitmap = rotateImg;
                String imagePath = saveToInternalStorage(rotateImg);
                saveonFirebase(imagePath);
                String imagePathOriginal = saveToInternalStorageOriginalImage(rotateImg);
                saveOnFireBaseOriginalImage(imagePathOriginal);

            } else if (requestCode == IMAGE_CAMERA) {

                //The image is snapped from the camera
                String filePath = imageCameraPath;
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath, opt);
                //Calculate inSampleSize
                opt.inSampleSize = calculateInSampleSize(opt, 512, 512);
                opt.inJustDecodeBounds = false;
                Bitmap img = BitmapFactory.decodeFile(filePath, opt);
                //Because some version of andorid return the photos from the gallery with a strange orientation,
                //I rotate the bitmap in order to correct it.
                //So if the image will be return with 90°, i rotate and carry it to 0°
                Bitmap rotateImg = rotateBitmap(getOrientation(filePath), img);
                profileImg.setImageBitmap(rotateImg);
                profileBitmap = rotateImg;
                String imagePath = saveToInternalStorage(rotateImg);
                saveonFirebase(imagePath);
                String imagePathOriginal = saveToInternalStorageOriginalImage(rotateImg);
                saveOnFireBaseOriginalImage(imagePathOriginal);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(photoStorage)));
            } else if (requestCode == IMAGE_CROP) {
                //Case when the image was cropped.
                //I take the new image and set it as user image
                Bitmap bitmap = BitmapFactory.decodeFile(user.getImagePath());
                profileImg.setImageBitmap(bitmap);
                profileBitmap = bitmap;
            } else if (requestCode == MODIFY_CREDENTIALS) {
                Bundle result = data.getExtras();
                String mail = result.getString("mail");
                User firstUser = getIntent().getParcelableExtra("user");
                firstUser.setEmail(new User.MyPair(mail, firstUser.getEmail().getStatus()));
                setSharedPrefUserInfo(firstUser);
                user.setEmail(new User.MyPair(mail, user.getEmail().getStatus()));
                edtMail.setText(mail);

            }
        }

    }


    //Get the orientation of the image specified on the path.
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

    //Take all the user information from User object and fill the screen with that information
    private void setUser(User user) {

        //Text information
        edtName.setText(user.getName().getValue());
        edtSurname.setText(user.getSurname().getValue());
        edtCity.setText(user.getCity().getValue());
        edtCap.setText(user.getCap().getValue());
        edtStreet.setText(user.getStreet().getValue());
        edtPhone.setText(user.getPhone().getValue());
        edtMail.setText(user.getEmail().getValue());
        edtDescription.setText(user.getDescription().getValue());

        //set the correct status of the lock and switch button
        if (user.checkMail()) {
            swMail.setChecked(true);
            lockMail.setImageResource(R.drawable.ic_lock_open_black_24dp);
        } else {
            swMail.setChecked(false);
            lockMail.setImageResource(R.drawable.ic_lock_outline_black_24dp);
        }

        if (user.checkPhone()) {
            lockPhone.setImageResource(R.drawable.ic_lock_open_black_24dp);
            swPhone.setChecked(true);
        } else {
            lockPhone.setImageResource(R.drawable.ic_lock_outline_black_24dp);
            swPhone.setChecked(false);
        }

        if (user.checkStreet()) {
            lockStreet.setImageResource(R.drawable.ic_lock_open_black_24dp);
            swStreet.setChecked(true);
        } else {
            lockStreet.setImageResource(R.drawable.ic_lock_outline_black_24dp);
            swStreet.setChecked(false);
        }

        //Upload the user image
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


    protected void setUserInfo() {


        if (profileBitmap != null) {
            //If I'm here the user has changed the image so I need to save it inside the Internal Storage
            String imagePath = saveToInternalStorage(profileBitmap);
            saveonFirebase(imagePath);
        }

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
        dbref.child("users").child(user.getKey()).setValue(user);

        setSharedPrefUserInfo(user);
    }

    private void setSharedPrefUserInfo(User u) {
        //I Create a new json object in order to store all the information contained inside my user object.
        //Then I save it inside the SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        Gson json = new Gson();
        String toStore = json.toJson(u);
        edit.putString("user", toStore).apply();
        edit.commit();
        setUser(u);


    }

    private void saveonFirebase(String imagePath) {

        Uri file = Uri.fromFile(new File(imagePath));
        //Create a storage reference from our app
        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("userImgProfile/" + user.getKey() + "/picture.jpg");
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

            }
        });
    }

    private void saveOnFireBaseOriginalImage(String imagePath) {


        Uri file = Uri.fromFile(new File(imagePath));
        //Create a storage reference from our app
        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("userImgProfile/" + user.getKey() + "/picture_Original.jpg");
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

            }
        });
    }


    private void getUserInfoFromShared() {
        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        if (userName.equals(defaultString)) {
            user = new User();
            return;
        }
        Gson json = new Gson();
        user = json.fromJson(userName, User.class);

    }


    //This method will save a bitmap inside the Internal Storage of the application
    private String saveToInternalStorage(Bitmap bitmapImage) {

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        //If the directory where I want to save the image does not exist I create it
        if (!directory.exists()) {
            directory.mkdir();
        }
        String imagePath = null;
        //Create of the destination path
        File mypath = new File(directory, User.profileImgName);

        FileOutputStream fos = null;
        //Copy of the file
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(User.COMPRESS_FORMAT_BIT, User.IMAGE_QUALITY, fos);
            fos.close();
            imagePath = new String(directory + "/" + User.profileImgName);
            user.setImagePath(imagePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }


    // In order to allow the user to modify the way in witch the image is cropped I need to save also
    //the original image
    private String saveToInternalStorageOriginalImage(Bitmap bitmapImage) {


        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File mypath = new File(directory, User.profileImgNameCrop);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(User.COMPRESS_FORMAT_BIT, User.IMAGE_QUALITY, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(directory + "/" + User.profileImgNameCrop);
    }

    //This method will load the bitmap saved inside the Internal Storage of the application
    private Bitmap loadImageFromStorage() {
        Bitmap image = null;

        if (user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
        }
        return image;
    }

    //This method will load the original bitmap saved inside the Internal Storage of the application
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

    //Calculate the parameter used for reduce the dimension and the resolution of the image
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

    //Rotate the bitmap source of the orientation specified as parameter
    private Bitmap rotateBitmap(int orientation, Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        if (source != null) {
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_credential, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(EditProfile.this, EditCredential.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            bundle.putString("from", "Edit");
            intent.putExtras(bundle);

            //The costant MODIFY_PROFILE is useful when onActivityResult will be called.
            //In this way we can understand that the activity that finish will be associate with that constant
            //(See later)
            startActivityForResult(intent, MODIFY_CREDENTIALS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}