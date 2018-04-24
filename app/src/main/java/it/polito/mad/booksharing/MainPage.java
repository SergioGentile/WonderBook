package it.polito.mad.booksharing;

import android.*;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private User user;
    private TextView tvName;
    private CircleImageView profileImage;
    private View navView;
    private String userId;
    private FirebaseAuth mAuth;
    private MapView mMapView;
    private MaterialSearchView searchView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    //For showing book
    LinearLayout ll_search_runtime;
    ListView lv_search_runtime, lv_searched;
    FirebaseDatabase firebaseDatabase;
    ArrayList<Book> books;
    ArrayList<String> booksQuery;
    ArrayList<Book> booksMatch;
    ArrayList<User> usersMatch;
    ArrayList<String> bookIds;
    GoogleMap map;
    HashMap<String, Marker> markers;
    boolean submit;
    String fillSearchBar;
    int searchBarItem;
    private final static int AUTHOR = 0, TITLE = 1, NO_ITEM =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        //Ask permission for editing photo
        ActivityCompat.requestPermissions(MainPage.this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA},
                1);

        setContentView(R.layout.activity_main_page);
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                startActivity(new Intent(MainPage.this, AddBook.class).putExtras(bundle));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // navView = getLayoutInflater().inflate(R.layout.nav_header_main_page, null);
        navView = navigationView.getHeaderView(0);
        setDefaultUser();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
        this.markers = new HashMap<>();


        //Search part
        firebaseDatabase = FirebaseDatabase.getInstance();
        lv_search_runtime = (ListView) findViewById(R.id.lv_search_runtime);
        lv_searched = (ListView) findViewById(R.id.lv_searched);
        books = new ArrayList<>();
        booksQuery = new ArrayList<>();
        booksMatch = new ArrayList<>();
        usersMatch = new ArrayList<>();
        bookIds = new ArrayList<>();
        submit = false;
        searchView.closeSearch();
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                lv_search_runtime.setVisibility(View.GONE);
                setAdapterSearched(query.toString());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (!submit) {
                    setAdapterRuntime(newText);
                } else {
                    submit = true;
                }
                return false;
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        getUserInfoFromFireBase();
        // Check if user is signed in (non-null) and update UI accordingly.
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
    }

    private void setDefaultUser() {
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView)navView.findViewById(R.id.profileImageNavBar);
        tvName.setText("");
        profileImage.setImageResource(R.drawable.profile);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.searchButton);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.searchButton) {
            searchView.clearFocus();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
            startActivity(new Intent(MainPage.this, ShowProfile.class));

        } else if (id == R.id.nav_show_shared_book) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(MainPage.this, ShowAllMyBook.class).putExtras(bundle));
        }
        else if(id == R.id.nav_exit){
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo",Context.MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(MainPage.this,Start.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getUserInfoFromFireBase() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference child = reference.child("users").child(currentUser.getUid());
            child.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        saveUserInfoInSharedPref(dataSnapshot.getValue(User.class));
                        getImageInfoFromFireBase();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void getImageInfoFromFireBase() {
        StorageReference riversRef = FirebaseStorage.getInstance().getReference();
        StorageReference userPictureRef = riversRef.child("userImgProfile/" +user.getKey()+"/picture." + User.COMPRESS_FORMAT_STR);



        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        //If the directory where I want to save the image does not exist I create it
        if (!directory.exists()) {
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.profile);

        }

        //Create of the destination path
        File userPicture = new File(directory, User.profileImgName);

        userPictureRef.getFile(userPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                setUserInfoNavBar();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        StorageReference originalPictureRef = riversRef.child("userImgProfile/" + user.getKey()+"/picture_Original." + User.COMPRESS_FORMAT_STR);


        File originalPicture = new File(directory, User.profileImgNameCrop);

        originalPictureRef.getFile(originalPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.toString());
            }
        });


    }


    private void saveUserInfoInSharedPref(User user) {

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        Gson json = new Gson();
        String toStore = json.toJson(user);
        this.user = new User(user);
        edit.putString("user", toStore).apply();
        edit.commit();

        String alertMessage = this.user.checkInfo(getApplicationContext());
        if (alertMessage != null) {
            Bundle bundle = new Bundle();
            bundle.putString("from","Register");
            Intent intent = new Intent(MainPage.this,EditProfile.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }


    }

    private void setUserInfoNavBar(){
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView)navView.findViewById(R.id.profileImageNavBar);
        if(user!=null) {
            tvName.setText(this.user.getName().getValue() + " " + this.user.getSurname().getValue());

            Bitmap image = null;

            if (this.user.getImagePath() != null) {
                image = BitmapFactory.decodeFile(user.getImagePath());
                this.profileImage.setImageBitmap(image);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        getUserFromSharedPreference();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        setUserInfoNavBar();

        mMapView.onResume();
    }

    private void getUserFromSharedPreference() {

        SharedPreferences sharedPref = getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        Gson json = new Gson();
        this.user=json.fromJson(userName, User.class);
        //if the user is new there is no previous data
        if (this.user != null) {
            if (this.user.getDescription().getValue().equals("")) {
                this.user.setDescription(new User.MyPair(getString(R.string.description_value), "public"));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainPage.this, R.string.permission_ext_storage_denied, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private void setAdapterSearched(final String searchedString) {
        final List<String> colors = new ArrayList<>();
        colors.add(new String("#00897B"));
        colors.add(new String("#3F51B5"));
        colors.add(new String("#C62828"));
        colors.add(new String("#512DA8"));
        //qui reference
        DatabaseReference databaseReferenceBooks = firebaseDatabase.getReference("books");
        Query query = null;
        if(searchBarItem == TITLE){
            query = databaseReferenceBooks.orderByChild("title").equalTo(searchedString);
        }
        else if(searchBarItem == AUTHOR){
            query = databaseReferenceBooks.orderByChild("author").equalTo(searchedString);
        }

        if(query==null){
            return;
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lv_searched.setAdapter(null);
                booksMatch.clear();
                usersMatch.clear();
                bookIds.clear();
                map.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if(!issue.getValue(Book.class).getOwner().equals(user.getKey())){
                            booksMatch.add(issue.getValue(Book.class));
                            bookIds.add(issue.getKey());
                        }
                    }
                }
                for(Book bookToAdd : booksMatch){
                    DatabaseReference databaseReferenceUsers = firebaseDatabase.getReference("users");
                    Query queryUser = databaseReferenceUsers.child(bookToAdd.getOwner());
                    queryUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            usersMatch.add(dataSnapshot.getValue(User.class));

                            if(usersMatch.size() == booksMatch.size()){
                                //Daniele:
                                //In questo punto del codice ho finito di popolare le liste userMatch e booksMatch.
                                //In sostanza booksMatch contiene una lista di tutti i libri da mostrare nella listView e
                                //usersMatch contiene l'utente associato a quel libro (booksMatch e usersMatch agiscono come una mappa)
                                // Map<Book, User>
                                //Quindi una prima opzione è ciclare qui su tutti i libri contenuti nella lista booksMatch ù
                                //e inserire tutti i relativi marker nella mappa  (seconda opzione più in basso)

                                DatabaseReference databaseReferenceLocation = firebaseDatabase.getReference("locations").child("books");
                                GeoFire geoFire = new GeoFire(databaseReferenceLocation);

                                for(int pos = 0; pos < booksMatch.size(); pos++){
                                    geoFire.getLocation(bookIds.get(pos), new LocationCallback() {
                                        @Override
                                        public void onLocationResult(String key, GeoLocation location) {
                                            if (location != null) {
                                                System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                                                Marker m = map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(key));
                                                markers.put(key, m);

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                for (Marker marker : markers.values()) {
                                                    builder.include(marker.getPosition());
                                                }
                                                LatLngBounds bounds = builder.build();
                                                int width = getResources().getDisplayMetrics().widthPixels;
                                                int height = getResources().getDisplayMetrics().heightPixels;
                                                int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

                                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                                                map.animateCamera(cu);


                                            } else {
                                                System.out.println(String.format("There is no location for key %s in GeoFire", key));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            System.err.println("There was an error getting the GeoFire location: " + databaseError);
                                        }
                                    });
                                }

                                //Qui si setta l'adapter della list view
                                lv_searched.setAdapter(new BaseAdapter() {
                                    @Override
                                    public int getCount() {
                                        return booksMatch.size();
                                    }

                                    @Override
                                    public Object getItem(int position) {
                                        return booksMatch.get(position);
                                    }

                                    @Override
                                    public long getItemId(int position) {
                                        return 0;
                                    }

                                    @Override
                                    public View getView(int position, View convertView, ViewGroup parent) {
                                        if (convertView == null) {
                                            convertView = getLayoutInflater().inflate(R.layout.adapter_searched_book, parent, false);
                                        }
                                        //Popolo l'adapter
                                        final Book book = booksMatch.get(position);
                                        final User currentUser = usersMatch.get(position);
                                        //User userOfBook = usersMatch.get(position);
                                        ImageView imageView = (ImageView) convertView.findViewById(R.id.image_book_searched);
                                        Picasso.with(MainPage.this).load(book.getUrlMyImage()).noFade().into(imageView);
                                        TextView title = (TextView) convertView.findViewById(R.id.title_searched);
                                        RatingBar rb = (RatingBar) convertView.findViewById(R.id.rating_searched);
                                        //For setting a text view with two different colors
                                        TextView owner = (TextView) convertView.findViewById(R.id.shared_name);
                                        Spannable sharedBy = new SpannableString(getString(R.string.shared_by) + " ");
                                        sharedBy.setSpan(new ForegroundColorSpan(Color.WHITE), 0, sharedBy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        owner.setText(sharedBy);
                                        Spannable wordTwo = new SpannableString(currentUser.getName().getValue() + " " + currentUser.getSurname().getValue());
                                        wordTwo.setSpan(new ForegroundColorSpan(Color.WHITE), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        owner.append(wordTwo);
                                        TextView location = (TextView) convertView.findViewById(R.id.shared_location);

                                        title.setText(book.getTitle());
                                        rb.setRating(new Float(book.getRating()));
                                        String currentLocation = new String(currentUser.getCity().getValue());
                                        if(currentUser.getStreet().getStatus().equals("public")){
                                            currentLocation+=", " + currentUser.getStreet().getValue();
                                        }
                                        location.setText(currentLocation);

                                        CardView cv = (CardView) convertView.findViewById(R.id.adapter_cv_searched);
                                        cv.setCardBackgroundColor(Color.parseColor(colors.get(position % colors.size())));


                                        LinearLayout llAdapter = (LinearLayout) convertView.findViewById(R.id.ll_adapter_searched_book);

                                        llAdapter.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(MainPage.this, ShowBookFull.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putParcelable("book_mp", book);
                                                bundle.putParcelable("user_mp", currentUser);
                                                bundle.putParcelable("user_owner", user);
                                                intent.putExtras(bundle);
                                                startActivity(intent);
                                            }
                                        });

                                        //Seconda opzione: qui stai esaminando il libro in posizione "position"
                                        //Puoi fare una query per quel libro (per trovarne la posizione) e settare il marker
                                        //setta il marker di booksMatch.get(position) qui
                                        return convertView;

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String concatenateFieldBook(Book book){
        return (book.getAuthor() + " " + book.getTitle() + " " + book.getSubtitle() + " " + book.getPublisher() + " ").toLowerCase();
    }

    private void setAdapterRuntime(final String searchedString) {
        DatabaseReference databaseReferenceBooks = firebaseDatabase.getReference("books");
        databaseReferenceBooks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!submit) {
                    lv_search_runtime.setVisibility(View.VISIBLE);
                }
                submit = false;
                if (searchedString.isEmpty() || searchedString.length() < 2) {
                    lv_search_runtime.setAdapter(null);
                    return;
                }
                books.clear();
                booksQuery.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (concatenateFieldBook(book).contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                        //Check if there is a things with the same title
                        if (!stringAlreadyPresent(concatenateFieldBook(book), books)) {
                            books.add(book);
                            booksQuery.add(searchedString.toLowerCase());
                        }
                    }
                }


                //here i have all the books with the title searched
                lv_search_runtime.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return books.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return books.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(R.layout.adapter_search_bar_runtime, parent, false);
                        }

                        LinearLayout ll;
                        TextView textView;
                        ImageView imageView;
                        View view;
                        textView = (TextView) convertView.findViewById(R.id.tv_search_runtime);
                        imageView = (ImageView) convertView.findViewById(R.id.image_search_runtime);
                        ll = (LinearLayout) convertView.findViewById(R.id.ll_adapter_runtime);
                        view = (View) convertView.findViewById(R.id.line_search_runtime);

                        //Understand what the user search
                        fillSearchBar = "";
                        final int searchItem = understandSearchItem(booksQuery.get(position), books.get(position));
                        Drawable d;
                        switch (searchItem){
                            case TITLE:
                                fillSearchBar = books.get(position).getTitle();
                                textView.setText(books.get(position).getTitle());
                                d = getDrawable(R.drawable.ic_book_black_24dp);
                                d.setTint(getColor(R.color.colorPrimary));
                                d.setTintMode(PorterDuff.Mode.SRC_IN);
                                imageView.setImageDrawable(d);
                                break;
                            case AUTHOR:
                                fillSearchBar = books.get(position).getAuthor();
                                textView.setText(books.get(position).getAuthor());
                                d = getDrawable(R.drawable.ic_person_black_24dp);
                                d.setTint(getColor(R.color.colorPrimary));
                                d.setTintMode(PorterDuff.Mode.SRC_IN);
                                imageView.setImageDrawable(d);
                                break;
                            case NO_ITEM:
                                ll.setVisibility(View.GONE);
                                imageView.setVisibility(View.GONE);
                                textView.setVisibility(View.GONE);
                                view.setVisibility(View.GONE);
                                break;

                        }


                        ll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                searchBarItem = searchItem;
                                searchView.setQuery(fillSearchBar, true);
                                submit = true;
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

    private int understandSearchItem(String query, Book book){
        query = query.toLowerCase();
        if(book.getTitle().toLowerCase().contains(query) || book.getSubtitle().toLowerCase().contains(query)){
            return TITLE;
        }
        else if(book.getAuthor().toLowerCase().contains(query)){
            return AUTHOR;
        }
        return NO_ITEM;

    }

    private boolean stringAlreadyPresent(String toSearch, ArrayList<Book> books) {

        for (Book book : books) {
            if (concatenateFieldBook(book).toLowerCase().equals(toSearch.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}