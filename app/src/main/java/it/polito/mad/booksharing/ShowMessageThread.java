package it.polito.mad.booksharing;

import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowMessageThread extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListAdapter adapter;
    private User user;
    private View navView;
    private NavigationView navigationView;
    private FirebaseDatabase firebaseDatabaseAccess;
    private DatabaseReference databaseReferenceAccess;
    boolean updateStatusOnline;
    private List<String> updateMessageThreadOld;
    private List<String> updateMessageThreadNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message_thread);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navView = navigationView.getHeaderView(0);


        setNotification(12);

        user = getIntent().getExtras().getParcelable("user");

        firebaseDatabaseAccess = FirebaseDatabase.getInstance();
        databaseReferenceAccess = firebaseDatabaseAccess.getReference("users").child(user.getKey()).child("status");

        setUserInfoNavBar();
        showAllChat();

    }
    private void setNotification(Integer notificaction_count) {

        TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
        if(notificaction_count!=0) {
            TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_show_chat));

            //Set current notification inside initNavBar method
            message_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
            message_nav_bar.setTypeface(null, Typeface.BOLD);
            message_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
            message_nav_bar.setText(notificaction_count.toString());

            //Set notification on toolbar icon


            toolbarNotification.setText(notificaction_count.toString());
            toolbarNotification.setVisibility(View.VISIBLE);
        }else{
            toolbarNotification.setVisibility(View.GONE);
        }
    }


    private void showAllChat(){
        final ListView listOfMessage = (ListView) findViewById(R.id.list_of_message_thread);
        adapter = new FirebaseListAdapter<Peer>(this, Peer.class, R.layout.adapter_message_thread, FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("chats").orderByPriority()) {
            @Override
            protected void populateView(final View v, final Peer peer, int position) {
                //Get references to the views of list_item.xml

                final CircleImageView profileImage;
                final TextView name, lastMessage, lastTimestamp;
                final ReceiverInformation receiverInformation;

                profileImage = v.findViewById(R.id.profile);
                name = v.findViewById(R.id.user);
                lastMessage = v.findViewById(R.id.last_mess);
                lastTimestamp = v.findViewById(R.id.text_time);

                receiverInformation = peer.getReceiverInformation();
                Picasso.with(ShowMessageThread.this).load(peer.getReceiverInformation().getPathImage()).into(profileImage);
                name.setText(receiverInformation.getName() + " " + receiverInformation.getSurname());

                //Count the message not read
                /***** UPDATE THIS PART WITH NOTIFICATION CLOUD SERVICE****/

               /* FirebaseDatabase firebaseDatabaseNotRead = FirebaseDatabase.getInstance();
                DatabaseReference databaseReferenceNotRead = firebaseDatabaseNotRead.getReference().child("chats").child(peer.getKeyChat());
                databaseReferenceNotRead.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshots) {
                          notification.setVisibility(View.GONE);
                            int counter_notification = 0;
                            for(DataSnapshot dataSnapshot : dataSnapshots.getChildren()){
                                ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);
                                if(!cm.getSender().equals(user.getKey()) && !cm.isStatus_read()){
                                    counter_notification++;
                                }
                            }
                            if(counter_notification>0){
                                notification.setVisibility(View.VISIBLE);
                                notification.setText(counter_notification + "");
                            }
                            else{
                                notification.setVisibility(View.GONE);
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/

                 /*************/


                //Update receiver information if necessary
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase.getReference().child("users").child(receiverInformation.getKey());
                //Take the user and compare it with the current one
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String nameUpdate, surnameUpdate;
                        boolean somethingChange = false;
                        nameUpdate = receiverInformation.getName();
                        surnameUpdate = receiverInformation.getSurname();
                        User receiverUpdate = dataSnapshot.getValue(User.class);
                        //compare the name
                        if(!receiverUpdate.getName().getValue().toLowerCase().equals(receiverInformation.getName().toLowerCase())){
                            nameUpdate = receiverUpdate.getName().getValue();
                            somethingChange = true;
                        }
                        //compare the surname
                        if(!receiverUpdate.getSurname().getValue().toLowerCase().equals(receiverInformation.getSurname().toLowerCase())){
                            surnameUpdate = receiverUpdate.getName().getValue();
                            somethingChange = true;
                        }
                        //compare the image path
                        if(!receiverUpdate.getUser_image_url().toLowerCase().equals(receiverInformation.getPathImage().toLowerCase())){
                            Picasso.with(ShowMessageThread.this).load(receiverUpdate.getUser_image_url()).into(profileImage);
                            somethingChange = true;
                        }
                        name.setText(nameUpdate + " " + surnameUpdate);
                        //if something change, update the db.
                        FirebaseDatabase firebaseDatabaseUpdate = FirebaseDatabase.getInstance();
                        Log.d("User key" , user.getKey());
                        Log.d("Peer key", peer.getKeyChat());
                        DatabaseReference databaseReferenceUpdate = firebaseDatabaseUpdate.getReference("users").child(user.getKey()).child("chats").child(peer.getKeyChat()).child("receiverInformation");
                        if(somethingChange){
                            databaseReferenceUpdate.setValue(new ReceiverInformation(nameUpdate, surnameUpdate, receiverUpdate.getUser_image_url(),receiverUpdate.getKey()));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                LinearLayout ll = (LinearLayout) v.findViewById(R.id.adapter_message_thread);
                LinearLayout ll1 = (LinearLayout) v.findViewById(R.id.ll1);
                LinearLayout ll2 = (LinearLayout) v.findViewById(R.id.ll2);
                LinearLayout container = (LinearLayout) v.findViewById(R.id.container);
                LinearLayout centerContainer = (LinearLayout) v.findViewById(R.id.center_container);

                View line = (View) v.findViewById(R.id.view_line);
                if(peer.getLastMessage().isEmpty()){
                    profileImage.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    lastMessage.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                    lastTimestamp.setVisibility(View.GONE);
                    line.setVisibility(View.GONE);
                    ll.setVisibility(View.GONE);
                    ll1.setVisibility(View.GONE);
                    ll2.setVisibility(View.GONE);
                    centerContainer.setVisibility(View.GONE);
                    container.setVisibility(View.GONE);
                    return;
                }
                else{
                    profileImage.setVisibility(View.VISIBLE);
                    name.setVisibility(View.VISIBLE);
                    lastMessage.setVisibility(View.VISIBLE);
                    v.setVisibility(View.VISIBLE);
                    lastTimestamp.setVisibility(View.VISIBLE);
                    line.setVisibility(View.VISIBLE);
                    ll.setVisibility(View.VISIBLE);
                    ll1.setVisibility(View.VISIBLE);
                    ll2.setVisibility(View.VISIBLE);
                    centerContainer.setVisibility(View.VISIBLE);
                    container.setVisibility(View.VISIBLE);
                }



                lastMessage.setText(peer.getLastMessage());
                lastTimestamp.setText(DateFormat.format("HH:mm",peer.getLastTimestamp()));

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Here i have a chat with key keyChat
                        updateStatusOnline = false;
                        Intent intent = new Intent(ShowMessageThread.this, ChatPage.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("sender", user);
                        bundle.putParcelable("receiver", new User(receiverInformation.getName(), receiverInformation.getSurname(), receiverInformation.getPathImage(), receiverInformation.getKey()));
                        intent.putExtra("key_chat", peer.getKeyChat());
                        intent.putExtras(bundle);
                        intent.putExtra("fromShowMessageThread", true);
                        startActivity(intent);
                    }
                });
            }
        };

        listOfMessage.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_show_shared_book) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowMessageThread.this, ShowAllMyBook.class).putExtras(bundle));
        }
        else if (id == R.id.nav_profile) {
            startActivity(new Intent(ShowMessageThread.this, ShowProfile.class));

        }
        if(id == R.id.nav_show_chat){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.nav_home) {
            startActivity(new Intent(ShowMessageThread.this, MainPage.class));

        } else if (id == R.id.nav_exit) {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
            if (directory.exists()) {
                File crop_image = new File(directory, User.profileImgNameCrop);
                crop_image.delete();
                File user_image = new File(directory, User.profileImgName);
                user_image.delete();

            }
            startActivity(new Intent(ShowMessageThread.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue(time);
        databaseReferenceAccess.onDisconnect().setValue(time);
        finish();
        return true;
    }


    private void setUserInfoNavBar() {
        TextView barName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        CircleImageView barprofileImage = (CircleImageView) navView.findViewById(R.id.profileImageNavBar);
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

    @Override
    protected void onResume() {
        super.onResume();
        updateStatusOnline = true;
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue("online");
        databaseReferenceAccess.onDisconnect().setValue(time);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue(time);
        databaseReferenceAccess.onDisconnect().setValue(time);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        String time = new Date().getTime() + "";
        if(updateStatusOnline) {
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);
        Log.d("OnStop:", "On stop is called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String time = new Date().getTime() + "";
        if(updateStatusOnline){
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue("online");
        databaseReferenceAccess.onDisconnect().setValue(time);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("status",updateStatusOnline);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateStatusOnline = savedInstanceState.getBoolean("status", true);
    }
}
