package it.polito.mad.booksharing;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fenchtose.nocropper.CropperView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class cropper extends AppCompatActivity {

    ImageButton btnRotate, btnCrop;
    TextView btnDone;
    CropperView cropperView;
    Bitmap newBitmap, originalBitmap;
    boolean isSnappedtoCenter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        originalBitmap = BitmapFactory.decodeFile("/data/data/it.polito.mad.booksharing/app_imageDir/profile_cropper.jpeg");

        btnDone = (TextView) findViewById(R.id.btn_done);
        btnCrop = (ImageButton)findViewById(R.id.crop_button);
        btnRotate = (ImageButton)findViewById(R.id.rotate_button);
        cropperView = (CropperView)findViewById(R.id.imageView);
        cropperView.setImageBitmap(originalBitmap);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropImage();
                saveImageOnInternalStorage(cropperView.getCroppedBitmap());
                finish();
            }
        });


        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(newBitmap!=null){
                    cropperView.setImageBitmap(rotateBitmap(newBitmap, 90));
                    newBitmap = cropperView.getCroppedBitmap();
                }
                else{
                    cropperView.setImageBitmap(rotateBitmap(originalBitmap, 90));
                    newBitmap = cropperView.getCroppedBitmap();
                }

            }
        });

        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSnappedtoCenter){
                    cropperView.cropToCenter();
                }
                else{
                    cropperView.fitToCenter();
                }
                isSnappedtoCenter = !isSnappedtoCenter;
            }
        });

    }

    private Bitmap rotateBitmap(Bitmap mBitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
    }


    private void cropImage(){
        newBitmap = cropperView.getCroppedBitmap();
        if(newBitmap!=null ){
            Log.d("DEBUG CROP", "Arrivo nella parte di setting");
            cropperView.setImageBitmap(newBitmap);
        }
        else{
            newBitmap = originalBitmap;
        }
    }

    private void saveImageOnInternalStorage(Bitmap bitmap){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, "profile.jpeg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            Log.e("PATH", directory + "/profile.jpeg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
    }
}
