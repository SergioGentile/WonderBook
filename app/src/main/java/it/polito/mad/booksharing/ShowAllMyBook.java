package it.polito.mad.booksharing;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowAllMyBook extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView lv;
    private List<Book> data;
    private List<String> keys;
    private User user;
    private LinearLayout llEmpty;
    private ProgressBar progressAnimation;
    private View navView;
    private Toolbar toolbar;
    private TextView tvName;
    private CircleImageView
            profileImage;
    private SwipeRefreshLayout srl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_my_book);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //This class manage the exhibition of all the book owned by the user.

        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        progressAnimation = (ProgressBar) findViewById(R.id.progressAnimation);
        progressAnimation.setVisibility(View.VISIBLE);
        llEmpty = (LinearLayout) findViewById(R.id.llEmpty);
        lv = (ListView) findViewById(R.id.lv);
        llEmpty.setVisibility(View.GONE);
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                startActivity(new Intent(ShowAllMyBook.this, AddBook.class).putExtras(bundle));
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        progressAnimation.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
        llEmpty.setVisibility(View.GONE);
        showAllMyBooks(user.getKey());
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);
    }

    private void showAllMyBooks(String keyOwner) {

        //This list contains the code of different colors.
        //It's useful to show the card of the ListView with different colors.
        final List<String> colors = new ArrayList<>();
        colors.add(new String("#7E57C2"));
        colors.add(new String("#009688"));
        colors.add(new String("#5C6BC0"));

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

                progressAnimation.setVisibility(View.GONE);
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
                        title.setText(User.capitalizeFirst(book.getTitle()));
                        author.setText(User.capitalizeSpace(book.getAuthor()));
                        publication.setText(User.capitalizeFirst(book.getPublisher() + ", " + book.getYear()));
                        //owner.setText(book.getOwner());

                        //If an official image of the book exist, fill the card with it, otherwise fill the image view
                        //With the image taken by me (the image that show the conditions of the book)
                        if (!book.getUrlImage().isEmpty()) {
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
                        } else {
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
                        ImageButton btnEdit = (ImageButton) convertView.findViewById(R.id.editMyBook);

                        //edit the book
                        btnEdit.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(ShowAllMyBook.this, ShowProfile.class));
        } else if (id == R.id.nav_home) {
            //Nothing to do
        } else if (id == R.id.nav_exit) {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
            if (directory.exists()) {
                File crop_image = new File(directory, User.profileImgNameCrop);
                crop_image.delete();
                File user_image = new File(directory, User.profileImgName);
                user_image.delete();

            }
            startActivity(new Intent(ShowAllMyBook.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        finish();
        return true;
    }


    private void setUserInfoNavBar() {
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView) navView.findViewById(R.id.profileImageNavBar);
        tvName.setText(this.user.getName().getValue() + " " + this.user.getSurname().getValue());
        Bitmap image = null;

        if (this.user.getImagePath() != null) {
            image = BitmapFactory.decodeFile(user.getImagePath());
            this.profileImage.setImageBitmap(image);
        }

    }

}
