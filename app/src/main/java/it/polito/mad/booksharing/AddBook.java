package it.polito.mad.booksharing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Switch;
import android.widget.TextView;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class AddBook extends Activity {

    private Switch swAvailable;
    private TextView tvAvailable;
    private String photoName;
    private ImageButton btnDone, btnDelete;
    private LinearLayout btnScan;
    private EditText tvTitle, tvAuthor, tvYear, tvProduction, tvDescription, tvSubtitle, tvISBN;
    private ImageView myImageBook;
    private String urlMyImageBook;
    final static int SCAN_CODE = 2, IMAGE_GALLERY = 0, IMAGE_CAMERA = 1;
    private Uri imageCameraUri;
    private String imageCameraPath;
    private File photoStorage;
    private String pathMyImageBook, urlImageBook;
    private RatingBar ratingBar;
    private Book book;
    private String key;
    private boolean edit;
    private String uploadDate;
    private User user;
    private ScrollView sv;
    ProgressDialog pd;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the book that the user is writing.
        //It's useful if the user rotate the screen
        String isbn10 = new String(tvISBN.getText().toString());
        String isbn13 = new String("");
        if(tvISBN.getText().toString().length()==13){
            isbn13 = tvISBN.getText().toString();
        }
        else if(book!=null && book.getIsbn13()!=null){
            isbn13 = book.getIsbn13();
        }
        if(tvISBN.getText().toString().length()==10){
            isbn10 = tvISBN.getText().toString();
        }
        else if(book!=null &&  book.getIsbn10()!=null){
            isbn10 = book.getIsbn10();
        }
        Book bookToSave = new Book(tvTitle.getText().toString(), tvSubtitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook == null ? "" : urlImageBook, urlMyImageBook == null ? "" : urlMyImageBook, user.getKey(), isbn10, isbn13, Float.toString(ratingBar.getRating()), swAvailable.isChecked());
        if (uploadDate != null) {
            bookToSave.setDate(uploadDate);
        }
        bookToSave.setAvailable(swAvailable.isChecked());

        //Put the book in the outState
        outState.putParcelable("book", bookToSave);
        //Save also the path of the image, that isnt present on the book object
        outState.putString("path", pathMyImageBook);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        book = inState.getParcelable("book");

        //Take the book stored before.
        //Set again all the lable
        tvTitle.setText(book.getTitle());
        tvSubtitle.setText(book.getSubtitle());
        tvAuthor.setText(book.getAuthor());
        tvYear.setText(book.getYear());

        String isbn = new String("");
        if(book.getIsbn13()!=null && !book.getIsbn13().isEmpty()){
            isbn = book.getIsbn13();
        }
        else if(book.getIsbn10()!=null && !book.getIsbn10().isEmpty()){
            isbn = book.getIsbn10();
        }
        tvISBN.setText(isbn);
        tvDescription.setText(book.getDescription());
        ratingBar.setRating(new Float(book.getRating()));
        tvProduction.setText(book.getPublisher());
        urlImageBook = new String(book.getUrlImage());
        urlMyImageBook = new String(book.getUrlMyImage());
        pathMyImageBook = inState.getString("path");
        uploadDate = book.getDate();

        if(book.isAvailable()){
            swAvailable.setChecked(true);
            tvAvailable.setTextColor(getColor(R.color.available));
            tvAvailable.setText(getString(R.string.available_upper));
        }
        else{
            swAvailable.setChecked(false);
            tvAvailable.setTextColor(getColor(R.color.unavailable));
            tvAvailable.setText(getString(R.string.unavailable_upper));
        }

        //If the image change (so the path isn't empty) load again the image path
        if (!pathMyImageBook.isEmpty()) {
            //Decode the image stored as a JPEG file
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            opt.inSampleSize = calculateInSampleSize(opt, 512, 512);
            opt.inJustDecodeBounds = false;
            Bitmap img = BitmapFactory.decodeFile(pathMyImageBook, opt);
            if (img != null) {
                myImageBook.setImageBitmap(img);
                myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        btnScan = (LinearLayout) findViewById(R.id.btnScan);
        btnDone = (ImageButton) findViewById(R.id.btnDone);
        btnDelete = (ImageButton) findViewById(R.id.btnDelete);
        tvAuthor = (EditText) findViewById(R.id.tvAuthor);
        tvTitle = (EditText) findViewById(R.id.tvTitle);
        tvISBN = (EditText) findViewById(R.id.tvISBN);
        tvSubtitle = (EditText) findViewById(R.id.tvSubtitle);
        tvProduction = (EditText) findViewById(R.id.tvProduction);
        tvDescription = (EditText) findViewById(R.id.tvDescription);
        sv = (ScrollView) findViewById(R.id.scrollAb);
        tvYear = (EditText) findViewById(R.id.tvYear);
        myImageBook = (ImageView) findViewById(R.id.myImageBook);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        swAvailable = (Switch) findViewById(R.id.swAvailable);
        tvAvailable = (TextView) findViewById(R.id.tvAvailable);
        urlImageBook = "";
        urlMyImageBook = "";

        //This part is useful when the description field over the max number of lines.
        //If the user scroll the description field, the scrollerView is blocked, and with the same principle
        //when the user scroll the scrollView the description field is blocked.
        tvDescription.setMovementMethod(new ScrollingMovementMethod());
        sv.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (tvDescription.getLineCount() >= tvDescription.getMaxLines()) {
                    tvDescription.getParent().requestDisallowInterceptTouchEvent(false);
                }

                return false;
            }
        });

        tvDescription.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (tvDescription.getLineCount() >= tvDescription.getMaxLines()) {
                    tvDescription.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        //Get the user
        user = getIntent().getExtras().getParcelable("user");
        if (savedInstanceState != null) {
            pathMyImageBook = savedInstanceState.getString("path", "");
        } else {
            pathMyImageBook = "";
        }

        //Edit show me if the user compile the form for the first time or modify some book that already exist.
        edit = getIntent().getBooleanExtra("edit", false);

        //Avoid focus on the first field
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (!edit) {
            //If the book doesn't exist yet, avoid the possibility to delete it.
            btnDelete.setVisibility(View.GONE);
        } else {
            //I'm in edit mode. I must get the book and the key to refer to firebase because
            //the book already exist.
            btnDelete.setVisibility(View.VISIBLE);
            key = getIntent().getExtras().getString("key");
            book = getIntent().getParcelableExtra("book");
            urlImageBook = new String(book.getUrlImage());
            urlMyImageBook = new String(book.getUrlMyImage());
            uploadDate = book.getDate();
            tvTitle.setText(book.getTitle());
            tvSubtitle.setText(book.getSubtitle());

            String isbn = new String("");
            if(book.getIsbn13()!=null && !book.getIsbn13().isEmpty()){
                isbn = book.getIsbn13();
            }
            else if(book.getIsbn10()!=null && !book.getIsbn10().isEmpty()){
                isbn = book.getIsbn10();
            }
            tvISBN.setText(isbn);
            tvAuthor.setText(book.getAuthor());
            tvYear.setText(book.getYear());
            tvDescription.setText(book.getDescription());
            ratingBar.setRating(new Float(book.getRating()));
            tvProduction.setText(book.getPublisher());

            if(book.isAvailable()){
                swAvailable.setChecked(true);
                tvAvailable.setTextColor(getColor(R.color.available));
                tvAvailable.setText(getString(R.string.available_upper));
            }
            else{
                swAvailable.setChecked(false);
                tvAvailable.setTextColor(getColor(R.color.unavailable));
                tvAvailable.setText(getString(R.string.unavailable_upper));
            }

            //If the user dosen't change the image(path is empty) load the old one present into the database.
            if (pathMyImageBook.isEmpty()) {
                Picasso.with(AddBook.this).load(urlMyImageBook).noFade().placeholder(R.drawable.progress_animation)
                        .error(R.drawable.ic_error_outline_black_24dp).into(myImageBook, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        myImageBook.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

                    @Override
                    public void onError() {
                        myImageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                });
            }
        }


        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Ask to the user if he is sure to delete the book.
                //If yes, i delete all.
                CharSequence chooses[] = new CharSequence[]{getString(R.string.yes), getString(R.string.no)};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddBook.this);
                builder.setTitle(getString(R.string.ask_to_delete_book));
                builder.setItems(chooses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choose) {
                        if (choose == 0) {
                            //User is sure, i must delete all
                            try {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUrlMyImage());
                                storageReference.delete();
                            } catch (Exception e) {
                                Log.w("AddBook", "Impossible to delete the file");
                            }
                            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                            DatabaseReference databaseReference = firebaseDatabase.getReference("books/" + key);
                            databaseReference.removeValue();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            //Notify to the showBookFull activity that the book was deleted, so do not show it again
                            intent.putExtra("cancelled", true);
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
                //Start the activity to fill all the editText
                Intent intent = new Intent(AddBook.this, CameraScan.class);
                startActivityForResult(intent, SCAN_CODE);
            }
        });

        swAvailable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                swAvailable.setChecked(isChecked);
                if(isChecked){
                    tvAvailable.setTextColor(getColor(R.color.available));
                    tvAvailable.setText(getString(R.string.available_upper));
                }
                else{
                    tvAvailable.setTextColor(getColor(R.color.unavailable));
                    tvAvailable.setText(getString(R.string.unavailable_upper));
                }
            }
        });

        //All it's done
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!tvAuthor.getText().toString().isEmpty() && !tvTitle.getText().toString().isEmpty() && !tvISBN.getText().toString().isEmpty() && !tvYear.getText().toString().isEmpty() && !tvDescription.getText().toString().isEmpty() && !tvProduction.getText().toString().isEmpty() && (!pathMyImageBook.isEmpty() || edit)) {
                    if (tvISBN.getText().toString().length() == 13 || tvISBN.getText().toString().length() == 10) {
                        if (edit) {
                            //if we are in the edit mode, reload the information
                            String isbn10 = new String("");
                            String isbn13 = new String("");
                            if(tvISBN.getText().toString().length()==13){
                                isbn13 = tvISBN.getText().toString();
                            }
                            else if(book.getIsbn13()!=null){
                                isbn13 = book.getIsbn13();
                            }
                            if(tvISBN.getText().toString().length()==10){
                                isbn10 = tvISBN.getText().toString();
                            }
                            else if(book.getIsbn10()!=null){
                                isbn10 = book.getIsbn10();
                            }
                            reloadDatabase(new Book(tvTitle.getText().toString(), tvSubtitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, urlMyImageBook, user.getKey(), isbn10, isbn13, Float.toString(ratingBar.getRating()), swAvailable.isChecked()));
                        } else {
                            //Otherwise it's the first time
                            String isbn10 = new String("");
                            String isbn13 = new String("");
                            if(tvISBN.getText().toString().length()==13){
                                isbn13 = tvISBN.getText().toString();
                            }
                            else if(book.getIsbn13()!=null){
                                isbn13 = book.getIsbn13();
                            }
                            if(tvISBN.getText().toString().length()==10){
                                isbn10 = tvISBN.getText().toString();
                            }
                            else if(book.getIsbn10()!=null){
                                isbn10 = book.getIsbn10();
                            }
                            uploadDatabase(new Book(tvTitle.getText().toString(), tvSubtitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, "", user.getKey(), isbn10, isbn13, Float.toString(ratingBar.getRating()), swAvailable.isChecked()));
                        }
                    } else {
                        Toast.makeText(AddBook.this, getString(R.string.wrong_isbn), Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(AddBook.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                }

            }
        });

        myImageBook.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SCAN_CODE) {
            //Flush all the field
            tvISBN.setText("");
            tvTitle.setText("");
            tvSubtitle.setText("");
            tvAuthor.setText("");
            tvYear.setText("");
            urlImageBook = "";
            tvProduction.setText("");
            tvAvailable.setText(getString(R.string.available_upper));
            tvAvailable.setTextColor(getColor(R.color.available));
            swAvailable.setChecked(true);

            if (resultCode == RESULT_OK) {
                List<Book> books = intent.getParcelableArrayListExtra("books");
                //Only one book for one isbn. So if the result is RESULT_OK, by sure there is a book on the list
                book = books.get(0);
                String title = book.getTitle();
                String subtitle = book.getSubtitle();
                String author = book.getAuthor();
                String year = book.getYear();
                String urlImage = book.getUrlImage();
                String publisher = book.getPublisher();
                String isbn = new String("");
                if(book.getIsbn13()!=null && !book.getIsbn13().isEmpty()){
                    isbn = book.getIsbn13();
                }
                else if(book.getIsbn10()!=null && !book.getIsbn10().isEmpty()){
                    isbn = book.getIsbn10();
                }
                tvISBN.setText(isbn);
                tvTitle.setText(title);
                tvSubtitle.setText(subtitle);
                tvAuthor.setText(author);
                tvYear.setText(year);
                urlImageBook = urlImage;
                tvProduction.setText(publisher);
                if(book.isAvailable()){
                    swAvailable.setChecked(true);
                    tvAvailable.setTextColor(getColor(R.color.available));
                    tvAvailable.setText(getString(R.string.available_upper));
                }
                else{
                    tvAvailable.setTextColor(getColor(R.color.unavailable));
                    swAvailable.setChecked(false);
                    tvAvailable.setText(getString(R.string.unavailable_upper));
                }

            } else {
                //Here check for only the isbn. So fill the field ISBN on the view even if i didn't find the book online
                if (intent.getStringExtra("isbn") != null) {
                    tvISBN.setText(intent.getExtras().getString("isbn"));
                }
                Toast.makeText(AddBook.this, getString(R.string.fill_not_possible), Toast.LENGTH_SHORT).show();
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

        } else if (requestCode == IMAGE_CAMERA && resultCode == RESULT_OK) {

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


    //Re-Load an existing book, for example because the user change something such as the title, author and so on
    public void reloadDatabase(Book book) {
        //Instatiate the book as final so that it can be used in the anonymous methods.
        final Book bookToUpload = book;
        //Set the progress bar (stop it when the upload end)
        pd = new ProgressDialog(AddBook.this);
        pd.setMessage(getString(R.string.wait));
        pd.setCancelable(false);
        pd.show();
        //Replace with the real upload date
        bookToUpload.setDate(uploadDate);
        //Replace the image file.

        if (!pathMyImageBook.isEmpty()) {
            //If pathMyImageBook isn't empty, it means that the image was changed by the user.
            //So reload it again and delete the previous one (if the upload of the new one will be successful).
            Uri file = Uri.fromFile(new File(pathMyImageBook));
            StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("imagesMyBooks/" + photoName);
            UploadTask uploadTask = riversRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    //If it's not possible
                    //Replace the content of the database
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("books/" + key);
                    databaseReference.setValue(bookToUpload);

                    Intent intent = new Intent();
                    intent.putExtra("modified", true);
                    Bundle bundle = new Bundle();
                    String isbn10 = new String("");
                    String isbn13 = new String("");
                    if(tvISBN.getText().toString().length()==13){
                        isbn13 = tvISBN.getText().toString();
                    }
                    else {
                        isbn13 = bookToUpload.getIsbn13();
                    }
                    if(tvISBN.getText().toString().length()==10){
                        isbn10 = tvISBN.getText().toString();
                    }
                    else{
                        isbn10 = bookToUpload.getIsbn10();
                    }
                    bundle.putParcelable("book", new Book(tvTitle.getText().toString(), tvSubtitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, urlMyImageBook, user.getKey(), isbn10, isbn13, Float.toString(ratingBar.getRating()), swAvailable.isChecked()) );
                    intent.putExtras(bundle);
                    setResult(RESULT_CANCELED, intent);
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    finish();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    //Replace the content of the database
                    try {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookToUpload.getUrlMyImage());
                        storageReference.delete();
                    } catch (Exception e) {
                        Log.w("AddBook", "Impossible to delete the file");
                    }


                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    urlMyImageBook = downloadUrl.toString();
                    bookToUpload.setUrlMyImage(urlMyImageBook);

                    //Put the book into the intent
                    Intent intent = new Intent();
                    intent.putExtra("modified", true);
                    Bundle bundle = new Bundle();
                    String isbn10 = new String("");
                    String isbn13 = new String("");
                    if(tvISBN.getText().toString().length()==13){
                        isbn13 = tvISBN.getText().toString();
                    }
                    else {
                        isbn13 = bookToUpload.getIsbn13();
                    }
                    if(tvISBN.getText().toString().length()==10){
                        isbn10 = tvISBN.getText().toString();
                    }
                    else{
                        isbn10 = bookToUpload.getIsbn10();
                    }
                    bundle.putParcelable("book", new Book(tvTitle.getText().toString(), tvSubtitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, urlMyImageBook, user.getKey(), isbn10, isbn13, Float.toString(ratingBar.getRating()), swAvailable.isChecked()));
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    //Upload with the new settings
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("books/" + key);
                    databaseReference.setValue(bookToUpload);
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    finish();
                }
            });


        } else {
            //Replace the content of the database, the image doesn't change.
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference("books/" + key);
            databaseReference.setValue(bookToUpload);

            Intent intent = new Intent();
            intent.putExtra("modified", true);
            Bundle bundle = new Bundle();
            String isbn10 = new String("");
            String isbn13 = new String("");
            if(tvISBN.getText().toString().length()==13){
                isbn13 = tvISBN.getText().toString();
            }
            else if(book.getIsbn13()!=null){
                isbn13 = book.getIsbn13();
            }
            if(tvISBN.getText().toString().length()==10){
                isbn10 = tvISBN.getText().toString();
            }
            else if(book.getIsbn10()!=null){
                isbn10 = book.getIsbn10();
            }
            bundle.putParcelable("book", new Book(tvTitle.getText().toString(), tvSubtitle.getText().toString(), tvAuthor.getText().toString(), tvYear.getText().toString(), tvProduction.getText().toString(), tvDescription.getText().toString(), urlImageBook, urlMyImageBook, user.getKey(), isbn10, isbn13, Float.toString(ratingBar.getRating()), swAvailable.isChecked()));
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            finish();
        }

    }

    public void uploadDatabase(Book book) {
        final Book bookToUpload = book;
        pd = new ProgressDialog(AddBook.this);
        pd.setMessage(getString(R.string.wait));
        pd.setCancelable(false);
        pd.show();
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
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Get the url of the image uploaded before, and store the new book
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                urlMyImageBook = downloadUrl.toString();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("books");
                bookToUpload.setUrlMyImage(urlMyImageBook);
                DatabaseReference instanceReference = databaseReference.push();

                instanceReference.setValue(bookToUpload);
                if (pd.isShowing()) {
                    pd.dismiss();
                }
                finish();
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


