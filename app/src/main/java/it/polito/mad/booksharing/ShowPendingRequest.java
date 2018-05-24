package it.polito.mad.booksharing;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ShowPendingRequest extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private User user;
    private final static int BORROW=0, LAND=1;
    private ListView listOfRequest;
    private FirebaseListAdapter<Request> adapter;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pending_request);
        user = getIntent().getExtras().getParcelable("user");
        listOfRequest = (ListView) findViewById(R.id.list_of_requests);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabsRequest);
        setList(BORROW);
        showEmpty(BORROW);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
       // navView = navigationView.getHeaderView(0);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setList(tab.getPosition());
                showEmpty(tab.getPosition());
                //Add the listener to notify if the list is empty
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    private void showEmpty(int type){
        //Set all gone
        final LinearLayout ll = (LinearLayout) findViewById(R.id.empty);
        final TextView tv = (TextView) findViewById(R.id.tvWarning);
        ll.setVisibility(View.GONE);
        if(type == BORROW){
            FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        if(dataSnapshot.getChildrenCount() <= 0){
                            //Empty visible
                            ll.setVisibility(View.VISIBLE);
                            tv.setText(getString(R.string.warning_no_out_request));
                        }
                        else{
                            //check if some request with state start exist
                            int count = 0;
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                Request r = ds.getValue(Request.class);
                                if(r.getStatus().equals(Request.SENDED)){
                                    count++;
                                }
                            }
                            if(count<=0){
                                ll.setVisibility(View.VISIBLE);
                            }
                            else{
                                ll.setVisibility(View.GONE);
                            }

                        }
                    }
                    else{
                        //Empty visible
                        ll.setVisibility(View.VISIBLE);
                        tv.setText(getString(R.string.warning_no_out_request));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ll.setVisibility(View.GONE);
                }
            });
        }
        else{
            FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("incoming").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        if(dataSnapshot.getChildrenCount() <= 0){
                            //Empty visible
                            ll.setVisibility(View.VISIBLE);
                            tv.setText(getString(R.string.warning_no_in_request));
                        }
                        else{
                            //Empty gone
                            int count = 0;
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                Request r = ds.getValue(Request.class);
                                if(r.getStatus().equals(Request.SENDED)){
                                    count++;
                                }
                            }
                            if(count<=0){
                                ll.setVisibility(View.VISIBLE);
                            }
                            else{
                                ll.setVisibility(View.GONE);
                            }
                        }
                    }
                    else{
                        //Empty visible
                        ll.setVisibility(View.VISIBLE);
                        tv.setText(getString(R.string.warning_no_in_request));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ll.setVisibility(View.GONE);
                }
            });
        }
    }


    private void setList(int type){
        listOfRequest.setAdapter(null);
        setToolbarColor(type);
        adapter = getAdapter(type);
        listOfRequest.setAdapter(adapter);
    }

    private void setToolbarColor(int type){
        int color, colorDark;
        if(type == LAND){
            color = R.color.land;
            colorDark = R.color.landDark;
        }
        else{
            color = R.color.borrow;
            colorDark = R.color.borrowDark;
        }

        toolbar.setBackgroundColor(getColor(color));
        tabLayout.setBackgroundColor(getColor(color));
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getColor(colorDark));
    }

    private FirebaseListAdapter<Request> getAdapter(int type) {
        FirebaseListAdapter<Request> adapterToReturn = null;
        if(LAND == type){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("incoming");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_pending_notification_incoming, databaseReference) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    LinearLayout ll1 = (LinearLayout) v.findViewById(R.id.item_container);
                    if(!request.getStatus().equals(Request.SENDED)){
                        ll1.setVisibility(View.GONE);
                        return;
                    }
                    else{
                        ll1.setVisibility(View.VISIBLE);
                    }
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView borrower =(TextView) v.findViewById(R.id.book_borrower);
                    title.setText(request.getBookTitle());
                    borrower.setText(getString(R.string.other_request_description).replace("*req_name*", request.getNameBorrower()).replace("*req_date*", DateFormat.format("dd/MM/yyyy", -1*request.getTime())));
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    View view = v.findViewById(R.id.line);
                    view.setBackgroundColor(getColor(R.color.land));

/*
                    final LinearLayout ll = (LinearLayout) v.findViewById(R.id.accept_refuse_ll);
                    final LinearLayout llConteiner = (LinearLayout) v.findViewById(R.id.item_container);
                    ll.setVisibility(View.GONE);

                    llConteiner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ll.getVisibility() == View.GONE){

                                ll.setVisibility(View.VISIBLE);
                                ll.animate().translationY(0).setDuration(200);

                            }else{

                                ll.animate().translationY(ll.getBaseline()).setDuration(200);
                                ll.setVisibility(View.GONE);
                            }


                        }
                    });
                    */
                    Picasso.with(ShowPendingRequest.this).load(request.getBookImageUrl()).into(imageBook);
                    TextView accept = (TextView) v.findViewById(R.id.tv_accept);
                    TextView reject = (TextView) v.findViewById(R.id.tv_refuse);
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).child("status").setValue(Request.ACCEPTED);
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).child("status").setValue(Request.ACCEPTED);
                            //change the status of the book from "available" to "not available"
                            FirebaseDatabase.getInstance().getReference("books").child(request.getKeyBook()).child("available").setValue(false);
                        }
                    });
                    reject.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).child("status").setValue(Request.REJECTED);
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).child("status").setValue(Request.REJECTED);
                        }
                    });


                }
            };
        }
        else {
            Query query = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming").orderByChild("time");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_pending_notification_outcoming, query) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    LinearLayout ll1 = (LinearLayout) v.findViewById(R.id.item_container);
                    if(!request.getStatus().equals(Request.SENDED)){
                        ll1.setVisibility(View.GONE);
                        return;
                    }
                    else{
                        ll1.setVisibility(View.VISIBLE);
                    }
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView lender =(TextView) v.findViewById(R.id.book_lender);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    title.setText(request.getBookTitle());
                    lender.setText(getString(R.string.your_request_description).replace("*req_name*", request.getNameLender()).replace("*req_date*", DateFormat.format("dd/MM/yyyy", -1*request.getTime())));
                    Picasso.with(ShowPendingRequest.this).load(request.getBookImageUrl()).into(imageBook);
                    View view = v.findViewById(R.id.line);
                    view.setBackgroundColor(getColor(R.color.borrow));

/*
                    final LinearLayout ll = (LinearLayout) v.findViewById(R.id.cancel_ll);
                    final LinearLayout llConteiner = (LinearLayout) v.findViewById(R.id.item_container);
                    ll.setVisibility(View.GONE);

                    llConteiner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ll.getVisibility() == View.GONE){

                                ll.setVisibility(View.VISIBLE);
                                ll.animate().translationY(0).setDuration(200);


                            }else{

                                ll.setVisibility(View.GONE);
                                ll.animate().translationY(0).setDuration(200);

                            }


                        }
                    });
*/
                    LinearLayout cancel = (LinearLayout)v.findViewById(R.id.cancel_ll);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).removeValue();
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).removeValue();

                        }
                    });

                }
            };
        }

        return adapterToReturn;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            startActivity(new Intent(ShowPendingRequest.this, ShowProfile.class));
        } else if (id == R.id.nav_show_shared_book) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowPendingRequest.this, ShowAllMyBook.class).putExtras(bundle));
        } else if (id == R.id.nav_show_chat) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowPendingRequest.this, ShowMessageThread.class).putExtras(bundle));
        } if (id == R.id.pending_request) {
            //Start the intent
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if(id == R.id.nav_loans){
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowPendingRequest.this, ShowMovment.class).putExtras(bundle));
        }
        else if (id == R.id.nav_exit) {
            DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey());
            databaseReference.child( "loggedIn").setValue(false);
            databaseReference.child("notificationMap").setValue(MyNotificationManager.getInstance(this).getMap());
            databaseReference.child("notificationCounter").setValue(MyNotificationManager.getInstance(this).getMessageCounter());
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("messageCounter", Context.MODE_PRIVATE).edit().clear().apply();
            ShortcutBadger.removeCount(ShowPendingRequest.this);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
            if (directory.exists()) {
                File crop_image = new File(directory, User.profileImgNameCrop);
                crop_image.delete();
                File user_image = new File(directory, User.profileImgName);
                user_image.delete();

            }

            startActivity(new Intent(ShowPendingRequest.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }


    protected void setNotificaRichiestaPrestito(Integer notificaction_count) {


        TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
        TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.pending_request));
        if (notificaction_count != 0) {

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


    protected void setNotificaPrestito(Integer notificaction_count) {


        TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
        TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_loans));
        if (notificaction_count != 0) {

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

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(4).setChecked(true);
    }
}
