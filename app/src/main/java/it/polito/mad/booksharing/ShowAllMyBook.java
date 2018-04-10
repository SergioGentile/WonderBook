package it.polito.mad.booksharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_my_book);

        lv = (ListView)findViewById(R.id.lv);
        data = new ArrayList<>();

        showAllMyBooks("Sergio");
    }


    private void showAllMyBooks(String ownerName){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("books").orderByChild("owner").equalTo(ownerName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot bookSnap : dataSnapshot.getChildren()) {
                        Book book = bookSnap.getValue(Book.class);
                        data.add(book);
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
                        // TextView owner = (TextView) convertView.findViewById(R.id.owner_adapter);
                        ImageView imageBook = (ImageView) convertView.findViewById(R.id.image_adapter);

                        Book book = data.get(position);
                        title.setText(book.getTitle());
                        author.setText(book.getAuthor());
                        //owner.setText(book.getOwner());

                        Picasso.with( ShowAllMyBook.this )
                                .load( book.getUrlMyImage() ).noFade()
                                // .error( R.drawable.ic_error_black_24dp )
                                .placeholder( R.drawable.progress_animation )
                                .into( imageBook );

                        //Here to show all the book
                        LinearLayout ll = (LinearLayout) convertView.findViewById(R.id.adapter_ll);
                        ll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ShowAllMyBook.this, ShowBookFull.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("book", data.get(position));
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
