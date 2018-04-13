package it.polito.mad.booksharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.content.Intent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;


public class ShowProfile extends AppCompatActivity {
    private static final int MODIFY_PROFILE = 1;
    private ImageButton btnModify;
    private Toolbar toolbar;
    private TextView tvDescription, tvName, tvStreet, tvPhone, tvMail;
    private User user;
    private LinearLayout llParent, llPhone, llMail, llDescription;
    private CircleImageView circleImageView;
    private CircleImageView expandedImage;

    private Animator mCurrentAnimator;

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

        circleImageView = (CircleImageView) findViewById(R.id.profileImage);


        //Initialize the user (must be removed an replace with data stored previously)
        getUserInfoFromFireBase("Alessia");


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

        //Zoom the image when pressed
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImage();
            }
        });

    }

    private void zoomImage() {

        //Take the reference of the field
        expandedImage = (CircleImageView) findViewById(R.id.expanded_image);
        //If the user image is setted I take that from the path specified inside the user object
        //else I user the default one specified inside the resources
        if(user.getImagePath()!=null) {
            expandedImage.setImageBitmap(BitmapFactory.decodeFile(user.getImagePath()));
        }else{
            expandedImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.profile));
        }


        //Structure I need to perform the zoom

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).

        circleImageView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.

        expandedImage.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImage.setPivotX(0f);
        expandedImage.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImage, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImage,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(100);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImage, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImage,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImage,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImage,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(100);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        expandedImage.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        circleImageView.setAlpha(1f);
                        expandedImage.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
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
                getUserInfoFromSharedPref("Alessia");
            }
        }
    }

    //Take all the user information from User object and fill the screen with that information
    private void setUser(User user){

        //Text information
        tvName.setText(user.getName().getValue() + " " +  user.getSurname().getValue());
        tvPhone.setText(user.getPhone().getValue());
        tvMail.setText(user.getEmail().getValue());
        tvDescription.setText(user.getDescription().getValue());

        //Show only the information that have not been made private by the user
        if(user.checkStreet() && !user.getStreet().getValue().equals("")){
            tvStreet.setText(user.getStreet().getValue() + " (" + user.getCity().getValue()+")");
        }
        else{
            tvStreet.setText(user.getCity().getValue());
        }

        if (!user.checkPhone() || user.getPhone().getValue().equals("") ) {
            llPhone.setVisibility(View.GONE);
        }
        else{
            llPhone.setVisibility(View.VISIBLE);
        }

        if (!user.checkMail() || user.getEmail().getValue().equals("") ) {
            llMail.setVisibility(View.GONE);
        }
        else{
            llMail.setVisibility(View.VISIBLE);
        }

        //Set the user profile image
        showUserPictureProfile(user);
    }

    //Set the user profile image
    private void showUserPictureProfile(User user) {
        Bitmap image = null;

        if (user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
            circleImageView = (CircleImageView) findViewById(R.id.profileImage);
            circleImageView.setImageBitmap(image);
        }
    }

    //All of the user info are stored inside the SharedPrefernces as String that is
    //the serialization of a json object populated with the information about a user

    private void getUserInfoFromFireBase(String userID) {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        reference.child("users").child("Alessia").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                    saveUserInfoInSharedPref(dataSnapshot.getValue(User.class));
                    getImageInfoFromFireBase("Alessia");



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void getImageInfoFromFireBase(String userID) {



        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

        StorageReference riversRef = FirebaseStorage.getInstance().getReference();
        StorageReference userPictureRef = riversRef.child("userImgProfile/" +userID+"/picture.jpg");


        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        //If the directory where I want to save the image does not exist I create it
        if (!directory.exists()) {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
            circleImageView.setImageBitmap(image);
        }

        //Create of the destination path
        File userPicture = new File(directory, User.profileImgName);

        userPictureRef.getFile(userPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                showUserPictureProfile(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        StorageReference originalPictureRef = riversRef.child("userImgProfile/" + userID+"/picture_Original.jpg");


        File originalPicture = new File(directory, User.profileImgNameCrop);

        originalPictureRef.getFile(originalPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                showUserPictureProfile(user);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.toString());
            }
        });


    }

    private void saveUserInfoInSharedPref(User user) {

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        Gson json = new Gson();
        String toStore = json.toJson(user);
        this.user = new User(user);
        edit.putString("user", toStore).apply();
        edit.commit();


        //getUserInfoFromSharedPref("Alessia");
        setUser(user);

    }


    protected void getUserInfoFromSharedPref(String userid){

        SharedPreferences sharedPref = getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        if (userName.equals(defaultString)){
            user= new User();
            return;
        }
        Gson json = new Gson();
        user=json.fromJson(userName, User.class);
        if(user.getDescription().getValue().equals("")){

            user.setDescription(new User.MyPair(getString(R.string.description_value),"public"));
        }
        setUser(user);

    }
}
