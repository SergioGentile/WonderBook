package it.polito.mad.booksharing;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private User user;
    private TextView tvName;
    private CircleImageView profileImage;
    private View navView;
    private String userId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();



        setContentView(R.layout.activity_main_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                startActivity(new Intent(MainPage.this, AddBook.class).putExtras(bundle));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // navView = getLayoutInflater().inflate(R.layout.nav_header_main_page, null);
        navView = navigationView.getHeaderView(0);
        setDefaultUser();




    }


    @Override
    public void onStart() {
        super.onStart();

        getUserInfoFromFireBase();
        // Check if user is signed in (non-null) and update UI accordingly.

    }

    private void setDefaultUser() {
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView)navView.findViewById(R.id.profileImageNavBar);
        tvName.setText("");
        profileImage.setImageResource(R.drawable.profile);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu., menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            startActivity(new Intent(MainPage.this, ShowProfile.class));

        } else if (id == R.id.nav_show_shared_book) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(MainPage.this, ShowAllMyBook.class).putExtras(bundle));
        }
        else if(id == R.id.nav_exit){
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo",Context.MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(MainPage.this,Start.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getUserInfoFromFireBase() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference child = reference.child("users").child(currentUser.getUid());
            child.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        saveUserInfoInSharedPref(dataSnapshot.getValue(User.class));
                        getImageInfoFromFireBase();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void getImageInfoFromFireBase() {
        StorageReference riversRef = FirebaseStorage.getInstance().getReference();
        StorageReference userPictureRef = riversRef.child("userImgProfile/" +user.getKey()+"/picture.jpg");



        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        //If the directory where I want to save the image does not exist I create it
        if (!directory.exists()) {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.profile);

        }

        //Create of the destination path
        File userPicture = new File(directory, User.profileImgName);

        userPictureRef.getFile(userPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                setUserInfoNavBar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        StorageReference originalPictureRef = riversRef.child("userImgProfile/" + user.getKey()+"/picture_Original.jpg");


        File originalPicture = new File(directory, User.profileImgNameCrop);

        originalPictureRef.getFile(originalPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

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

        setUserInfoNavBar();

    }

    private void setUserInfoNavBar(){
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView)navView.findViewById(R.id.profileImageNavBar);
        if(user!=null) {
            tvName.setText(this.user.getName().getValue() + " " + this.user.getSurname().getValue());

            Bitmap image = null;

            if (this.user.getImagePath() != null) {
                image = BitmapFactory.decodeFile(user.getImagePath());
                this.profileImage.setImageBitmap(image);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getUserFromSharedPreference();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        setUserInfoNavBar();
    }

    private void getUserFromSharedPreference() {

        SharedPreferences sharedPref = getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        Gson json = new Gson();
        this.user=json.fromJson(userName, User.class);
        //if the user is new there is no previous data
        if (this.user != null) {
            if (this.user.getDescription().getValue().equals("")) {
                this.user.setDescription(new User.MyPair(getString(R.string.description_value), "public"));
            }
        }
    }



}

