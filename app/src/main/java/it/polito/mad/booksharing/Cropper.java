package it.polito.mad.booksharing;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fenchtose.nocropper.CropperView;

import java.io.File;
import java.io.FileOutputStream;

public class Cropper extends AppCompatActivity {

    ImageButton btnRotate, btnCrop;
    TextView btnDone;
    CropperView cropperView;
    Bitmap newBitmap, originalBitmap;
    boolean isSnappedToCenter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        //Get the path where the image is located
        String path = getIntent().getExtras().getString("user-path");
        //Decode the user image as bitmap. I replace profile with profile_cropper because i will work with the original
        //version of the image.
        originalBitmap = BitmapFactory.decodeFile(path.replace("profile", "profile_cropper"));

        //Take all the references to the view
        btnDone = (TextView) findViewById(R.id.btn_done);
        btnCrop = (ImageButton)findViewById(R.id.crop_button);
        btnRotate = (ImageButton)findViewById(R.id.rotate_button);
        cropperView = (CropperView)findViewById(R.id.imageView);
        cropperView.setImageBitmap(originalBitmap);

        //Catch when the button done is pressed, so the user end to modify own image
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
                //Save the cropped image on the storage
                saveImageOnInternalStorage(cropperView.getCroppedBitmap());
                setResult(Activity.RESULT_OK);
                finish();
            }
        });


        //Rotate the image by 90°
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Rotate the image
                //newBitmap contain the new version of the image
                cropperView.setImageBitmap(rotateBitmap(cropperView.getCroppedBitmap(), 90));
                newBitmap = cropperView.getCroppedBitmap();
            }
        });

        //Fit the image to the center
        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropperView.setImageBitmap(originalBitmap);
            }
        });

    }

    //Function to rotate the bitmap
    private Bitmap rotateBitmap(Bitmap mBitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
    }


    //Function to crop the image.
    //Set the new cropped image as new user image profile
    private void cropImage(){
        newBitmap = cropperView.getCroppedBitmap();
        //If the image is the same of the original one, set the same image.
        if(newBitmap!=null ){
            cropperView.setImageBitmap(newBitmap);
        }
        else{
            newBitmap = originalBitmap;
        }
    }

    //Save the image on the storage dedicated to the application.
    private void saveImageOnInternalStorage(Bitmap bitmap){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, User.profileImgName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Toast.makeText(Cropper.this, R.string.error_upload_image, Toast.LENGTH_SHORT).show();
        }
    }
}
