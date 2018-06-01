package it.polito.mad.booksharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ShowAllMyBook extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView lv;
    private List<Book> data;
    private List<String> keys;
    private User user;
    private LinearLayout llEmpty;
    private ProgressBar progressAnimation;
    private View navView;
    private Toolbar toolbar;
    private TextView tvName;
    private CircleImageView
            profileImage;
    private SwipeRefreshLayout srl;
    private NavigationView navigationView;
    private MyBroadcastReceiver mMessageReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_my_book);

        mMessageReceiver = new MyBroadcastReceiver();
        mMessageReceiver.setCurrentActivityHandler(this);

        toolbar = findViewById(R.id.toolbar);
        //This class manage the exhibition of all the book owned by the user.

        srl = findViewById(R.id.srl);
        progressAnimation = findViewById(R.id.progressAnimation);
        progressAnimation.setVisibility(View.VISIBLE);
        llEmpty = findViewById(R.id.llEmpty);
        lv = findViewById(R.id.lv);
        llEmpty.setVisibility(View.GONE);
        data = new ArrayList<>();
        keys = new ArrayList<>();
        user = getIntent().getExtras().getParcelable("user");
        showAllMyBooks(user.getKey());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navView = navigationView.getHeaderView(0);

        MyNotificationManager notificationManager = MyNotificationManager.getInstance(this);


        setUserInfoNavBar();

        //Manage the gesure to swipe down the screen.
        //It refresh the list.
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(ShowAllMyBook.this, getString(R.string.updating), Toast.LENGTH_SHORT).show();
                srl.setRefreshing(true);
                lv.setVisibility(View.GONE);
                llEmpty.setVisibility(View.GONE);
                showAllMyBooks(user.getKey());
            }
        });

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                startActivity(new Intent(ShowAllMyBook.this, AddBook.class).putExtras(bundle));
            }
        });


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

    protected void setNotification(Integer notification_count, Integer notification_pending_count, Integer notification_loans_count) {

        TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
        TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_show_chat));
        TextView pending_request_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.pending_request));
        TextView loans_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_loans));

        if (notification_count != 0) {

            //Set current notification inside initNavBar method
            message_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
            message_nav_bar.setTypeface(null, Typeface.BOLD);
            message_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
            message_nav_bar.setText(notification_count.toString());

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
        Integer tot = notification_count + notification_pending_count + notification_loans_count;

        if (tot != 0) {
            toolbarNotification.setText(tot.toString());
            toolbarNotification.setVisibility(View.VISIBLE);
        } else {
            toolbarNotification.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        progressAnimation.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        llEmpty.setVisibility(View.GONE);
        showAllMyBooks(user.getKey());
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);
        MyNotificationManager notificationManager = MyNotificationManager.getInstance(this);
        setNotification(notificationManager.getMessageCounter(), notificationManager.getPendingRequestCounter(), notificationManager.getChangeStatusNotifications());
    }

    private void showAllMyBooks(String keyOwner) {


        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference reference = firebaseDatabase.getReference();

        //Query for all the book owned by the user
        Query query = reference.child("books").orderByChild("owner").equalTo(keyOwner);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data.clear();
                keys.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot bookSnap : dataSnapshot.getChildren()) {
                        Book book = bookSnap.getValue(Book.class);
                        data.add(book);
                        keys.add(bookSnap.getKey());
                    }
                }

                progressAnimation.setVisibility(View.GONE);
                if (data.isEmpty()) {
                    llEmpty.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                } else {
                    lv.setVisibility(View.VISIBLE);
                    llEmpty.setVisibility(View.GONE);
                }
                srl.setRefreshing(false);

                //The adapter fill the card.
                lv.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return data.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return data.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(final int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(R.layout.adapter_show_all_my_book, parent, false);
                        }

                        //Now convertView refers to an instance of my layout.
                        TextView title = convertView.findViewById(R.id.title_adapter);
                        TextView author = convertView.findViewById(R.id.author_adapter);
                        TextView publication = convertView.findViewById(R.id.publication_adapter);
                        final ImageView imageBook = convertView.findViewById(R.id.image_adapter);
                        imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        Book book = data.get(position);
                        title.setText(User.capitalizeFirst(book.getTitle()));
                        author.setText(User.capitalizeSpace(book.getAuthor()));
                        publication.setText(User.capitalizeFirst(book.getPublisher() + ", " + book.getYear()));

                        //If an official image of the book exist, fill the card with it, otherwise fill the image view
                        //With the image taken by me (the image that show the conditions of the book)
                        if (!book.getUrlImage().isEmpty()) {
                            imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            Picasso.with(ShowAllMyBook.this)
                                    .load(book.getUrlImage()).noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .error(R.drawable.ic_error_outline_black_24dp)
                                    .into(imageBook, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            imageBook.setScaleType(ImageView.ScaleType.FIT_XY);
                                        }

                                        @Override
                                        public void onError() {
                                            imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                        }
                                    });
                        } else {
                            imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            Picasso.with(ShowAllMyBook.this)
                                    .load(book.getUrlMyImage()).noFade()
                                    .placeholder(R.drawable.progress_animation)
                                    .error(R.drawable.ic_error_outline_black_24dp)
                                    .into(imageBook, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            imageBook.setScaleType(ImageView.ScaleType.FIT_XY);
                                        }

                                        @Override
                                        public void onError() {
                                            imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                        }
                                    });
                        }


                        //Here to show all the book
                        LinearLayout ll_my_book = convertView.findViewById(R.id.ll_adapter_my_book);
                        ImageButton btnEdit = convertView.findViewById(R.id.editMyBook);

                        //edit the book
                        btnEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ShowAllMyBook.this, AddBook.class).putExtra("edit", true);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("user", user);
                                bundle.putParcelable("book", data.get(position));
                                intent.putExtras(bundle);
                                intent.putExtra("key", keys.get(position));
                                startActivity(intent);
                            }
                        });

                        //Show all the information about the book.
                        ll_my_book.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ShowAllMyBook.this, ShowBookFull.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("user", user);
                                bundle.putParcelable("book", data.get(position));
                                intent.putExtra("key", keys.get(position));
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });

                        return convertView;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initNavBar(TextView message_nav_bar) {


    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            // Handle the camera action
            startActivity(new Intent(ShowAllMyBook.this, ShowProfile.class));

        } else if (id == R.id.nav_show_shared_book) {
            //Start the intent
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_show_chat) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowAllMyBook.this, ShowMessageThread.class).putExtras(bundle));
        }
        if (id == R.id.pending_request) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowAllMyBook.this, ShowPendingRequest.class).putExtras(bundle));
        } else if (id == R.id.nav_loans) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowAllMyBook.this, ShowMovment.class).putExtras(bundle));
        } else if (id == R.id.nav_exit) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey());
            databaseReference.child("loggedIn").setValue(false);
            databaseReference.child("notificationMap").setValue(MyNotificationManager.getInstance(this).getMap());
            databaseReference.child("notificationCounter").setValue(MyNotificationManager.getInstance(this).getMessageCounter());
            databaseReference.child("pendingRequestCounter").setValue(MyNotificationManager.getInstance(this).getPendingRequestCounter());
            databaseReference.child("lenderStatusNotificationCounter").setValue(MyNotificationManager.getInstance(this).getLenderStatusNotificationCounter());
            databaseReference.child("borrowerStatusNotificationCounter").setValue(MyNotificationManager.getInstance(this).getBorrowerStatusNotificationCounter());
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("notificationPref", Context.MODE_PRIVATE).edit().clear().apply();
            ShortcutBadger.removeCount(ShowAllMyBook.this);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
            if (directory.exists()) {
                File crop_image = new File(directory, User.profileImgNameCrop);
                crop_image.delete();
                File user_image = new File(directory, User.profileImgName);
                user_image.delete();

            }

            startActivity(new Intent(ShowAllMyBook.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }


    private void setUserInfoNavBar() {
        tvName = navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = navView.findViewById(R.id.profileImageNavBar);
        tvName.setText(this.user.getName().getValue() + " " + this.user.getSurname().getValue());
        Bitmap image = null;

        if (this.user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
            this.profileImage.setImageBitmap(image);
        }

    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private ShowAllMyBook currentActivity = null;

        void setCurrentActivityHandler(ShowAllMyBook currentActivity) {
            this.currentActivity = currentActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UpdateView")) {
                MyNotificationManager myNotificationManager = MyNotificationManager.getInstance(currentActivity);
                currentActivity.setNotification(myNotificationManager.getMessageCounter(), myNotificationManager.getPendingRequestCounter(), myNotificationManager.getChangeStatusNotifications());
            }
        }
    }

}
