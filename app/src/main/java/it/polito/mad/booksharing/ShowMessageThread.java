package it.polito.mad.booksharing;

import android.content.ClipData;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowMessageThread extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListAdapter adapter;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message_thread);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
     /*   DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
*/
        user = getIntent().getExtras().getParcelable("user");

        showAllChat();

    }

    private void showAllChat(){
        final ListView listOfMessage = (ListView) findViewById(R.id.list_of_message_thread);

        adapter = new FirebaseListAdapter<Peer>(this, Peer.class, R.layout.adapter_message_thread, FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("chats").orderByPriority()) {
            @Override
            protected void populateView(View v, final Peer peer, int position) {
                //Get references to the views of list_item.xml

                final CircleImageView profileImage;
                final TextView name, lastMessage;
                final ReceiverInformation receiverInformation;

                profileImage = v.findViewById(R.id.profile);
                name = v.findViewById(R.id.user);
                lastMessage = v.findViewById(R.id.last_mess);
                receiverInformation = peer.getReceiverInformation();
                Picasso.with(ShowMessageThread.this).load(peer.getReceiverInformation().getPathImage()).into(profileImage);
                name.setText(receiverInformation.getName() + " " + receiverInformation.getSurname());
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
                if(peer.getLastMessage().isEmpty()){
                    ll.setVisibility(View.GONE);
                    profileImage.setVisibility(View.GONE);
                    name.setVisibility(View.GONE);
                    lastMessage.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                   // View line = (View) v.findViewById(R.id.line);
                    //line.setVisibility(View.GONE);
                    return;
                }


                lastMessage.setText(peer.getLastMessage());

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Here i have a chat with key keyChat
                        Intent intent = new Intent(ShowMessageThread.this, ChatPage.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("sender", user);
                        bundle.putParcelable("receiver", new User(receiverInformation.getName(), receiverInformation.getSurname(), receiverInformation.getPathImage(), receiverInformation.getKey()));
                        intent.putExtra("key_chat", peer.getKeyChat());
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });

                //DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(peer.getPeerInformationReceiver(user.getKey()).getKey()).child("chats");
            }
        };

        listOfMessage.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(ShowMessageThread.this, ShowProfile.class));

        } else if (id == R.id.nav_home) {
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
        finish();
        return true;
    }

    //Update user chat
    private void updateReceiver(){

    }

}
