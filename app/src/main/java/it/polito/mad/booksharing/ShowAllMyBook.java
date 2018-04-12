package it.polito.mad.booksharing;

import android.content.Intent;
import android.graphics.Color;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ShowAllMyBook extends AppCompatActivity {

    ListView lv;
    List<Book> data;
    List<String> keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_my_book);

        lv = (ListView)findViewById(R.id.lv);
        data = new ArrayList<>();
        keys = new ArrayList<>();

        showAllMyBooks("Sergio");
    }


    private void showAllMyBooks(String ownerName){
        final List<String> colors = new ArrayList<>();
        colors.add(new String("#00897B"));
        colors.add(new String("#3F51B5"));
        colors.add(new String("#C62828"));
        colors.add(new String("#512DA8"));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("books").orderByChild("owner").equalTo(ownerName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot bookSnap : dataSnapshot.getChildren()) {
                        Book book = bookSnap.getValue(Book.class);
                        data.add(book);
                        Log.d("Butto dentro ", bookSnap.getKey());
                        keys.add(bookSnap.getKey());
                    }
                }

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
                        if(convertView==null){
                            convertView = getLayoutInflater().inflate(R.layout.adapter_show_all_my_book, parent, false);
                        }

                        //Now convertView refers to an instance of my layout.
                        TextView title = (TextView) convertView.findViewById(R.id.title_adapter);
                        TextView author = (TextView) convertView.findViewById(R.id.author_adapter);
                        TextView publication = (TextView) convertView.findViewById(R.id.publication_adapter);
                        // TextView owner = (TextView) convertView.findViewById(R.id.owner_adapter);
                        final ImageView imageBook = (ImageView) convertView.findViewById(R.id.image_adapter);
                        imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        Book book = data.get(position);
                        title.setText(book.getTitle());
                        author.setText(book.getAuthor());
                        publication.setText(book.getPublisher() + ", " + book.getYear());
                        //owner.setText(book.getOwner());

                        Picasso.with(ShowAllMyBook.this)
                                .load(book.getUrlImage()).noFade()
                                .placeholder( R.drawable.progress_animation )
                                .into(imageBook, new com.squareup.picasso.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        imageBook.setScaleType(ImageView.ScaleType.FIT_XY);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });


                        //Here to show all the book
                        CardView cv = (CardView) convertView.findViewById(R.id.adapter_cv);
                        cv.setCardBackgroundColor(Color.parseColor(colors.get(position%colors.size())));
                        TextView tvEdit = (TextView) convertView.findViewById(R.id.editMyBook);

                        tvEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ShowAllMyBook.this, AddBook.class).putExtra("edit", true);
                                intent.putExtra("book", data.get(position));
                                intent.putExtra("key", keys.get(position));
                                startActivity(intent);
                                finish();
                            }
                        });


                        cv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ShowAllMyBook.this, ShowBookFull.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("book", data.get(position));
                                intent.putExtra("key", keys.get(position));
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
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
