package it.polito.mad.booksharing;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AlertDialogLayout;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AddBook extends Activity {

    private String photoName;
    private ImageButton btnDone, btnDelete;
    private ImageView btnScan;
    private EditText tvTitle, tvAuthor, tvYear, tvProduction, tvDescription;
    private ImageView myImageBook;
    private String urlImageBook, urlMyImageBook, isbn10, isbn13;
    final static int SCAN_CODE = 2, IMAGE_GALLERY = 0, IMAGE_CAMERA = 1;
    private Uri imageCameraUri;
    private String imageCameraPath;
    private File photoStorage;
    private String pathMyImageBook;
    private RatingBar ratingBar;
    private Book book;
    private String key;
    private boolean edit;
    private String uploadDate;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState); // the UI component values are saved here.
        outState.putParcelable("book", book);
        outState.putString("path", pathMyImageBook);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        book = inState.getParcelable("book");
        pathMyImageBook = inState.getString("path");

        /*ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        //If the directory where I want to save the image does not exist I create it
        if (!directory.exists()) {
            directory.mkdir();
        }

        Bitmap bitmapImage = null;
        //Create of the destination path
        pathMyImageBook = new String(directory + "/book.jpeg");


        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        opt.inSampleSize = calculateInSampleSize(opt, 512, 512);
        opt.inJustDecodeBounds = false;
        Bitmap img = BitmapFactory.decodeFile(pathMyImageBook, opt);
        if(img!=null && !edit){
            myImageBook.setImageBitmap(img);
            myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
        }*/

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_book);
        btnScan = (ImageView) findViewById(R.id.btnScan);
        btnDone = (ImageButton) findViewById(R.id.btnDone);
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        tvAuthor = (EditText) findViewById(R.id.tvAuthor);
        tvTitle = (EditText) findViewById(R.id.tvTitle);
        tvProduction = (EditText) findViewById(R.id.tvProduction);
        tvDescription = (EditText) findViewById(R.id.tvDescription);
        tvYear = (EditText) findViewById(R.id.tvYear);
        myImageBook = (ImageView) findViewById(R.id.myImageBook);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        urlImageBook = new String("");
        pathMyImageBook = "";

        edit = getIntent().getBooleanExtra("edit", false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (!edit) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
            key = getIntent().getExtras().getString("key");
            book = getIntent().getParcelableExtra("book");
            tvTitle.setText(book.getTitle());
            tvAuthor.setText(book.getAuthor());
            tvYear.setText(book.getYear());
            tvDescription.setText(book.getDescription());
            ratingBar.setRating(new Float(book.getRating()));
            tvProduction.setText(book.getPublisher());
            urlImageBook = new String(book.getUrlImage());
            urlMyImageBook = new String(book.getUrlMyImage());
            myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
            uploadDate = book.getDate();


            Picasso.with(AddBook.this).load(urlMyImageBook).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(myImageBook, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
                }

                @Override
                public void onError() {

                }


            });
        }


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence chooses[] = new CharSequence[]{"Yes", "No"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddBook.this);
                builder.setTitle("Sei sicuro di voler cancellare il libro?");
                builder.setItems(chooses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choose) {
                        Log.d("Choose", choose + "");
                        if (choose == 0) {
                            //User is sure, i must delete all
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUrlMyImage());
                            storageReference.delete();
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = firebaseDatabase.getReference("books/" + key);
                            databaseReference.removeValue();
                            finish();

                        }
                    }
                });
                builder.show();

            }
        });

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start the new activity
                Intent intent = new Intent(AddBook.this, CameraScan.class);
                startActivityForResult(intent, SCAN_CODE);
            }
        });


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvAuthor.getText().toString().isEmpty() && !tvTitle.getText().toString().isEmpty() && !tvYear.getText().toString().isEmpty() && !urlImageBook.isEmpty() && !tvDescription.getText().toString().isEmpty() && !tvProduction.getText().toString().isEmpty() && (!pathMyImageBook.isEmpty() || edit)) {
                    if (urlImageBook.isEmpty()) {
                        urlImageBook = urlMyImageBook;
                    }
                    if (edit) {
                        reloadDatabase(new Book(tvTitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, urlMyImageBook, "Sergio", book.getIsbn10(), book.getIsbn13(), Float.toString(ratingBar.getRating())));
                    } else {
                        uploadDatabase(new Book(tvTitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, "", "Sergio", isbn10, isbn13, Float.toString(ratingBar.getRating())));
                    }
                    finish();
                } else {
                    Toast.makeText(AddBook.this, "Attenzione: compilare tutti i campi", Toast.LENGTH_SHORT).show();
                }

            }
        });

        myImageBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_CODE) {
            if (resultCode == RESULT_OK) {
                Book book = intent.getParcelableExtra("book");
                String title = book.getTitle();
                String author = book.getAuthor();
                String year = book.getYear();
                String urlImage = book.getUrlImage();
                String publisher = book.getPublisher();
                isbn10 = book.getIsbn10();
                isbn13 = book.getIsbn13();
                if (title != null) {
                    tvTitle.setText(title);
                }
                if (author != null) {
                    tvAuthor.setText(author);
                }
                if (year != null) {
                    tvYear.setText(year);
                }
                if (urlImage != null) {
                    urlImageBook = urlImage;
                }
                if (publisher != null) {
                    tvProduction.setText(publisher);
                }
            } else {
                Toast.makeText(AddBook.this, "Attenzione: non è stato possibile precompilare i form.", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == IMAGE_GALLERY && resultCode == RESULT_OK) {
            Uri pictureUri = intent.getData();
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
            myImageBook.setImageBitmap(rotateImg);
            myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
            saveToInternalStorage(rotateImg);

        } else if (requestCode == IMAGE_CAMERA) {

            //The image is snapped from the camera
            String filePath = imageCameraPath;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opt);
            //Calculate inSampleSizef
            opt.inSampleSize = calculateInSampleSize(opt, 512, 512);
            opt.inJustDecodeBounds = false;
            Bitmap img = BitmapFactory.decodeFile(filePath, opt);
            //Because some version of andorid return the photos from the gallery with a strange orientation,
            //I rotate the bitmap in order to correct it.
            //So if the image will be return with 90°, i rotate and carry it to 0°
            Bitmap rotateImg = rotateBitmap(getOrientation(filePath), img);
            myImageBook.setImageBitmap(rotateImg);
            myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
            saveToInternalStorage(rotateImg);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(photoStorage)));
        }
    }

    //This method will save a bitmap inside the Internal Storage of the application
    private String saveToInternalStorage(Bitmap bitmapImage) {

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        //If the directory where I want to save the image does not exist I create it
        if (!directory.exists()) {
            directory.mkdir();
        }

        //Create of the destination path
        File mypath = new File(directory, "book.jpeg");

        FileOutputStream fos = null;
        //Copy of the file
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 10, fos);
            fos.close();
            pathMyImageBook = new String(directory + "/book.jpeg");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return directory.getAbsolutePath();
    }

    public void reloadDatabase(Book book) {
        final Book bookToUpload = book;
        //Replace with the real upload date
        bookToUpload.setDate(uploadDate);
        //Replace the image file
        if (!pathMyImageBook.isEmpty()) {

            Uri file = Uri.fromFile(new File(pathMyImageBook));
            StorageReference riversRef = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUrlMyImage());
            riversRef.putFile(file);
        }
        //Replace the content of the database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("books/" + key);
        databaseReference.setValue(bookToUpload);
    }

    public void uploadDatabase(Book book) {
        final Book bookToUpload = book;
        //Upload the image
        Uri file = Uri.fromFile(new File(pathMyImageBook));
        //Create a storage reference from our app
        StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("imagesMyBooks/" + photoName);
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
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                urlMyImageBook = downloadUrl.toString();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("books");
                bookToUpload.setUrlMyImage(urlMyImageBook);
                DatabaseReference instanceReference = databaseReference.push();
                instanceReference.setValue(bookToUpload);
            }
        });
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

    private void openGallery() {

        //Show a popup where the user can choose to pick a new image from the camera or from the gallery
        CharSequence chooses[] = new CharSequence[]{getString(R.string.gallery), getString(R.string.camera)};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddBook.this);
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
                        photoName = getResources().getString(R.string.app_name).replace(" ", "_") + "_" + currentDateTimeString + ".jpeg";
                        photoStorage = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/" + photoName);
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
                    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date()).replace(" ", "_").replace(":", "_");
                    photoName = getResources().getString(R.string.app_name).replace(" ", "_") + "_" + currentDateTimeString + ".jpeg";
                    //Take the image from the gallery
                    Intent takePicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(takePicture, IMAGE_GALLERY);//zero can be replaced with any action code
                }
            }
        });
        builder.show();


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


}


