package it.polito.mad.booksharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;


public class ShowProfile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int MODIFY_PROFILE = 1;
    private static final int SHOW_MY_PROFILE = 2, SHOW_OTHER_PROFILE=3;
    private ImageButton btnModify, btnMessage;
    private Toolbar toolbar;
    private TextView tvDescription, tvName, tvStreet, tvPhone, tvMail, tvReviews;
    private RatingBar rateReviews;
    private Integer show_mode;
    private User user;
    private LinearLayout llParent, llPhone, llMail, llDescription;
    private CircleImageView circleImageView;
    private CircleImageView expandedImage;
    private View navView;
    private Animator mCurrentAnimator;
    private CircleImageView profileImage;
    private NavigationView navigationView;
    private MyBroadcastReceiver mMessageReceiver;
    private LinearLayout rlReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Start the activity
        super.onCreate(savedInstanceState);

        mMessageReceiver = new MyBroadcastReceiver();
        mMessageReceiver.setCurrentActivityHandler(this);

        setContentView(R.layout.activity_show_profile);

        //Take all the references to the fields
        toolbar = findViewById(R.id.toolbar);
        tvName = findViewById(R.id.tvName);
        tvStreet = findViewById(R.id.tvStreet);
        tvPhone = findViewById(R.id.tvPhone);
        tvMail = findViewById(R.id.tvMail);
        tvDescription = findViewById(R.id.tvDescription);
        btnModify = findViewById(R.id.btnModify);
        btnMessage = findViewById(R.id.btnMessage);

        llMail = findViewById(R.id.llMail);
        llPhone = findViewById(R.id.llPhone);
        llParent = findViewById(R.id.llParent);
        llDescription = findViewById(R.id.llDescription);
        circleImageView = findViewById(R.id.profileImage);
        tvReviews = findViewById(R.id.tv_reviews);
        rateReviews = findViewById(R.id.rating_reviews);


        rlReviews = findViewById(R.id.rl_reviews);
        rlReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowProfile.this, ShowReviews.class);
                Bundle bundle = new Bundle();
                if(getIntent().getExtras()==null || getIntent().getExtras().getParcelable("user_owner") == null || getIntent().getExtras().getParcelable("user_mp") == null) {
                    bundle.putParcelable("user_logged", user);
                    bundle.putParcelable("user_to_review", user);
                }else{
                    bundle.putParcelable("user_logged", getIntent().getExtras().getParcelable("user_owner"));
                    bundle.putParcelable("user_to_review", getIntent().getExtras().getParcelable("user_mp"));
                }

                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        //Initialize the user (must be removed an replace with data stored previously)
        if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("user_mp") != null) {
            btnModify.setVisibility(View.GONE);
            if(getIntent().getBooleanExtra("showMessage", true)){
                btnMessage.setVisibility(View.VISIBLE);
            }
            else{
                btnMessage.setVisibility(View.GONE);
            }
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
            toolbarNotification.setVisibility(View.GONE);
            show_mode = SHOW_OTHER_PROFILE;

        } else {
            show_mode = SHOW_MY_PROFILE;
            getUserInfoFromSharedPref();
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            navView = navigationView.getHeaderView(0);
            btnMessage.setVisibility(View.GONE);
        }

        if(show_mode==SHOW_OTHER_PROFILE){
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        //Catch when the button modify it's pressed
        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Put the user in a bundle and send it to the activity EditProfile
                Intent intent = new Intent(ShowProfile.this, EditProfile.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                bundle.putString("from", "Show");
                intent.putExtras(bundle);

                //The costant MODIFY_PROFILE is useful when onActivityResult will be called.
                //In this way we can understand that the activity that finish will be associate with that constant
                //(See later)
                startActivityForResult(intent, MODIFY_PROFILE);
            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start activity for chat
                //Check if a chat between me and the other user exist
                final User sender = getIntent().getExtras().getParcelable("user_owner");
                final User receiver = getIntent().getExtras().getParcelable("user_mp");
                //User1: the owner of the phone (sender)
                //User2: the owner of the book (receiver)
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String keyChat = null;
                        if (dataSnapshot.exists()) {
                            //Chat list already exist
                            //Find previous chat between peer
                            boolean found = false;
                            for (DataSnapshot dataPeer : dataSnapshot.getChildren()) {
                                Peer peer = dataPeer.getValue(Peer.class);
                                if (peer.getReceiverInformation().getKey().equals(receiver.getKey())) {
                                    //Chat already exist
                                    found = true;
                                    keyChat = dataPeer.getKey();
                                }
                            }
                            if (!found) {
                                keyChat = createInstanceOfChat(sender, receiver);
                            }
                        } else {
                            keyChat = createInstanceOfChat(sender, receiver);
                        }

                        //Here i have a chat with key keyChat
                        Intent intent = new Intent(ShowProfile.this, ChatPage.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("sender", sender);
                        bundle.putParcelable("receiver", receiver);
                        intent.putExtra("key_chat", keyChat);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        //Zoom the image when pressed
        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImage();
            }
        });

        setUserInfoNavBar();
    }

    private String createInstanceOfChat(User sender, User receiver) {
        //Create chat list
        //For the user1
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats");
        DatabaseReference instanceReference1 = databaseReference.push();
        String key = instanceReference1.getKey();
        instanceReference1.setValue(new Peer(receiver, key));

        //For the receiver: i put the sender
        DatabaseReference instanceReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(key);
        instanceReference2.setValue(new Peer(sender, key));

        return key;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("UpdateView"));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    protected void setNotification(Integer notificaction_count,Integer notification_pending_count,Integer notification_loans_count) {

        if(show_mode==SHOW_MY_PROFILE) {
            TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
            TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_show_chat));
            TextView pending_request_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.pending_request));
            TextView loans_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_loans));

            if (notificaction_count != 0) {

                //Set current notification inside initNavBar method
                message_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
                message_nav_bar.setTypeface(null, Typeface.BOLD);
                message_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
                message_nav_bar.setText(notificaction_count.toString());

                //Set notification on toolbar icon
                message_nav_bar.setVisibility(View.VISIBLE);
            } else {
                message_nav_bar.setVisibility(View.GONE);
            }

            if (notification_pending_count != 0) {
                //Set current notification inside initNavBar method
                pending_request_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
                pending_request_nav_bar.setTypeface(null, Typeface.BOLD);
                pending_request_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
                pending_request_nav_bar.setText(notification_pending_count.toString());
                //Set notification on toolbar icon
                pending_request_nav_bar.setVisibility(View.VISIBLE);
            } else {
                pending_request_nav_bar.setVisibility(View.GONE);
            }
            if (notification_loans_count != 0) {
                //Set current notification inside initNavBar method
                loans_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
                loans_nav_bar.setTypeface(null, Typeface.BOLD);
                loans_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
                loans_nav_bar.setText(notification_loans_count.toString());
                //Set notification on toolbar icon
                loans_nav_bar.setVisibility(View.VISIBLE);
            } else {
                loans_nav_bar.setVisibility(View.GONE);
            }
            Integer tot = notificaction_count + notification_pending_count + notification_loans_count;

            if (tot != 0) {
                toolbarNotification.setText(tot.toString());
                toolbarNotification.setVisibility(View.VISIBLE);
            } else {
                toolbarNotification.setVisibility(View.GONE);
            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        MyNotificationManager notificationManager = MyNotificationManager.getInstance(this);
        setNotification(notificationManager.getMessageCounter(),notificationManager.getPendingRequestCounter(), notificationManager.getChangeStatusNotifications());

        if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("user_mp") != null) {
            getUserInfoFromExtra();
            setUser();
        } else {
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.getMenu().getItem(1).setChecked(true);
            getUserInfoFromSharedPref();
            setUser();
        }
        setUserInfoNavBar();

        FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("numRev").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Integer numRev =dataSnapshot.getValue(Integer.class);
                    if(user.getNumRev()!=numRev){
                        user.setNumRev(numRev);
                        if(Integer.valueOf(user.getNumRev())==1){
                            tvReviews.setText(user.getNumRev() + " " + getString(R.string.review));
                        }
                        else{
                            tvReviews.setText(user.getNumRev() + " " + getString(R.string.reviews));
                        }
                        setSharedPrefUserInfo(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("numStars").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Float numStars =dataSnapshot.getValue(Float.class);
                    if(user.getNumStars()!=numStars){
                        user.setNumStars(numStars);
                        rateReviews.setRating(numStars);
                        setSharedPrefUserInfo(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
    }

    private void zoomImage() {

        //Take the reference of the field
        expandedImage = findViewById(R.id.expanded_image);
        //If the user image is setted I take that from the path specified inside the user object
        //else I user the default one specified inside the resources
        if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("user_mp") != null) {
            expandedImage.setImageBitmap(BitmapFactory.decodeFile(user.getImagePath().replace("profile.", "profile_samb.")));
        } else if (user.getImagePath() != null) {
            expandedImage.setImageBitmap(BitmapFactory.decodeFile(user.getImagePath()));
        } else {
            expandedImage.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.profile));
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
                                        View.Y, startBounds.top))
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
            if (resultCode == Activity.RESULT_OK) {

                getUserInfoFromSharedPref();
                setUser();
                setUserInfoNavBar();
            }
        }
    }

    //Take all the user information from User object and fill the screen with that information
    private void setUser() {

        //Text information
        tvName.setText(user.getName().getValue() + " " + user.getSurname().getValue());
        tvPhone.setText(user.getPhone().getValue());
        tvMail.setText(user.getEmail().getValue());
        tvDescription.setText(user.getDescription().getValue());

        //Show only the information that have not been made private by the user
        if (user.checkStreet() && !user.getStreet().getValue().equals("")) {
            tvStreet.setText(user.getStreet().getValue() + " (" + user.getCity().getValue() + ")");
        } else {
            tvStreet.setText(user.getCity().getValue());
        }

        if (!user.checkPhone() || user.getPhone().getValue().equals("")) {
            llPhone.setVisibility(View.GONE);
        } else {
            llPhone.setVisibility(View.VISIBLE);
        }

        if (!user.checkMail() || user.getEmail().getValue().equals("")) {
            llMail.setVisibility(View.GONE);
        } else {
            llMail.setVisibility(View.VISIBLE);
        }

        if(Integer.valueOf(user.getNumRev())==1){
            tvReviews.setText(user.getNumRev() + " " + getString(R.string.review));
        }
        else{
            tvReviews.setText(user.getNumRev() + " " + getString(R.string.reviews));
        }

        rateReviews.setRating(user.getNumStars());


        //Set the user profile image
        showUserPictureProfile(user);
    }

    //Set the user profile image
    private void showUserPictureProfile(User user) {
        Bitmap image = null;

        if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("user_mp") != null) {
            //image = BitmapFactory.decodeFile(user.getImagePath().replace("profile.", "profile_samb."));
            circleImageView = findViewById(R.id.profileImage);
            Picasso.with(ShowProfile.this).load(user.getUser_image_url()).into(circleImageView);
        } else if (user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
            circleImageView = findViewById(R.id.profileImage);
            circleImageView.setImageBitmap(image);
        }
    }

    //All of the user info are stored inside the SharedPrefernces as String that is
    //the serialization of a json object populated with the information about a user

    protected void getUserInfoFromSharedPref() {

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        if (userName.equals(defaultString)) {
            user = new User();
            return;
        }
        Gson json = new Gson();
        user = json.fromJson(userName, User.class);
        if (user.getDescription().getValue().equals("")) {

            user.setDescription(new User.MyPair(getString(R.string.description_value), "public"));
        }

        setUser();

    }

    protected void getUserInfoFromExtra() {
        user = getIntent().getExtras().getParcelable("user_mp");
        setUser();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;

        } else if (id == R.id.nav_show_shared_book) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowProfile.this, ShowAllMyBook.class).putExtras(bundle));
        } else if (id == R.id.nav_show_chat) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowProfile.this, ShowMessageThread.class).putExtras(bundle));
        } if (id == R.id.pending_request) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowProfile.this, ShowPendingRequest.class).putExtras(bundle));
        }
        else if(id == R.id.nav_loans){
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowProfile.this, ShowMovment.class).putExtras(bundle));
        }
        else if (id == R.id.nav_exit) {
            DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey());
            databaseReference.child( "loggedIn").setValue(false);
            databaseReference.child("notificationMap").setValue(MyNotificationManager.getInstance(this).getMap());
            databaseReference.child("notificationCounter").setValue(MyNotificationManager.getInstance(this).getMessageCounter());
            databaseReference.child("pendingRequestCounter").setValue(MyNotificationManager.getInstance(this).getPendingRequestCounter());
            databaseReference.child("lenderStatusNotificationCounter").setValue(MyNotificationManager.getInstance(this).getLenderStatusNotificationCounter());
            databaseReference.child("borrowerStatusNotificationCounter").setValue(MyNotificationManager.getInstance(this).getBorrowerStatusNotificationCounter());
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("notificationPref", Context.MODE_PRIVATE).edit().clear().apply();
            ShortcutBadger.removeCount(ShowProfile.this);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
            if (directory.exists()) {
                File crop_image = new File(directory, User.profileImgNameCrop);
                crop_image.delete();
                File user_image = new File(directory, User.profileImgName);
                user_image.delete();

            }

            startActivity(new Intent(ShowProfile.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    private void setUserInfoNavBar() {
        if(show_mode==SHOW_MY_PROFILE) {
            TextView barName = navView.findViewById(R.id.profileNameNavBar);
            navView.getBackground().setAlpha(80);

            CircleImageView barprofileImage = navView.findViewById(R.id.profileImageNavBar);
            if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("user_owner") != null) {
                User currentUser = getIntent().getExtras().getParcelable("user_owner");
                barName.setText(currentUser.getName().getValue() + " " + currentUser.getSurname().getValue());
                Bitmap image = null;
                if (currentUser.getImagePath() != null) {
                    image = BitmapFactory.decodeFile(currentUser.getImagePath());
                    barprofileImage.setImageBitmap(image);
                }
            } else {
                barName.setText(this.user.getName().getValue() + " " + this.user.getSurname().getValue());
                Bitmap image = null;

                if (this.user.getImagePath() != null) {
                    image = BitmapFactory.decodeFile(user.getImagePath());
                    barprofileImage.setImageBitmap(image);
                }
            }
        }
    }





    private class MyBroadcastReceiver extends BroadcastReceiver {
        private ShowProfile currentActivity = null;

        void setCurrentActivityHandler(ShowProfile currentActivity) {
            this.currentActivity = currentActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UpdateView")) {
                MyNotificationManager myNotificationManager = MyNotificationManager.getInstance(currentActivity);
                currentActivity.setNotification(myNotificationManager.getMessageCounter(),myNotificationManager.getPendingRequestCounter(),myNotificationManager.getChangeStatusNotifications());
            }
        }
    }
}
