package it.polito.mad.booksharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ShowMessageThread extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseListAdapter<Peer> adapter;
    private User user;
    private View navView;
    private NavigationView navigationView;
    private FirebaseDatabase firebaseDatabaseAccess;
    private DatabaseReference databaseReferenceAccess;
    boolean updateStatusOnline;
    private MyBroadcastReceiver mMessageReceiver;
    SwipeMenuListView listOfMessage;
    private float x1, x2;
    static final int MIN_DISTANCE = 40;
    private boolean imDelete;
    private MyNotificationManager notificationManager;
    private int counter_examinated, counter_not_empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_message_thread);


        mMessageReceiver = new MyBroadcastReceiver();
        mMessageReceiver.setCurrentActivityHandler(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listOfMessage = findViewById(R.id.list_of_message_thread);

        notificationManager = MyNotificationManager.getInstance(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navView = navigationView.getHeaderView(0);

        MyNotificationManager notificationManager = MyNotificationManager.getInstance(this);
        setNotification(notificationManager.getMessageCounter());

        user = getIntent().getExtras().getParcelable("user");


        firebaseDatabaseAccess = FirebaseDatabase.getInstance();
        databaseReferenceAccess = firebaseDatabaseAccess.getReference("users").child(user.getKey()).child("status");

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete_black_24dp);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

// set creator
        listOfMessage.setMenuCreator(creator);
        listOfMessage.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        listOfMessage.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        //delete chat
                        deleteChat(position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        imDelete = false;
        setUserInfoNavBar();
        showAllChat();
        showImageEmpty();

    }

    private void showImageEmpty() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(user.getKey()).child("chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshots) {
                final ImageView iv = (ImageView) findViewById(R.id.ivEmpty);
                final LinearLayout ll = (LinearLayout) findViewById(R.id.llEmpty);
                final TextView tv = (TextView) findViewById(R.id.tvEmpty);
                counter_examinated = 0;
                counter_not_empty = 0;
                if (dataSnapshots.exists()) {
                    Log.d("Counter", "passo 1");
                    for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                        Log.d("Counter", "passo 2");
                        databaseReference.child(dataSnapshot.getKey()).child("lastMessage").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.d("Counter", "passo 3");
                                String result = dataSnapshot.getValue(String.class);
                                counter_examinated++;
                                if (!result.isEmpty()) {
                                    counter_not_empty++;
                                }

                                if (counter_examinated == dataSnapshots.getChildrenCount()) {
                                    Log.d("Counter", "Not empty: " + counter_not_empty);
                                    Log.d("Counter ", "Examinated: " + counter_examinated);
                                    if (counter_not_empty > 0) {
                                        //Set Gone
                                        tv.setVisibility(View.GONE);
                                        iv.setVisibility(View.GONE);
                                        ll.setVisibility(View.GONE);


                                    } else {
                                        //Set Visible
                                        tv.setVisibility(View.VISIBLE);
                                        iv.setVisibility(View.VISIBLE);
                                        ll.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    tv.setVisibility(View.VISIBLE);
                    iv.setVisibility(View.VISIBLE);
                    ll.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void deleteChat(final int position) {
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(user.getKey()).child("chats").child(adapter.getItem(position).getKeyChat());
        databaseReference.child("lastTimestamp").setValue(1);
        databaseReference.child("lastMessage").setValue("");


        //Delete the notification
        TextView notification = (TextView) adapter.getView(position, null, null).findViewById(R.id.notification);
        notification.setVisibility(View.GONE);
        notification.setText(0 + "");

        //Delete all the messages
        FirebaseDatabase firebaseDatabaseMessages = FirebaseDatabase.getInstance();
        final DatabaseReference databaseReferenceMessages = firebaseDatabaseMessages.getReference("chats").child(adapter.getItem(position).getKeyChat());
        databaseReferenceMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshots) {

                int counter_unread = 0;
                for (DataSnapshot dataSnapshot : dataSnapshots.getChildren()) {
                    ChatMessage cm = dataSnapshot.getValue(ChatMessage.class);
                    if (!cm.getDeleteFor().contains(user.getKey())) {
                        //Update
                        if (!cm.getSender().equals(user.getKey())) {
                            notificationManager.clearNotificationUser(cm.getSender());
                            databaseReferenceMessages.child(cm.getKey()).child("status_read").setValue(true);
                            counter_unread++;
                        }
                        cm.getDeleteFor().add(user.getKey());
                        databaseReferenceMessages.child(cm.getKey()).child("deleteFor").setValue(cm.getDeleteFor());
                    }
                }
                setNotification(notificationManager.getMessageCounter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private int dp2px(int dips) {
        return (int) (dips * ShowMessageThread.this.getResources().getDisplayMetrics().density + 0.5f);
    }


    private void setNotificationAdapterThread(TextView v, String key) {
        int currentNotification = notificationManager.getReceiverCount(key).intValue();

        if (v == null) {
            v = (TextView) adapter.getView(0, null, null).findViewById(R.id.notification);
        }
        if (currentNotification > 0) {
            v.setVisibility(View.VISIBLE);
            v.setText(currentNotification + "");
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void setNotification(Integer notificaction_count) {

        TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
        TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_show_chat));
        if (notificaction_count > 0) {


            //Set current notification inside initNavBar method
            message_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
            message_nav_bar.setTypeface(null, Typeface.BOLD);
            message_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
            message_nav_bar.setText(notificaction_count.toString());

            //Set notification on toolbar icon
            message_nav_bar.setVisibility(View.VISIBLE);

            toolbarNotification.setText(notificaction_count.toString());
            toolbarNotification.setVisibility(View.VISIBLE);
        } else {
            toolbarNotification.setVisibility(View.GONE);
            message_nav_bar.setVisibility(View.GONE);
        }
    }


    private void showAllChat() {

        adapter = new FirebaseListAdapter<Peer>(this, Peer.class, R.layout.adapter_message_thread, FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("chats").orderByChild("lastTimestamp")) {
            @Override
            protected void populateView(View v, Peer peer, final int position) {
                //Get references to the views of list_item.xml
                final CircleImageView profileImage;
                final TextView name, lastMessage, lastTimestamp;
                final ReceiverInformation receiverInformation;
                final Peer peerAnonymus = peer;
                final TextView notificationView;

                profileImage = v.findViewById(R.id.profile);
                name = v.findViewById(R.id.user);
                lastMessage = v.findViewById(R.id.last_mess);
                lastTimestamp = v.findViewById(R.id.text_time);
                receiverInformation = peer.getReceiverInformation();
                notificationView = v.findViewById(R.id.notification);

                setNotificationAdapterThread(notificationView, receiverInformation.getKey());
                Picasso.with(ShowMessageThread.this).load(peer.getReceiverInformation().getPathImage()).noFade().into(profileImage);
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
                        if (!receiverUpdate.getName().getValue().toLowerCase().equals(receiverInformation.getName().toLowerCase())) {
                            nameUpdate = receiverUpdate.getName().getValue();
                            somethingChange = true;
                        }
                        //compare the surname
                        if (!receiverUpdate.getSurname().getValue().toLowerCase().equals(receiverInformation.getSurname().toLowerCase())) {
                            surnameUpdate = receiverUpdate.getName().getValue();
                            somethingChange = true;
                        }
                        //compare the image path
                        if (!receiverUpdate.getUser_image_url().toLowerCase().equals(receiverInformation.getPathImage().toLowerCase())) {
                            Picasso.with(ShowMessageThread.this).load(receiverUpdate.getUser_image_url()).into(profileImage);
                            somethingChange = true;
                        }

                        //if something change, update the db.
                        FirebaseDatabase firebaseDatabaseUpdate = FirebaseDatabase.getInstance();
                        DatabaseReference databaseReferenceUpdate = firebaseDatabaseUpdate.getReference("users").child(user.getKey()).child("chats").child(peerAnonymus.getKeyChat()).child("receiverInformation");
                        if (somethingChange) {
                            name.setText(nameUpdate + " " + surnameUpdate);
                            databaseReferenceUpdate.setValue(new ReceiverInformation(nameUpdate, surnameUpdate, receiverUpdate.getUser_image_url(), receiverUpdate.getKey()));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                LinearLayout ll = v.findViewById(R.id.adapter_message_thread);
                LinearLayout ll1 = v.findViewById(R.id.ll1);
                LinearLayout ll2 = v.findViewById(R.id.ll2);
                LinearLayout ll3 = v.findViewById(R.id.ll2);
                LinearLayout container = v.findViewById(R.id.container);
                LinearLayout centerContainer = v.findViewById(R.id.center_container);
                FrameLayout frameLayout = v.findViewById(R.id.frame);
                View line = v.findViewById(R.id.view_line);

                if (peer.getLastMessage().isEmpty()) {
                    v.setVisibility(View.GONE);
                    return;
                } else {
                    v.setVisibility(View.VISIBLE);
                }


                lastMessage.setText(peer.getLastMessage());
                lastTimestamp.setText(DateFormat.format("HH:mm", -1 * peer.getLastTimestamp()));


                final TextView notification = v.findViewById(R.id.notification);
                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        System.out.println("Event catched: " + event.getAction());
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                x1 = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                x2 = event.getX();
                                float deltaX = x2 - x1;
                                System.out.println("Swipe: " + deltaX);
                                if (deltaX < -MIN_DISTANCE) {
                                    //It's a swipe right to left
                                    imDelete = true;
                                    listOfMessage.smoothOpenMenu(position);
                                } else if (deltaX > MIN_DISTANCE) {
                                    imDelete = false;
                                    listOfMessage.smoothCloseMenu();
                                } else {

                                    if (imDelete) {
                                        listOfMessage.smoothCloseMenu();
                                    } else {
                                        notification.setVisibility(View.GONE);
                                        notification.setText(0 + "");
                                        updateStatusOnline = false;
                                        Intent intent = new Intent(ShowMessageThread.this, ChatPage.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("sender", user);
                                        bundle.putParcelable("receiver", new User(receiverInformation.getName(), receiverInformation.getSurname(), receiverInformation.getPathImage(), receiverInformation.getKey()));
                                        intent.putExtra("key_chat", peerAnonymus.getKeyChat());
                                        intent.putExtras(bundle);
                                        intent.putExtra("fromShowMessageThread", true);
                                        startActivity(intent);
                                    }
                                    imDelete = false;
                                }
                                break;
                            case MotionEvent.ACTION_CANCEL:
                                imDelete = true;
                                listOfMessage.smoothOpenMenu(position);
                                break;
                        }
                        return true;
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
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(ShowMessageThread.this, ShowProfile.class));

        }
        if (id == R.id.nav_show_chat) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(ShowMessageThread.this, MainPage.class));

        } else if (id == R.id.nav_exit) {

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey());
            databaseReference.child("loggedIn").setValue(false);
            databaseReference.child("notificationMap").setValue(notificationManager.getMap());
            databaseReference.child("notificationCounter").setValue(notificationManager.getMessageCounter());

            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("messageCounter", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("notificationMap", Context.MODE_PRIVATE).edit().clear().apply();
            ShortcutBadger.removeCount(ShowMessageThread.this);


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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue(time);
        databaseReferenceAccess.onDisconnect().setValue(time);
        finish();
        return true;
    }


    private void setUserInfoNavBar() {
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

    @Override
    protected void onResume() {
        super.onResume();
        listOfMessage.smoothCloseMenu();
        updateStatusOnline = true;
        String time = new Date().getTime() + "";
        databaseReferenceAccess.setValue("online");
        databaseReferenceAccess.onDisconnect().setValue(time);
        navigationView.getMenu().getItem(3).setChecked(true);
        MyNotificationManager notificationManager = MyNotificationManager.getInstance(this);
        notificationManager.clearNotification();
        setNotification(notificationManager.getMessageCounter());
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
        if (updateStatusOnline) {
            databaseReferenceAccess.setValue(time);
        }
        databaseReferenceAccess.onDisconnect().setValue(time);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        Log.d("OnStop:", "On stop is called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String time = new Date().getTime() + "";
        if (updateStatusOnline) {
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
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("UpdateView"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("status", updateStatusOnline);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateStatusOnline = savedInstanceState.getBoolean("status", true);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private ShowMessageThread currentActivity = null;

        void setCurrentActivityHandler(ShowMessageThread currentActivity) {
            this.currentActivity = currentActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UpdateView")) {

                SwipeMenuListView view = findViewById(R.id.list_of_message_thread);
                for (int i = 0; i < adapter.getCount(); i++) {

                    View child = view.getChildAt(i);
                    String key = adapter.getItem(i).getReceiverInformation().getKey();
                    setNotificationAdapterThread((TextView) child.findViewById(R.id.notification), key);

                }
                MyNotificationManager myNotificationManager = MyNotificationManager.getInstance(currentActivity);
                currentActivity.setNotification(myNotificationManager.getMessageCounter());
                //Search the right things to update


            }
        }
    }
}
