package it.polito.mad.booksharing;

import android.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import org.w3c.dom.Text;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, DialogOrderType.BottomSheetListener {

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
    //To download all the books
    ArrayList<Book> books;
    ArrayList<String> booksQuery;
    //To avoid the repetition when i search something
    ArrayList<String> stringAlreadyMatched;
    ArrayList<ShowOnAdapter> stringRuntime;

    //to show all the result of the research
    ArrayList<Book> booksMatch;
    ArrayList<User> usersMatch;
    ArrayList<String> bookIds;
    GoogleMap map;
    HashMap<String, Marker> markers;
    boolean submit;
    int searchBarItem;
    private final static int AUTHOR = 0, TITLE = 1, ANY = 2;
    private final static int DISTANCE = 0, RATING = 1, NO_ORDER = 2, DATE = 3;
    int counter_location;
    private int tabFieldSearch;
    private String runTimeQuery;
    private ImageView imageScanOnSearch;
    private TextView orderDialog, tvOrderType;

    double latPhone, longPhone;
    private boolean tabFlag = false;


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
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

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
        imageScanOnSearch = (ImageView) findViewById(R.id.imgScanOnSearch);
        books = new ArrayList<>();
        booksQuery = new ArrayList<>();
        booksMatch = new ArrayList<>();
        usersMatch = new ArrayList<>();
        stringAlreadyMatched = new ArrayList<>();
        stringRuntime = new ArrayList<>();
        bookIds = new ArrayList<>();
        submit = false;
        searchView.closeSearch();
        tabFieldSearch = ANY;
        runTimeQuery = new String("");
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tabFlag = true;
                tabLayout.setVisibility(View.GONE);
                imageScanOnSearch.setVisibility(View.GONE);
                lv_search_runtime.setVisibility(View.GONE);
                setAdapterSearched(query.toString().toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!submit) {
                    runTimeQuery = newText.toLowerCase();
                    setAdapterRuntime(newText.toLowerCase(), tabFieldSearch);
                } else {
                    submit = true;
                }
                return false;
            }
        });

        imageScanOnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainPage.this, CameraScan.class).putExtra("only-isbn", true), 0);
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                tabFlag = false;
                tabLayout.getTabAt(0).select();
                tabLayout.setVisibility(View.VISIBLE);
                imageScanOnSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                if (tabFlag == false) {
                    tabLayout.setVisibility(View.GONE);
                    imageScanOnSearch.setVisibility(View.GONE);
                }

            }
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    tabFieldSearch = ANY;
                    imageScanOnSearch.setVisibility(View.VISIBLE);
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, ANY);
                    }
                } else if (tab.getPosition() == 1) {
                    tabFieldSearch = TITLE;
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, TITLE);
                    }
                } else if (tab.getPosition() == 2) {
                    tabFieldSearch = AUTHOR;
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, AUTHOR);
                    }
                } else if (tab.getPosition() == 4) {
                    imageScanOnSearch.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 4 || tab.getPosition() == 0) {
                    imageScanOnSearch.setVisibility(View.GONE);
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        orderDialog = (TextView) findViewById(R.id.dialogOrder);
        tvOrderType = (TextView) findViewById(R.id.tvOrderType);

        orderDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogOrderType bottomSheet = new DialogOrderType();
                bottomSheet.show(getSupportFragmentManager(), "exampleBottomSheet");
            }
        });
    }


    private double distanceLocation(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        double i = 348842;
        double i2 = i / 60000;
        return (double) Math.round(Math.abs(dist) * 100) / (double) 100;
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0 && resultCode == AppCompatActivity.RESULT_OK) {
            searchView.setQuery(intent.getStringExtra("isbn"), false);
        }
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
        map.setMaxZoomPreference(18);
    }

    private void setDefaultUser() {
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView) navView.findViewById(R.id.profileImageNavBar);
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
        } else if (id == R.id.nav_exit) {
            FirebaseAuth.getInstance().signOut();
            getSharedPreferences("UserInfo", Context.MODE_PRIVATE).edit().clear().apply();
            startActivity(new Intent(MainPage.this, Start.class));
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getUserInfoFromFireBase() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference child = reference.child("users").child(currentUser.getUid());
            child.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        saveUserInfoInSharedPref(dataSnapshot.getValue(User.class));
                        getImageInfoFromFireBase();
                        setAdapterSearchedRecentAdd();
                        Geocoder geocoder = new Geocoder(MainPage.this);
                        List<Address> addresses;
                        String location = user.getStreet().getValue() + " " + user.getCap().getValue() + " " + user.getCity().getValue();
                        try {
                            addresses = geocoder.getFromLocationName(location, 1);
                            if (addresses.size() > 0) {
                                latPhone = addresses.get(0).getLatitude();
                                longPhone = addresses.get(0).getLongitude();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainPage.this, "Non è stato possibile trovare la propria posizione", Toast.LENGTH_SHORT);
                        }
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
        StorageReference userPictureRef = riversRef.child("userImgProfile/" + user.getKey() + "/picture." + User.COMPRESS_FORMAT_STR);


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

        StorageReference originalPictureRef = riversRef.child("userImgProfile/" + user.getKey() + "/picture_Original." + User.COMPRESS_FORMAT_STR);


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
            bundle.putString("from", "Register");
            Intent intent = new Intent(MainPage.this, EditProfile.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }


    }

    private void setUserInfoNavBar() {
        tvName = (TextView) navView.findViewById(R.id.profileNameNavBar);
        navView.getBackground().setAlpha(80);

        profileImage = (CircleImageView) navView.findViewById(R.id.profileImageNavBar);
        if (user != null) {
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

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String defaultString = "";
        String userName = sharedPref.getString("user", defaultString);
        Gson json = new Gson();
        this.user = json.fromJson(userName, User.class);
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
        //qui reference
        DatabaseReference databaseReferenceBooks = firebaseDatabase.getReference("books");
        Query query = null;
        if (searchBarItem == TITLE) {
            query = databaseReferenceBooks.orderByChild("title").equalTo(searchedString);
        } else if (searchBarItem == AUTHOR) {
            query = databaseReferenceBooks.orderByChild("author").equalTo(searchedString);
        }

        if (query == null) {
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
                        if (!issue.getValue(Book.class).getOwner().equals(user.getKey())) {
                            booksMatch.add(issue.getValue(Book.class));
                            bookIds.add(issue.getKey());
                        }
                    }
                }
                for (Book bookToAdd : booksMatch) {
                    DatabaseReference databaseReferenceUsers = firebaseDatabase.getReference("users");
                    Query queryUser = databaseReferenceUsers.child(bookToAdd.getOwner());
                    queryUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            usersMatch.add(dataSnapshot.getValue(User.class));

                            if (usersMatch.size() == booksMatch.size()) {


                                //Daniele:
                                //In questo punto del codice ho finito di popolare le liste userMatch e booksMatch.
                                //In sostanza booksMatch contiene una lista di tutti i libri da mostrare nella listView e
                                //usersMatch contiene l'utente associato a quel libro (booksMatch e usersMatch agiscono come una mappa)
                                // Map<Book, User>
                                //Quindi una prima opzione è ciclare qui su tutti i libri contenuti nella lista booksMatch ù
                                //e inserire tutti i relativi marker nella mappa  (seconda opzione più in basso)

                                DatabaseReference databaseReferenceLocation = firebaseDatabase.getReference("locations").child("books");
                                GeoFire geoFire = new GeoFire(databaseReferenceLocation);

                                counter_location = 0;
                                for (int pos = 0; pos < booksMatch.size(); pos++) {
                                    final int i = pos;
                                    geoFire.getLocation(bookIds.get(pos), new LocationCallback() {
                                        @Override
                                        public void onLocationResult(String key, GeoLocation location) {
                                            counter_location++;
                                            if (location != null) {
                                                System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                                                String snippet = getString(R.string.shared_by) + " " + usersMatch.get(i).getSurname().getValue() + " " + usersMatch.get(i).getName().getValue();
                                                Marker m = map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(booksMatch.get(i).getTitle()).snippet(snippet));
                                                markers.put(key, m);
                                                //Evaluate distance for the bok
                                                booksMatch.get(i).setDistance(distanceLocation(latPhone, longPhone, location.latitude, location.longitude));
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
                                                booksMatch.get(i).setDistance(0);
                                                System.out.println(String.format("There is no location for key %s in GeoFire", key));
                                            }
                                            if (counter_location == booksMatch.size()) {
                                                //Qui si setta l'adapter della list view
                                                //Sort adapter

                                                setAdapter(DISTANCE);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            System.err.println("There was an error getting the GeoFire location: " + databaseError);
                                        }
                                    });
                                }

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

    private void setAdapterSearchedRecentAdd() {
        //qui reference

        DatabaseReference databaseReferenceBooks = firebaseDatabase.getReference("books");
        Query query = null;
        query = databaseReferenceBooks.orderByChild("date").limitToLast(20);

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
                        if (!issue.getValue(Book.class).getOwner().equals(user.getKey())) {
                            Log.d("Libro " , issue.getValue(Book.class).getTitle());
                            booksMatch.add(issue.getValue(Book.class));
                            bookIds.add(issue.getKey());
                        }
                    }
                }

                for (Book bookToAdd : booksMatch) {
                    DatabaseReference databaseReferenceUsers = firebaseDatabase.getReference("users");
                    Query queryUser = databaseReferenceUsers.child(bookToAdd.getOwner());
                    queryUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            usersMatch.add(dataSnapshot.getValue(User.class));

                            if (usersMatch.size() == booksMatch.size()) {

                                //Daniele:
                                //In questo punto del codice ho finito di popolare le liste userMatch e booksMatch.
                                //In sostanza booksMatch contiene una lista di tutti i libri da mostrare nella listView e
                                //usersMatch contiene l'utente associato a quel libro (booksMatch e usersMatch agiscono come una mappa)
                                // Map<Book, User>
                                //Quindi una prima opzione è ciclare qui su tutti i libri contenuti nella lista booksMatch ù
                                //e inserire tutti i relativi marker nella mappa  (seconda opzione più in basso)

                                DatabaseReference databaseReferenceLocation = firebaseDatabase.getReference("locations").child("books");
                                GeoFire geoFire = new GeoFire(databaseReferenceLocation);

                                counter_location = 0;
                                for (int pos = 0; pos < booksMatch.size(); pos++) {
                                    final int i = pos;
                                    geoFire.getLocation(bookIds.get(pos), new LocationCallback() {
                                        @Override
                                        public void onLocationResult(String key, GeoLocation location) {
                                            counter_location++;
                                            if (location != null) {
                                                System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                                                String snippet = getString(R.string.shared_by) + " " + usersMatch.get(i).getSurname().getValue() + " " + usersMatch.get(i).getName().getValue();
                                                Marker m = map.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title(booksMatch.get(i).getTitle()).snippet(snippet));
                                                markers.put(key, m);
                                                //Evaluate distance for the bok
                                                booksMatch.get(i).setDistance(distanceLocation(latPhone, longPhone, location.latitude, location.longitude));
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
                                                booksMatch.get(i).setDistance(0);
                                                System.out.println(String.format("There is no location for key %s in GeoFire", key));
                                            }
                                            if (counter_location == booksMatch.size()) {
                                                //Qui si setta l'adapter della list view
                                                List<Book> tmp = new ArrayList<>();
                                                //Take only the one near me and store in tmp
                                                for(int i=0; i<booksMatch.size(); i++){
                                                    if(booksMatch.get(i).getDistance()<20 && booksMatch.get(i).getDistance()!=0){
                                                        tmp.add(booksMatch.get(i));
                                                    }
                                                }

                                                Collections.sort(tmp, new Comparator<Book>() {

                                                    @Override
                                                    public int compare(Book b1, Book b2) {
                                                        return -b1.getDate().compareTo(b2.getDate());
                                                    }
                                                });
                                                booksMatch.clear();
                                                for(int i=0; i<tmp.size() && i<5; i++){
                                                    booksMatch.add(tmp.get(i));
                                                }

                                                setAdapter(NO_ORDER);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            System.err.println("There was an error getting the GeoFire location: " + databaseError);
                                        }
                                    });
                                }

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


    private void setAdapter(int order) {

        if (order == DISTANCE) {
            tvOrderType.setText("Più vicini a te");
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return b1.getDistance().compareTo(b2.getDistance());
                }
            });
        } else if (order == RATING) {
            tvOrderType.setText("I piu votati");
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return -b1.getRating().toString().compareTo(b2.getRating().toString());
                }
            });
        }
        else if(NO_ORDER == order){
            tvOrderType.setText("Le ultime uscite vicino a te");
        }
        else if(DATE == order){
            tvOrderType.setText("Le ultime usicte");
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return -b1.getDate().toString().compareTo(b2.getDate().toString());
                }
            });
        }
        final List<String> colors = new ArrayList<>();
        colors.add(new String("#00897B"));
        colors.add(new String("#3F51B5"));
        colors.add(new String("#C62828"));
        colors.add(new String("#512DA8"));
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
                final Book book = booksMatch.get(position);
                final User currentUser = usersMatch.get(position);
                //User userOfBook = usersMatch.get(position);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.image_book_searched);
                Picasso.with(MainPage.this).load(book.getUrlMyImage()).noFade().into(imageView);
                TextView title = (TextView) convertView.findViewById(R.id.title_searched);
                TextView author = (TextView) convertView.findViewById(R.id.author_searched);
                RatingBar rb = (RatingBar) convertView.findViewById(R.id.rating_searched);
                //For setting a text view with two different colors
                TextView owner = (TextView) convertView.findViewById(R.id.shared_name);
                Spannable sharedBy = new SpannableString(getString(R.string.shared_by) + " ");
                sharedBy.setSpan(new ForegroundColorSpan(Color.WHITE), 0, sharedBy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                owner.setText(sharedBy);
                Spannable wordTwo = new SpannableString(currentUser.getName().getValue() + " " + currentUser.getSurname().getValue());
                wordTwo.setSpan(new ForegroundColorSpan(Color.WHITE), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                owner.append(wordTwo);

                final TextView distance = (TextView) convertView.findViewById(R.id.distance);
                final LinearLayout ll_distance = (LinearLayout) convertView.findViewById(R.id.ll_location);

                if (booksMatch.get(position).getDistance() > 0) {
                    ll_distance.setVisibility(View.VISIBLE);
                    distance.setText(Double.toString(booksMatch.get(position).getDistance()));
                }


                title.setText(book.getTitle());
                author.setText(book.getAuthor());
                rb.setRating(new Float(book.getRating()));
                String currentLocation = new String(currentUser.getCity().getValue());
                if (currentUser.getStreet().getStatus().equals("public")) {
                    currentLocation += ", " + currentUser.getStreet().getValue();
                }

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

    private String concatenateFieldBook(Book book) {
        return (book.getAuthor() + " " + book.getTitle()).toLowerCase();
    }

    private void setAdapterRuntime(final String searchedString, final int tabField) {
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
                int counter = 0;
                Log.d("RE-SEARCH:", searchedString.toLowerCase());
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (tabField == ANY) {
                        Log.d("Field any", "counter " + counter + " and check for " + book.getTitle());
                        if (concatenateFieldBook(book).contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(concatenateFieldBook(book), books, ANY) && counter < 4) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                                counter++;
                            }
                        }
                    } else if (tabField == TITLE) {
                        Log.d("Field title", "counter " + counter + " and check for " + book.getTitle());
                        if (book.getTitle().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(book.getTitle().toLowerCase(), books, TITLE) && counter < 4) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                                counter++;
                            }
                        }
                    } else if (tabField == AUTHOR) {
                        if (book.getAuthor().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {

                            if (!stringAlreadyPresentOnBookList(book.getAuthor().toLowerCase(), books, AUTHOR) && counter < 4) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                                counter++;
                            }
                        }
                    }
                }


                //set element to show in adapter. I must show only
                stringAlreadyMatched.clear();
                stringRuntime.clear();

                for (int i = 0; i < books.size(); i++) {
                    //Understand the tipology of the item
                    int searchItem = understandSearchItem(booksQuery.get(i), books.get(i));
                    String itemString = new String("");
                    switch (searchItem) {
                        case TITLE:
                            itemString = books.get(i).getTitle();
                            break;
                        case AUTHOR:
                            itemString = books.get(i).getAuthor();
                            break;
                        case ANY:
                            break;
                    }
                    if (!itemString.isEmpty()) {
                        if (!stringAlreadyPresentOnStringList(itemString.toLowerCase(), stringAlreadyMatched)) {
                            stringAlreadyMatched.add(itemString);
                            stringRuntime.add(new ShowOnAdapter(searchItem, itemString));
                        }
                    }
                }


                //here i have all the books with the title searched
                //lv_search_runtime.setEmptyView(findViewById(R.id.empty_view));

                lv_search_runtime.setAdapter(new BaseAdapter() {

                    @Override
                    public int getCount() {
                        return stringRuntime.size();
                    }

                    @Override
                    public Object getItem(int position) {
                        return stringRuntime.get(position);
                    }

                    @Override
                    public long getItemId(int position) {
                        return 0;
                    }

                    @Override
                    public View getView(final int position, View convertView, ViewGroup parent) {
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
                        Drawable d = null;
                        textView.setText(stringRuntime.get(position).getText());
                        if (stringRuntime.get(position).getItemType() == AUTHOR) {
                            d = getDrawable(R.drawable.ic_person_black_24dp);
                        } else if (stringRuntime.get(position).getItemType() == TITLE) {
                            d = getDrawable(R.drawable.ic_book_black_24dp);
                        }
                        d.setTint(getColor(R.color.colorPrimary));
                        d.setTintMode(PorterDuff.Mode.SRC_IN);
                        imageView.setImageDrawable(d);


                        ll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                searchBarItem = stringRuntime.get(position).getItemType();
                                searchView.setQuery(stringRuntime.get(position).getText(), true);
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

    private int understandSearchItem(String query, Book book) {
        query = query.toLowerCase();
        if (book.getTitle().toLowerCase().contains(query) || book.getSubtitle().toLowerCase().contains(query)) {
            return TITLE;
        } else if (book.getAuthor().toLowerCase().contains(query)) {
            return AUTHOR;
        }
        return ANY;

    }

    private boolean stringAlreadyPresentOnBookList(String toSearch, ArrayList<Book> books, int type) {

        for (Book book : books) {
            if (concatenateFieldBook(book).toLowerCase().equals(toSearch.toLowerCase()) && type == ANY) {
                return true;
            } else if (book.getTitle().toLowerCase().equals(toSearch.toLowerCase()) && type == TITLE) {
                return true;
            } else if (book.getAuthor().toLowerCase().equals(toSearch.toLowerCase()) && type == AUTHOR) {
                return true;
            }


        }
        return false;
    }

    private boolean stringAlreadyPresentOnStringList(String toSearch, ArrayList<String> strings) {
        for (String string : strings) {
            if (string.toLowerCase().equals(toSearch.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onButtonClicked(int position) {

       if(position == 0){
          setAdapter(DISTANCE);

       }
       else if(position == 1){
           setAdapter(RATING);
       }
       else if(position == 2){
           setAdapter(DATE);
       }
    }
}

class ShowOnAdapter {
    int itemType;
    String text;

    public ShowOnAdapter(int itemType, String text) {
        this.itemType = itemType;
        this.text = text;
    }

    public ShowOnAdapter() {

    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


}