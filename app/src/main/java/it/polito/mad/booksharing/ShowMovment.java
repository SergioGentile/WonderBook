package it.polito.mad.booksharing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import me.leolin.shortcutbadger.ShortcutBadger;

public class ShowMovment extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private User user;
    private final static int BORROW = 0, LAND = 1, PAST = 2;
    private ListView listOfRequest;
    private FirebaseListAdapter<Request> adapter;
    private NavigationView navigationView;
    private View navView;
    private int posTab;
    private  MyBroadcastReceiver mMessageReceiver;
    MyNotificationManager myNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_movment);

        mMessageReceiver = new ShowMovment.MyBroadcastReceiver();
        mMessageReceiver.setCurrentActivityHandler(this);

        user = getIntent().getExtras().getParcelable("user");
        listOfRequest = (ListView) findViewById(R.id.list_of_requests);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = findViewById(R.id.tabsMovment);
        setList(BORROW);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navView = navigationView.getHeaderView(0);
        showEmpty(BORROW);
        posTab = 0;



        setUserInfoNavBar();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setList(tab.getPosition());
                showEmpty(tab.getPosition());
                posTab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("UpdateView"));
    }

    @Override
    protected void onStop(){
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("posTab", posTab);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TabLayout.Tab tab = tabLayout.getTabAt(savedInstanceState.getInt("posTab"));
        tab.select();

    }

    @Override
    protected void onPause(){
        super.onPause();
        myNotificationManager.setLenderStatusNotificationCounter(0);
        myNotificationManager.setBorrowerStatusNotificationCounter(0);
    }

    private void setTabView(Integer carryoutNotification,Integer receivedNotification,Integer notificationConclusi) {
        //EFFETTUATE

        TabLayout.Tab tab0 = tabLayout.getTabAt(0);
        tab0.setCustomView(R.layout.badged_tab);
        TextView tvTab0 = (TextView) tab0.getCustomView().findViewById(R.id.tvTab);

        tvTab0.setText(R.string.borrowed);
        TextView notification0 = (TextView) tab0.getCustomView().findViewById(R.id.notification_badge);
        if(carryoutNotification!=0) {
            notification0.setText(carryoutNotification.toString());
            notification0.setVisibility(View.VISIBLE);
        }else{
            notification0.setVisibility(View.GONE);
        }
        TabLayout.Tab tab1 = tabLayout.getTabAt(1);
        tab1.setCustomView(R.layout.badged_tab);
        TextView tvTab1 = (TextView) tab1.getCustomView().findViewById(R.id.tvTab);
        tvTab1.setText(getString(R.string.lended));
        TextView notification1 = (TextView) tab1.getCustomView().findViewById(R.id.notification_badge);
        if(receivedNotification!=0) {

            notification1.setText(receivedNotification.toString());
            notification1.setVisibility(View.VISIBLE);
        }else{
            notification1.setVisibility(View.GONE);
        }

        TabLayout.Tab tab2 = tabLayout.getTabAt(2);
        tab2.setCustomView(R.layout.badged_tab);
        TextView tvTab2 = (TextView) tab2.getCustomView().findViewById(R.id.tvTab);
        tvTab2.setText(getString(R.string.concluded_loans));
        TextView notification2 = (TextView) tab2.getCustomView().findViewById(R.id.notification_badge);
        if(notificationConclusi!=0) {

            notification2.setText(notificationConclusi.toString());
            notification2.setVisibility(View.VISIBLE);
        }else{
            notification2.setVisibility(View.GONE);
        }
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
                            tv.setText(getString(R.string.warning_no_borrow_book));
                        }
                        else{
                            //check if some request with state start exist
                            int count = 0;
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                Request r = ds.getValue(Request.class);
                                if(r.getStatus().equals(Request.ACCEPTED) || r.getStatus().equals(Request.WAIT_END)){
                                    count++;
                                }
                            }
                            if(count<=0){
                                tv.setText(getString(R.string.warning_no_borrow_book));
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
                        tv.setText(getString(R.string.warning_no_borrow_book));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ll.setVisibility(View.GONE);
                }
            });
        }
        else if (type == LAND){
            FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("incoming").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.exists()){
                        if(dataSnapshot.getChildrenCount() <= 0){
                            //Empty visible
                            ll.setVisibility(View.VISIBLE);
                            tv.setText(getString(R.string.warning_no_lend_book));
                        }
                        else{
                            //Empty gone
                            int count = 0;
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                Request r = ds.getValue(Request.class);
                                if(r.getStatus().equals(Request.ACCEPTED) || r.getStatus().equals(Request.WAIT_END)){
                                    count++;
                                }
                            }
                            if(count<=0){
                                tv.setText(getString(R.string.warning_no_lend_book));
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
                        tv.setText(getString(R.string.warning_no_lend_book));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    ll.setVisibility(View.GONE);
                }
            });
        }
        else{
            FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("ended").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        if(dataSnapshot.getChildrenCount() <= 0){
                            //Empty visible
                            ll.setVisibility(View.VISIBLE);
                            tv.setText(getString(R.string.warning_no_shared_book));
                        }
                        else{
                            //Empty gone
                            tv.setText(getString(R.string.warning_no_shared_book));
                            ll.setVisibility(View.GONE);
                        }
                    }
                    else{
                        //Empty visible
                        ll.setVisibility(View.VISIBLE);
                        tv.setText(getString(R.string.warning_no_shared_book));
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
        else if(BORROW == type){
            color = R.color.borrow;
            colorDark = R.color.borrowDark;
        }
        else{
            color = R.color.colorPrimary;
            colorDark = R.color.colorPrimaryDark;
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
            Query query = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("incoming").orderByChild("time");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_movment_incoming, query) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    LinearLayout ll1 = (LinearLayout) v.findViewById(R.id.item_container);
                    if(!request.getStatus().equals(Request.ACCEPTED) && !request.getStatus().equals(Request.WAIT_END)){
                        ll1.setVisibility(View.GONE);
                        return;
                    }
                    else{
                        ll1.setVisibility(View.VISIBLE);
                    }
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView borrower =(TextView) v.findViewById(R.id.book_borrower);
                    title.setText(User.capitalizeSpace(request.getBookTitle()));
                    borrower.setText(getString(R.string.lended_by_descr).replace("*name*", request.getNameBorrower()).replace("*date*", DateFormat.format("dd/MM/yyyy", -1*request.getTime())));
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
                    View line = (View) v.findViewById(R.id.line);
                    line.setBackgroundColor(getColor(R.color.land));
                    TextView conclude = (TextView)v.findViewById(R.id.tv_accept);
                    TextView waitEnd = (TextView) v.findViewById(R.id.waitEnd);
                    if(user.getKey().equals(request.getEndRequestBy())){
                        waitEnd.setVisibility(View.VISIBLE);
                        conclude.setVisibility(View.GONE);
                    }
                    else{
                        waitEnd.setVisibility(View.GONE);
                        conclude.setVisibility(View.VISIBLE);
                        conclude.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                conclude(request, LAND);
                            }
                        });
                    }

                    //Update lender
                    FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userToUpdate = dataSnapshot.getValue(User.class);
                                if (!request.getNameLender().equals(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue())) {
                                    //Update it
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("incoming").child(request.getKeyRequest()).child("nameLender").setValue(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //Update borrower
                    FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userToUpdate = dataSnapshot.getValue(User.class);
                                if (!request.getNameBorrower().equals(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue())) {
                                    //Update it
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("incoming").child(request.getKeyRequest()).child("nameBorrower").setValue(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User userIntent = dataSnapshot.getValue(User.class);
                                    Intent intent = new Intent(ShowMovment.this, ShowProfile.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("user_mp", userIntent);
                                    bundle.putParcelable("user_owner", user);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            };
        } else if (type == BORROW) {
            Query query = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming").orderByChild("time");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_movment_outcoming, query) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    LinearLayout ll1 = (LinearLayout) v.findViewById(R.id.item_container);
                    if (!request.getStatus().equals(Request.ACCEPTED) && !request.getStatus().equals(Request.WAIT_END)) {
                        ll1.setVisibility(View.GONE);
                        return;
                    } else {
                        ll1.setVisibility(View.VISIBLE);
                    }
                    TextView title = (TextView) v.findViewById(R.id.book_title);
                    TextView lender = (TextView) v.findViewById(R.id.book_lender);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    title.setText(User.capitalizeSpace(request.getBookTitle()));
                    lender.setText(getString(R.string.borrowed_by_descr).replace("*name*", request.getNameLender()).replace("*date*", DateFormat.format("dd/MM/yyyy", -1 * request.getTime())));
                    View line = (View) v.findViewById(R.id.line);
                    line.setBackgroundColor(getColor(R.color.borrow));
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
                    TextView conclude = (TextView) v.findViewById(R.id.tv_accept);
                    TextView waitEnd = (TextView) v.findViewById(R.id.waitEnd);
                    if (user.getKey().equals(request.getEndRequestBy())) {
                        waitEnd.setVisibility(View.VISIBLE);
                        conclude.setVisibility(View.GONE);
                    } else {
                        waitEnd.setVisibility(View.GONE);
                        conclude.setVisibility(View.VISIBLE);
                        conclude.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                conclude(request, BORROW);
                            }
                        });
                    }

                    //Update lender
                    FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userToUpdate = dataSnapshot.getValue(User.class);
                                if (!request.getNameLender().equals(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue())) {
                                    //Update it
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming").child(request.getKeyRequest()).child("nameLender").setValue(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //Update borrower
                    FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userToUpdate = dataSnapshot.getValue(User.class);
                                if (!request.getNameBorrower().equals(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue())) {
                                    //Update it
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming").child(request.getKeyRequest()).child("nameBorrower").setValue(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference child = reference.child("users").child(request.getKeyLender());
                            child.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //Download the updated book and start the activity
                                    final User currentUser = dataSnapshot.getValue(User.class);
                                    FirebaseDatabase.getInstance().getReference("books").child(request.getKeyBook()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()) {
                                                Book bookUpdated = dataSnapshot.getValue(Book.class);
                                                Intent intent = new Intent(ShowMovment.this, ShowBookFull.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putParcelable("book_mp", bookUpdated);
                                                bundle.putParcelable("user_mp", currentUser);
                                                bundle.putParcelable("user_owner", user);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }
                                            else{
                                                Toast.makeText(ShowMovment.this, getString(R.string.no_longer_available), Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    });

                }


            };
        } else {
            Query query = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("ended").orderByChild("timeEnd");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_past_movment, query) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    TextView title = (TextView) v.findViewById(R.id.tvTitle);
                    TextView tvUser = (TextView) v.findViewById(R.id.tvNameUser);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    TextView time = (TextView) v.findViewById(R.id.tvTime);
                    title.setText(User.capitalizeSpace(request.getBookTitle()));
                    View line = (View) v.findViewById(R.id.line);
                    LinearLayout arrow_lend = v.findViewById(R.id.lend_arrow);
                    LinearLayout arrow_borr = v.findViewById(R.id.borrow_arrow);

                    if(request.getKeyLender().equals(user.getKey())){
                        arrow_lend.setVisibility(View.VISIBLE);
                        arrow_borr.setVisibility(View.GONE);
                        tvUser.setText(getString(R.string.lended_by) + " " + request.getNameBorrower());
                        line.setBackgroundColor(getColor(R.color.land));
                    }
                    else{
                        arrow_lend.setVisibility(View.GONE);
                        arrow_borr.setVisibility(View.VISIBLE);
                        tvUser.setText(getString(R.string.borrowed_by)+ " " + request.getNameLender());
                        line.setBackgroundColor(getColor(R.color.borrow));
                    }

                    time.setText(getString(R.string.from) + " " + DateFormat.format("dd/MM/yyyy", -1 * request.getTime()) + " " + getString(R.string.to) + " " + DateFormat.format("dd/MM/yyyy", -1 * request.getTimeEnd()));

                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);

                    //Update lender
                    FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userToUpdate = dataSnapshot.getValue(User.class);
                                if (!request.getNameLender().equals(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue())) {
                                    //Update it
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("ended").child(request.getKeyRequest()).child("nameLender").setValue(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //Update borrower
                    FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                User userToUpdate = dataSnapshot.getValue(User.class);
                                if (!request.getNameBorrower().equals(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue())) {
                                    //Update it
                                    FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("ended").child(request.getKeyRequest()).child("nameBorrower").setValue(userToUpdate.getName().getValue() + " " + userToUpdate.getSurname().getValue());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            };
        }
        return adapterToReturn;
    }


    private void conclude(final Request request, final int type) {

        if (request.getStatus().equals(Request.WAIT_END)) {
            //It means that i can conclude all
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).removeValue();
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).removeValue();
            request.setStatus(Request.END);
            request.setTimeEnd(-1 * new Date().getTime());
            DatabaseReference dbrLend = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("ended").push();
            DatabaseReference dbrBorrow = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("ended");
            String keyEnd = dbrLend.getKey();
            request.setStatus(Request.END);
            request.setKeyRequest(keyEnd);
            dbrLend.setValue(request);
            dbrBorrow.child(keyEnd).setValue(request);
            //change the status of the book from "available" to "not available"
            FirebaseDatabase.getInstance().getReference("books").child(request.getKeyBook()).child("available").setValue(true);
            Toast.makeText(ShowMovment.this, getString(R.string.loan_completed), Toast.LENGTH_SHORT).show();
        } else {
            //Set only the flag in order to wait the other peer
            request.setEndRequestBy(user.getKey());
            request.setStatus(Request.WAIT_END);
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).setValue(request);
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).setValue(request);
            Toast.makeText(ShowMovment.this, getString(R.string.wait_other_user_finish), Toast.LENGTH_SHORT).show();
        }

        //Make the review
        String keyUserToReview = null;
        if (type == LAND) {
            keyUserToReview = request.getKeyBorrower();
        } else {
            keyUserToReview = request.getKeyLender();
        }

        Log.d("User to review: ", keyUserToReview);

        FirebaseDatabase.getInstance().getReference("users").child(keyUserToReview).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User userToReview = dataSnapshot.getValue(User.class);
                Intent intent = new Intent(ShowMovment.this, AddReview.class);
                intent.putExtra("titleBook", User.capitalizeSpace(request.getBookTitle()));
                Bundle bundle = new Bundle();
                bundle.putParcelable("user_logged", user);
                bundle.putParcelable("user_to_review", userToReview);
                intent.putExtras(bundle);
                if (type == LAND) {
                    intent.putExtra("status", "borrow");
                    myNotificationManager.subtractBorrowerStatusNotificationCounter(1);
                    setNotification(myNotificationManager.getMessageCounter(),myNotificationManager.getPendingRequestCounter(),myNotificationManager.getChangeStatusNotifications());
                    setTabView(myNotificationManager.getLenderStatusNotificationCounter(),myNotificationManager.getBorrowerStatusNotificationCounter(),0);
                } else {
                    intent.putExtra("status", "land");
                    myNotificationManager.subtractLenderStatusNotificationCounter(1);
                    setNotification(myNotificationManager.getMessageCounter(),myNotificationManager.getPendingRequestCounter(),myNotificationManager.getChangeStatusNotifications());
                    setTabView(myNotificationManager.getLenderStatusNotificationCounter(),myNotificationManager.getBorrowerStatusNotificationCounter(),0);
                }
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            startActivity(new Intent(ShowMovment.this, ShowProfile.class));
        } else if (id == R.id.nav_show_shared_book) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowMovment.this, ShowAllMyBook.class).putExtras(bundle));
        } else if (id == R.id.nav_show_chat) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowMovment.this, ShowMessageThread.class).putExtras(bundle));
        } else if (id == R.id.pending_request) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowMovment.this, ShowPendingRequest.class).putExtras(bundle));
        }
        if (id == R.id.nav_loans) {
            //Start the intent
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
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
            ShortcutBadger.removeCount(ShowMovment.this);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
            if (directory.exists()) {
                File crop_image = new File(directory, User.profileImgNameCrop);
                crop_image.delete();
                File user_image = new File(directory, User.profileImgName);
                user_image.delete();

            }

            startActivity(new Intent(ShowMovment.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(3).setChecked(true);
        myNotificationManager = MyNotificationManager.getInstance(this);
        int messageCounter = myNotificationManager.getMessageCounter();
        int pendingRequestCounter = myNotificationManager.getPendingRequestCounter();
        int changeStaus = myNotificationManager.getChangeStatusNotifications();
        myNotificationManager.clearNotification();
        setNotification(messageCounter,pendingRequestCounter,changeStaus);
        setTabView(myNotificationManager.getLenderStatusNotificationCounter(),myNotificationManager.getBorrowerStatusNotificationCounter(),0);
    }


    protected void setNotification(Integer notificaction_count, Integer notification_pending_count, Integer notification_loans_count) {

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
        }else{
            loans_nav_bar.setVisibility(View.GONE);
        }
        Integer tot = notificaction_count + notification_pending_count + notification_loans_count;

        if (tot != 0) {
            toolbarNotification.setText(tot.toString());
            toolbarNotification.setVisibility(View.VISIBLE);
        }else{
            toolbarNotification.setVisibility(View.GONE);
        }
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

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private ShowMovment currentActivity = null;

        void setCurrentActivityHandler(ShowMovment currentActivity) {
            this.currentActivity = currentActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UpdateView")) {
                MyNotificationManager myNotificationManager = MyNotificationManager.getInstance(currentActivity);
                currentActivity.setNotification(myNotificationManager.getMessageCounter(),myNotificationManager.getPendingRequestCounter(),myNotificationManager.getChangeStatusNotifications());
                setTabView(myNotificationManager.getLenderStatusNotificationCounter(),myNotificationManager.getBorrowerStatusNotificationCounter(),0);
            }
        }
    }
}
