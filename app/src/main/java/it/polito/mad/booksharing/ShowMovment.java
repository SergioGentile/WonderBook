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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;

import me.leolin.shortcutbadger.ShortcutBadger;

public class ShowMovment extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {


    private Toolbar toolbar;
    private TabLayout tabLayout;
    private User user;
    private final static int BORROW=0, LAND=1, PAST = 2;
    private ListView listOfRequest;
    private FirebaseListAdapter<Request> adapter;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_movment);

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

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setList(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
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
            color = R.color.past;
            colorDark = R.color.pastDark;
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
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_movment_incoming, databaseReference) {
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
                    title.setText(request.getBookTitle());
                    borrower.setText(request.getNameBorrower());
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
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
                }
            };
        }
        else if(type == BORROW){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_movment_outcoming, databaseReference) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    LinearLayout ll1 = (LinearLayout) v.findViewById(R.id.item_container);
                    if(!request.getStatus().equals(Request.ACCEPTED) && !request.getStatus().equals(Request.WAIT_END) ){
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
                    lender.setText(request.getNameLender());
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
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
                                conclude(request, BORROW);
                            }
                        });
                    }
                }
            };
        }
        else {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("ended");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_past_movment, databaseReference) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView lender =(TextView) v.findViewById(R.id.book_borrower);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    title.setText(request.getBookTitle());
                    lender.setText(request.getNameLender());
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
                }
            };
        }
        return adapterToReturn;
    }


    private void conclude(Request request, final int type){

        if(request.getStatus().equals(Request.WAIT_END)){
            //It means that i can conclude all
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).removeValue();
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).removeValue();
            request.setStatus(Request.END);
            DatabaseReference dbrLend = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("ended").push();
            DatabaseReference dbrBorrow = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("ended");
            String keyEnd = dbrLend.getKey();
            request.setStatus(Request.END);
            request.setKeyRequest(keyEnd);
            dbrLend.setValue(request);
            dbrBorrow.child(keyEnd).setValue(request);
            //change the status of the book from "available" to "not available"
            FirebaseDatabase.getInstance().getReference("books").child(request.getKeyBook()).child("available").setValue(true);
            Toast.makeText(ShowMovment.this, "Il prestito è stato completato", Toast.LENGTH_SHORT).show();
        }
        else{
            //Set only the flag in order to wait the other peer
            request.setEndRequestBy(user.getKey());
            request.setStatus(Request.WAIT_END);
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).setValue(request);
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).setValue(request);
            Toast.makeText(ShowMovment.this, "In attesa che l'altro utente confermi la cessione", Toast.LENGTH_SHORT).show();
        }

        //Make the review
        String keyUserToReview = null;
        if(type == LAND){
            keyUserToReview = request.getKeyBorrower();
        }
        else{
            keyUserToReview = request.getKeyLender();
        }

        Log.d("User to review: ", keyUserToReview);

        FirebaseDatabase.getInstance().getReference("users").child(keyUserToReview).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User userToReview = dataSnapshot.getValue(User.class);
                Intent intent = new Intent(ShowMovment.this, AddReview.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("user_logged", user);
                bundle.putParcelable("user_to_review", userToReview);
                intent.putExtras(bundle);
                if(type == LAND){
                    intent.putExtra("status", "land");
                }
                else{
                    intent.putExtra("status", "borrow");
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
        }else if(id == R.id.pending_request){
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(ShowMovment.this, ShowPendingRequest.class).putExtras(bundle));
        }
        if (id == R.id.nav_loans) {
            //Start the intent
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        else if (id == R.id.nav_exit) {
            DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey());
            databaseReference.child( "loggedIn").setValue(false);
            databaseReference.child("notificationMap").setValue(MyNotificationManager.getInstance(this).getMap());
            databaseReference.child("notificationCounter").setValue(MyNotificationManager.getInstance(this).getMessageCounter());
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("messageCounter", Context.MODE_PRIVATE).edit().clear().apply();
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
}
