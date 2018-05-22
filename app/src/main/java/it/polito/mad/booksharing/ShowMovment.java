package it.polito.mad.booksharing;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

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
                    if(!request.getStatus().equals(Request.ACCEPTED)){
                        v.setVisibility(View.GONE);
                        return;
                    }
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView borrower =(TextView) v.findViewById(R.id.book_borrower);
                    title.setText(request.getBookTitle());
                    borrower.setText(request.getNameBorrower());
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
                    LinearLayout conclude = (LinearLayout)v.findViewById(R.id.conclude_ll);
                    conclude.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).removeValue();
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).removeValue();
                            Request requestToEnd = request;
                            requestToEnd.setStatus(Request.END);
                            DatabaseReference dbrLend = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("ended").push();
                            DatabaseReference dbrBorrow = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("ended");
                            String keyEnd = dbrLend.getKey();
                            requestToEnd.setStatus(Request.END);
                            requestToEnd.setKeyRequest(keyEnd);
                            dbrLend.setValue(requestToEnd);
                            dbrBorrow.child(keyEnd).setValue(requestToEnd);
                            //change the status of the book from "available" to "not available"
                            FirebaseDatabase.getInstance().getReference("books").child(request.getKeyBook()).child("available").setValue(true);
                        }
                    });

                }
            };
        }
        else if(type == BORROW){
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_movment_outcoming, databaseReference) {
                @Override
                protected void populateView(View v, final Request request, int position) {
                    if(!request.getStatus().equals(Request.ACCEPTED)){
                        v.setVisibility(View.GONE);
                        return;
                    }
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView lender =(TextView) v.findViewById(R.id.book_lender);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    title.setText(request.getBookTitle());
                    lender.setText(request.getNameLender());
                    Picasso.with(ShowMovment.this).load(request.getBookImageUrl()).into(imageBook);
                    LinearLayout conclude = (LinearLayout)v.findViewById(R.id.conclude_ll);
                    conclude.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("outcoming").child(request.getKeyRequest()).removeValue();
                            FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("incoming").child(request.getKeyRequest()).removeValue();
                            Request requestToEnd = request;
                            requestToEnd.setStatus(Request.END);
                            DatabaseReference dbrLend = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyLender()).child("requests").child("ended").push();
                            DatabaseReference dbrBorrow = FirebaseDatabase.getInstance().getReference("users").child(request.getKeyBorrower()).child("requests").child("ended");
                            String keyEnd = dbrLend.getKey();
                            requestToEnd.setStatus(Request.END);
                            requestToEnd.setKeyRequest(keyEnd);
                            dbrLend.setValue(requestToEnd);
                            dbrBorrow.child(keyEnd).setValue(requestToEnd);
                            //change the status of the book from "available" to "not available"
                            FirebaseDatabase.getInstance().getReference("books").child(request.getKeyBook()).child("available").setValue(true);
                        }
                    });
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
}
