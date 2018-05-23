package it.polito.mad.booksharing;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Date;

public class ShowMovment extends AppCompatActivity {


    private Toolbar toolbar;
    private TabLayout tabLayout;
    private User user;
    private final static int BORROW=0, LAND=1, PAST = 2;
    private ListView listOfRequest;
    private FirebaseListAdapter<Request> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_movment);

        user = getIntent().getExtras().getParcelable("user");
        listOfRequest = (ListView) findViewById(R.id.list_of_requests);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabsMovment);
        setList(BORROW);

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
                    lender.setText(getString(R.string.borrowed_by_descr).replace("*name*", request.getNameLender()).replace("*date*", DateFormat.format("dd/MM/yyyy", -1*request.getTime())));
                    View line = (View) v.findViewById(R.id.line);
                    line.setBackgroundColor(getColor(R.color.borrow));
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
                    TextView title =(TextView) v.findViewById(R.id.tvTitle);
                    TextView tvUser =(TextView) v.findViewById(R.id.tvNameUser);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    TextView time = (TextView) v.findViewById(R.id.tvTime);
                    title.setText(request.getBookTitle());
                    View line = (View) v.findViewById(R.id.line);
                    if(request.getKeyLender().equals(user.getKey())){
                        tvUser.setText(getString(R.string.lended_by) + " " + request.getNameBorrower());
                        line.setBackgroundColor(getColor(R.color.land));
                    }
                    else{
                        tvUser.setText(getString(R.string.borrowed_by)+ " " + request.getNameLender());
                        line.setBackgroundColor(getColor(R.color.borrow));
                    }

                    time.setText(getString(R.string.from) + " " + DateFormat.format("dd/MM/yyyy", -1*request.getTime()) +  " " + getString(R.string.to) + " " +  DateFormat.format("dd/MM/yyyy", request.getTimeEnd()));

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
            request.setTimeEnd(new Date().getTime());
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
        }
        else{
            //Set only the flag in order to wait the other peer
            request.setEndRequestBy(user.getKey());
            request.setStatus(Request.WAIT_END);
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).setValue(request);
            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).setValue(request);
            Toast.makeText(ShowMovment.this, getString(R.string.wait_other_user_finish), Toast.LENGTH_SHORT).show();
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
                    intent.putExtra("status", "borrow");
                }
                else{
                    intent.putExtra("status", "land");
                }
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
