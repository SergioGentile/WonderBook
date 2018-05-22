package it.polito.mad.booksharing;

import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.List;

public class ShowPendingRequest extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private User user;
    private final static int BORROW=0, LAND=1;
    private ListView listOfRequest;
    private FirebaseListAdapter<Request> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pending_request);

        user = getIntent().getExtras().getParcelable("user");
        listOfRequest = (ListView) findViewById(R.id.list_of_requests);
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabsRequest);
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
                protected void populateView(View v, Request request, int position) {
                    TextView title =(TextView) v.findViewById(R.id.book_title);
                    TextView borrower =(TextView) v.findViewById(R.id.book_borrower);
                    title.setText(request.getBookTitle());
                    borrower.setText(request.getNameBorrower());
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);

                    final LinearLayout ll = (LinearLayout) v.findViewById(R.id.accept_refuse_ll);
                    final LinearLayout llConteiner = (LinearLayout) v.findViewById(R.id.item_container);
                    ll.setVisibility(View.GONE);

                    llConteiner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ll.getVisibility() == View.GONE){

                                ll.setVisibility(View.VISIBLE);
                                ll.animate().translationY(ll.getHeight()).setDuration(100);

                            }else{

                                ll.animate().translationY(0).setDuration(100);
                                ll.setVisibility(View.GONE);
                            }


                        }
                    });

                    Picasso.with(ShowPendingRequest.this).load(request.getBookImageUrl()).into(imageBook);

                }
            };
        }
        else{
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(user.getKey()).child("requests").child("outcoming");
            adapterToReturn = new FirebaseListAdapter<Request>(this, Request.class, R.layout.adapter_pending_notification_outcoming, databaseReference) {
                @Override
                protected void populateView(View v, Request request, int position) {
                    TextView title =(TextView) v.findViewById(R.id.book_title);

                    TextView borrower =(TextView) v.findViewById(R.id.book_lender);
                    ImageView imageBook = (ImageView) v.findViewById(R.id.image_book);
                    title.setText(request.getBookTitle());

                    borrower.setText(request.getNameBorrower());
                    Picasso.with(ShowPendingRequest.this).load(request.getBookImageUrl()).into(imageBook);



                    final LinearLayout ll = (LinearLayout) v.findViewById(R.id.cancel_ll);
                    final LinearLayout llConteiner = (LinearLayout) v.findViewById(R.id.item_container);
                    ll.setVisibility(View.GONE);

                    llConteiner.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(ll.getVisibility() == View.GONE){

                                ll.setVisibility(View.VISIBLE);
                                ll.animate().translationY(ll.getHeight()).setDuration(100);

                            }else{

                                ll.setVisibility(View.GONE);
                                ll.animate().translationY(0).setDuration(100);

                            }


                        }
                    });

                }
            };
        }

        return adapterToReturn;
    }
}
