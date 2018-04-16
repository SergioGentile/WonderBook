package it.polito.mad.booksharing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowAllMyBook extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private ListView lv;
    private List<Book> data;
    private List<String> keys;
    private User user;
    private LinearLayout llEmpty;
    private ImageView animation;
    private View navView;
    private Toolbar toolbar;
    private TextView tvName;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_my_book);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        animation = (ImageView) findViewById(R.id.progressAnimation);
        animation.setVisibility(View.VISIBLE);
        llEmpty = (LinearLayout) findViewById(R.id.llEmpty);
        llEmpty.setVisibility(View.GONE);
        lv = (ListView)findViewById(R.id.lv);
        data = new ArrayList<>();
        keys = new ArrayList<>();
        user = getIntent().getExtras().getParcelable("user");
        showAllMyBooks(user.getKey());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navView = navigationView.getHeaderView(0);
        setUserInfoNavBar();
    }

    private void showAllMyBooks(String keyOwner){
        final List<String> colors = new ArrayList<>();
        colors.add(new String("#00897B"));
        colors.add(new String("#3F51B5"));
        colors.add(new String("#C62828"));
        colors.add(new String("#512DA8"));
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("books").orderByChild("owner").equalTo(keyOwner);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot bookSnap : dataSnapshot.getChildren()) {
                        Book book = bookSnap.getValue(Book.class);
                        data.add(book);
                        keys.add(bookSnap.getKey());
                    }
                }


                animation.setVisibility(View.GONE);
                if(data.isEmpty()){
                    llEmpty.setVisibility(View.VISIBLE);
                }
                else{
                    llEmpty.setVisibility(View.GONE);
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
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("user", user);
                                bundle.putParcelable("book", data.get(position));
                                intent.putExtras(bundle);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(ShowAllMyBook.this, ShowProfile.class));
        }
        else if(id == R.id.nav_home){
            startActivity(new Intent(ShowAllMyBook.this,MainPage.class));
        }
        else if(id == R.id.nav_exit){
            startActivity(new Intent(ShowAllMyBook.this,Start.class));
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUserInfoNavBar(){
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView)navView.findViewById(R.id.profileImageNavBar);
        tvName.setText(this.user.getName().getValue() + " " + this.user.getSurname().getValue());
        Bitmap image = null;

        if (this.user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
            this.profileImage.setImageBitmap(image);
        }

    }
    @Override
    public void onResume(){
        super.onResume();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);
    }
}
