package it.polito.mad.booksharing;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ShowAllMyBook extends AppCompatActivity {

    private ListView lv;
    private List<Book> data;
    private List<String> keys;
    private User user;
    private LinearLayout llEmpty;
    private ImageView animation;
    private SwipeRefreshLayout srl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_my_book);

        //This class manage the exhibition of all the book owned by the user.

        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        animation = (ImageView) findViewById(R.id.progressAnimation);
        animation.setVisibility(View.VISIBLE);
        llEmpty = (LinearLayout) findViewById(R.id.llEmpty);
        lv = (ListView) findViewById(R.id.lv);
        llEmpty.setVisibility(View.GONE);
        data = new ArrayList<>();
        keys = new ArrayList<>();
        user = getIntent().getExtras().getParcelable("user");
        showAllMyBooks(user.getKey());

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        animation.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        llEmpty.setVisibility(View.GONE);
        showAllMyBooks(user.getKey());
    }

    private void showAllMyBooks(String keyOwner) {

        //This list contains the code of different colors.
        //It's useful to show the card of the ListView with different colors.
        final List<String> colors = new ArrayList<>();
        colors.add(new String("#00897B"));
        colors.add(new String("#3F51B5"));
        colors.add(new String("#C62828"));
        colors.add(new String("#512DA8"));

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

                animation.setVisibility(View.GONE);
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
                        TextView title = (TextView) convertView.findViewById(R.id.title_adapter);
                        TextView author = (TextView) convertView.findViewById(R.id.author_adapter);
                        TextView publication = (TextView) convertView.findViewById(R.id.publication_adapter);
                        final ImageView imageBook = (ImageView) convertView.findViewById(R.id.image_adapter);
                        imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        Book book = data.get(position);
                        title.setText(book.getTitle());
                        author.setText(book.getAuthor());
                        publication.setText(book.getPublisher() + ", " + book.getYear());
                        //owner.setText(book.getOwner());

                        //If an official image of the book exist, fill the card with it, otherwise fill the image view
                        //With the image taken by me (the image that show the conditions of the book)
                       if(!book.getUrlImage().isEmpty()){
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
                       }
                       else{
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
                        CardView cv = (CardView) convertView.findViewById(R.id.adapter_cv);
                        cv.setCardBackgroundColor(Color.parseColor(colors.get(position % colors.size())));
                        TextView tvEdit = (TextView) convertView.findViewById(R.id.editMyBook);

                        //edit the book
                        tvEdit.setOnClickListener(new View.OnClickListener() {
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
                        cv.setOnClickListener(new View.OnClickListener() {
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
}
