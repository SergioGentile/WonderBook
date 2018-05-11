package it.polito.mad.booksharing;

import android.*;
import android.Manifest;
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
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import android.widget.ProgressBar;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainPage extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, DialogOrderType.BottomSheetListener, LocationListener {

    private boolean firtTime = true;
    private User user;
    private TextView tvName;
    private CircleImageView profileImage;
    private ImageView userImage,userImageOriginal;
    private View navView;
    private String userId;
    private FirebaseAuth mAuth;
    private MapView mMapView;
    private MaterialSearchView searchView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private final static int AUTHOR = 0, TITLE = 1, ANY = 2, PUBLISHER = 3, ISBN = 4, CITY = 5, OWNER = 6;
    private final static int DISTANCE = 0, RATING = 1, NO_ORDER = 2, DATE = 3, YOUR_CITY = 4;
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
    CopyOnWriteArrayList<User> usersDownload;
    ArrayList<String> bookIds;
    GoogleMap map;
    HashMap<String, Marker> markers;
    boolean submit;
    int searchBarItem;
    int counter_location;
    private int tabFieldSearch;
    private String runTimeQuery;
    private ImageView imageScanOnSearch;
    private TextView orderDialog, tvOrderType;
    private ImageView imageView;

    double latPhone, longPhone;
    private boolean tabFlag = false;
    private LinearLayout emptyResearch;
    private ProgressBar progressAnimation;
    private List<SortedLocationItem> sortedLocationItems;
    LocationManager locationManager;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();

        //Ask permission for editing photo
        ActivityCompat.requestPermissions(MainPage.this,
                new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        setContentView(R.layout.activity_main_page);
        searchView = findViewById(R.id.search_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final TabLayout tabLayout = findViewById(R.id.tabs);
        userImage = new ImageView(MainPage.this);
        userImageOriginal = new ImageView(MainPage.this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // navView = getLayoutInflater().inflate(R.layout.nav_header_main_page, null);
        navView = navigationView.getHeaderView(0);


        setNotification(12);


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
        emptyResearch = (LinearLayout) findViewById(R.id.emptyResearch);
        progressAnimation = (ProgressBar) findViewById(R.id.progressAnimation);
        sortedLocationItems = new ArrayList<>();
        books = new ArrayList<>();
        booksQuery = new ArrayList<>();
        booksMatch = new ArrayList<>();
        stringAlreadyMatched = new ArrayList<>();
        stringRuntime = new ArrayList<>();
        bookIds = new ArrayList<>();
        usersDownload = new CopyOnWriteArrayList<>();
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
                    imageScanOnSearch.setVisibility(View.GONE);
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, TITLE);
                    }
                } else if (tab.getPosition() == 2) {
                    tabFieldSearch = AUTHOR;
                    imageScanOnSearch.setVisibility(View.GONE);
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, AUTHOR);
                    }
                } else if (tab.getPosition() == 3) {
                    tabFieldSearch = PUBLISHER;
                    imageScanOnSearch.setVisibility(View.GONE);
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, PUBLISHER);
                    }
                } else if (tab.getPosition() == 4) {
                    imageScanOnSearch.setVisibility(View.VISIBLE);
                    tabFieldSearch = ISBN;
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, ISBN);
                    }
                } else if (tab.getPosition() == 5) {
                    imageScanOnSearch.setVisibility(View.GONE);
                    tabFieldSearch = CITY;
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, CITY);
                    }
                } else if (tab.getPosition() == 6) {
                    tabFieldSearch = OWNER;
                    imageScanOnSearch.setVisibility(View.GONE);
                    if (tabLayout.getVisibility() == View.VISIBLE) {
                        setAdapterRuntime(runTimeQuery, OWNER);
                    }
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


        NotificationIDService.sendRegistrationToServer();
    }

    //evaluate the distance between two point (latitude, longitude) in KM
    private double distanceLocation(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; //*1.609344

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

    private void setNotification(Integer notificaction_count) {

        TextView toolbarNotification = findViewById(R.id.tv_nav_drawer_notification);
        if(notificaction_count!=0) {
            TextView message_nav_bar = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_show_chat));

            //Set current notification inside initNavBar method
            message_nav_bar.setGravity(Gravity.CENTER_VERTICAL);
            message_nav_bar.setTypeface(null, Typeface.BOLD);
            message_nav_bar.setTextColor(getResources().getColor(R.color.colorAccent));
            message_nav_bar.setText(notificaction_count.toString());

            //Set notification on toolbar icon


            toolbarNotification.setText(notificaction_count.toString());
            toolbarNotification.setVisibility(View.VISIBLE);
        }else{
            toolbarNotification.setVisibility(View.GONE);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Take the ISBN of the camera scan in order to facility the search operation
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

        //Save the list of the listView
        outState.putParcelableArrayList("booksMatch", booksMatch);
        outState.putBoolean("firstTime", firtTime);
    }


    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        //Restore the list of the listView
        booksMatch = inState.getParcelableArrayList("booksMatch");
        firtTime = inState.getBoolean("firstTime");
        setAdapter(DISTANCE);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMaxZoomPreference(18);
        enableMyLocationIfPermitted(map);
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

    private void enableMyLocationIfPermitted(GoogleMap map) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else if (map != null) {
            map.setMyLocationEnabled(true);
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
        else if (id == R.id.nav_show_chat) {
            //Start the intent
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", user);
            startActivity(new Intent(MainPage.this, ShowMessageThread.class).putExtras(bundle));
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

            startActivity(new Intent(MainPage.this, Start.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

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
                        setUserInfoNavBar();
                        if (firtTime) {
                            setAdapterSearchedRecentAdd();
                            firtTime = false;
                        }

                        //Here get the position and store it on latPhone and longPhone

                        //*POSIZIONE*
                        //Qua viene cercata la posizione attuale.
                        //Bisogna gestire i permessi.
                        //Se non si ha il permesso entra nel primo ramo dell'if e calcola la distanza libro/posizione rispetto alla posizione
                        //che si ha sulla showProfile.
                        Log.d("Search location for:", user.getName().getValue());
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (ActivityCompat.checkSelfPermission(MainPage.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainPage.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d("Permission dis-enabled:", user.getName().getValue());
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Geocoder geocoder = new Geocoder(MainPage.this);
                            List<Address> addresses;
                            String location = user.getStreet().getValue() + " " + user.getCap().getValue() + " " + user.getCity().getValue();
                            try {
                                addresses = geocoder.getFromLocationName(location, 1);
                                if (addresses.size() > 0) {
                                    latPhone = addresses.get(0).getLatitude();
                                    longPhone = addresses.get(0).getLongitude();
                                } else {
                                    latPhone = -1;
                                    longPhone = -1;
                                }
                            } catch (Exception e) {
                                Toast.makeText(MainPage.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT);
                                latPhone = -1;
                                longPhone = -1;
                            }
                        } else {
                            Log.d("Permission enabled:", user.getName().getValue());
                            try {

                                if(locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER)){
                                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, MainPage.this);
                                    Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
                                    onLocationChanged(location);
                                    Log.d("Position:", location.getLatitude() + " " + location.getLongitude());
                                }
                                else if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainPage.this);
                                    Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
                                    onLocationChanged(location);
                                    Log.d("Position:", location.getLatitude() + " " + location.getLongitude());
                                }
                                else{
                                    Log.d("pos","Take permission from edit prof");
                                    Geocoder geocoder = new Geocoder(MainPage.this);
                                    List<Address> addresses;
                                    String location = user.getStreet().getValue() + " " + user.getCap().getValue() + " " + user.getCity().getValue();
                                    try {
                                        addresses = geocoder.getFromLocationName(location, 1);
                                        if (addresses.size() > 0) {
                                            latPhone = addresses.get(0).getLatitude();
                                            longPhone = addresses.get(0).getLongitude();
                                        }
                                    } catch (Exception e1) {
                                        latPhone = -1;
                                        longPhone = -1;
                                        Toast.makeText(MainPage.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT);
                                    }
                                }
                            } catch (Exception e) {
                                Log.d("Exc","Exception catch");
                                e.printStackTrace();
                                Geocoder geocoder = new Geocoder(MainPage.this);
                                List<Address> addresses;
                                String location = user.getStreet().getValue() + " " + user.getCap().getValue() + " " + user.getCity().getValue();
                                try {
                                    addresses = geocoder.getFromLocationName(location, 1);
                                    if (addresses.size() > 0) {
                                        latPhone = addresses.get(0).getLatitude();
                                        longPhone = addresses.get(0).getLongitude();
                                    }
                                } catch (Exception e1) {
                                    latPhone = -1;
                                    longPhone = -1;
                                    Toast.makeText(MainPage.this, getString(R.string.own_pos_not_found), Toast.LENGTH_SHORT);
                                }
                            }
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

        if (!user.getUser_image_url().isEmpty()) {

            //Download the user picture and save it inside the local storage
            Picasso.with(MainPage.this)
                    .load(user.getUser_image_url()).noFade()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(profileImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {

                            BitmapDrawable drawable = (BitmapDrawable) profileImage.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
                            if (!directory.exists()) {
                                directory.mkdir();
                            }
                            File userPicture = new File(directory, User.profileImgName);
                            FileOutputStream outStream = null;
                            try {
                                outStream = new FileOutputStream(userPicture);
                                bitmap.compress(User.COMPRESS_FORMAT_BIT, User.IMAGE_QUALITY, outStream);

                                outStream.flush();
                                outStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError() {
                            userImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }
                    });


            //Download the original user picture and save in inside the local storage

            Picasso.with(MainPage.this)
                    .load(user.getCropped_image_url()).noFade()
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(userImageOriginal, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            BitmapDrawable drawable = (BitmapDrawable) userImageOriginal.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();
                            ContextWrapper cw = new ContextWrapper(getApplicationContext());
                            File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
                            File userPicture = new File(directory, User.profileImgNameCrop);
                            FileOutputStream outStream = null;
                            try {
                                outStream = new FileOutputStream(userPicture);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                                outStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onError() {
                            userImageOriginal.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }
                    });

        }
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


    //Set the listView of the search result
    //searchedString is the string that the user query.
    private void setAdapterSearched(final String searchedString) {
        //qui reference
        emptyResearch.setVisibility(View.GONE);
        progressAnimation.setVisibility(View.VISIBLE);
        DatabaseReference databaseReferenceBooks = firebaseDatabase.getReference("books");
        //Depends on the query type, I perform a different query
        Query query = null;
        if (searchBarItem == TITLE) {
            query = databaseReferenceBooks.orderByChild("title").equalTo(searchedString);
        } else if (searchBarItem == AUTHOR) {
            query = databaseReferenceBooks.orderByChild("author").equalTo(searchedString);
        } else if (searchBarItem == PUBLISHER) {
            query = databaseReferenceBooks.orderByChild("publisher").equalTo(searchedString);
        } else if (searchBarItem == OWNER) {
            query = databaseReferenceBooks.orderByChild("ownerName").equalTo(searchedString);
        } else if (searchBarItem == ISBN) {
            if (searchedString.length() == 10) {
                query = databaseReferenceBooks.orderByChild("isbn10").equalTo(searchedString);
            } else {
                query = databaseReferenceBooks.orderByChild("isbn13").equalTo(searchedString);
            }
        } else if (searchBarItem == CITY) {
            query = databaseReferenceBooks.orderByChild("city").equalTo(searchedString);
        }

        if (query == null) {
            progressAnimation.setVisibility(View.GONE);
            emptyResearch.setVisibility(View.VISIBLE);
            return;
        }

        //Ask the result to firebase and wait
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Reset all the list
                lv_searched.setAdapter(null);
                booksMatch.clear();
                bookIds.clear();
                map.clear();
                //Unmarshall al the book and put it into the list booksMatch.
                //Save the id into bookIds and avoid the one of the user that is using the application
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (!issue.getValue(Book.class).getOwner().equals(user.getKey())) {
                            booksMatch.add(issue.getValue(Book.class));
                            bookIds.add(issue.getKey());
                        }
                    }
                } else {
                    progressAnimation.setVisibility(View.GONE);
                    emptyResearch.setVisibility(View.VISIBLE);
                }
                if (booksMatch.isEmpty()) {
                    progressAnimation.setVisibility(View.GONE);
                    emptyResearch.setVisibility(View.VISIBLE);
                }
                usersDownload.clear();
                //Download all the users that are owner of the book
                for (Book bookToAdd : booksMatch) {
                    DatabaseReference databaseReferenceUsers = firebaseDatabase.getReference("users");
                    Query queryUser = databaseReferenceUsers.child(bookToAdd.getOwner());
                    queryUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            usersDownload.add(dataSnapshot.getValue(User.class));
                            //when all the user are been downloaded, start downloading the position of the books
                            if (usersDownload.size() == booksMatch.size()) {

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
                                                String snippet = getString(R.string.shared_by) + " " + User.capitalizeSpace(booksMatch.get(i).getOwnerName());
                                                //return a new lat and long in order to avoid overlap. The new lat/long is near the previous one and it is choose in a random way
                                                Position overlap = avoidOverlap(markers.values(), new Position(location.latitude, location.longitude));
                                                Marker m = map.addMarker(new MarkerOptions().position(new LatLng(overlap.latitude, overlap.longitude)).title(User.capitalizeFirst(booksMatch.get(i).getTitle())).snippet(snippet));
                                                markers.put(key, m);
                                                //Evaluate distance for the bok if latPhone and longPhone are available.

                                                if (latPhone != -1 && longPhone != -1) {
                                                    booksMatch.get(i).setDistance(distanceLocation(latPhone, longPhone, location.latitude, location.longitude));
                                                } else {
                                                    booksMatch.get(i).setDistance(-1);
                                                }

                                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                for (Marker marker : markers.values()) {
                                                    builder.include(marker.getPosition());
                                                }
                                                LatLngBounds bounds = builder.build();
                                                int width = getResources().getDisplayMetrics().widthPixels;
                                                int height = getResources().getDisplayMetrics().heightPixels;
                                                int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

                                                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(latPhone, longPhone), 12);
                                                map.animateCamera(cu);


                                            } else {
                                                booksMatch.get(i).setDistance(-1);
                                            }
                                            if (counter_location == booksMatch.size()) {
                                                //Qui si setta l'adapter della list view
                                                //Sort adapter
                                                progressAnimation.setVisibility(View.GONE);
                                                setAdapter(DISTANCE);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            progressAnimation.setVisibility(View.GONE);
                                            emptyResearch.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            emptyResearch.setVisibility(View.VISIBLE);
                            progressAnimation.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                emptyResearch.setVisibility(View.VISIBLE);
                progressAnimation.setVisibility(View.GONE);
            }
        });
    }


    //This function return a position near p but not the same.
    //In particoular check if the same position exist in the list markerList and add a random number to lat and longit.
    private Position avoidOverlap(Collection<Marker> markerList, Position p) {

        boolean found = false;
        int counter = 0; //Need to avoid deadlock
        while (!found && counter < 20) {
            found = true;
            for (Marker marker : markerList) {
                counter++;
                if (marker.getPosition().latitude == p.getLatitude() && marker.getPosition().longitude == p.getLongitude()) {
                    found = false;
                    //Add a number between 0.000000/0.000100
                    double lat, longit;
                    lat = getRandom(0, counter % 5) / (double) 100000;
                    longit = getRandom(0, counter % 5) / (double) 10000;
                    p.setLatitude(p.getLatitude() + lat);
                    p.setLongitude(p.getLongitude() + longit);
                }
            }
        }
        return p;
    }

    //Get a random number between max and min
    private int getRandom(int min, int max) {
        Random r = new Random();
        while (max <= min) {
            max++;
        }
        return r.nextInt(max - min) + max;
    }

    //This adapter is the same of setAdapterSearched, but in this case return all the near books (with respect the current position)
    //recently added
    private void setAdapterSearchedRecentAdd() {
        //qui reference
        emptyResearch.setVisibility(View.GONE);
        progressAnimation.setVisibility(View.VISIBLE);
        DatabaseReference databaseReferenceBooks = firebaseDatabase.getReference("books");
        Query query = null;
        //query only the latest books
        query = databaseReferenceBooks.orderByChild("date").limitToLast(20);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Clear all the list
                lv_searched.setAdapter(null);
                booksMatch.clear();
                bookIds.clear();
                markers.clear();
                map.clear();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        if (!issue.getValue(Book.class).getOwner().equals(user.getKey())) {
                            booksMatch.add(issue.getValue(Book.class));
                            bookIds.add(issue.getKey());
                        }
                    }
                } else {
                    progressAnimation.setVisibility(View.GONE);
                    emptyResearch.setVisibility(View.VISIBLE);
                }
                if (booksMatch.isEmpty()) {
                    progressAnimation.setVisibility(View.GONE);
                    emptyResearch.setVisibility(View.VISIBLE);
                }
                usersDownload.clear();
                //Unmarshall al the book and put it into the list booksMatch.
                //Save the id into bookIds and avoid the one of the user that is using the application
                for (final Book bookToAdd : booksMatch) {
                    DatabaseReference databaseReferenceUsers = firebaseDatabase.getReference("users");
                    Query queryUser = databaseReferenceUsers.child(bookToAdd.getOwner());
                    queryUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            //Download all the users that are owner of the book
                            usersDownload.add(dataSnapshot.getValue(User.class));
                            if (usersDownload.size() == booksMatch.size()) {

                                DatabaseReference databaseReferenceLocation = firebaseDatabase.getReference("locations").child("books");
                                GeoFire geoFire = new GeoFire(databaseReferenceLocation);

                                counter_location = 0;
                                for (int pos = 0; pos < booksMatch.size(); pos++) {
                                    final int i = pos;
                                    sortedLocationItems.clear();
                                    geoFire.getLocation(bookIds.get(pos), new LocationCallback() {
                                        @Override
                                        public void onLocationResult(String key, GeoLocation location) {
                                            counter_location++;
                                            if (location != null) {
                                                String snippet = getString(R.string.shared_by) + " " + User.capitalizeSpace(booksMatch.get(i).getOwnerName());
                                                if (latPhone != -1 && longPhone != -1) {
                                                    booksMatch.get(i).setDistance(distanceLocation(latPhone, longPhone, location.latitude, location.longitude));
                                                } else {
                                                    booksMatch.get(i).setDistance(-1);
                                                }
                                                sortedLocationItems.add(new SortedLocationItem(booksMatch.get(i), snippet, location.latitude, location.longitude));
                                            } else {
                                                booksMatch.get(i).setDistance(-1);
                                            }
                                            if (counter_location == booksMatch.size()) {
                                                //Here i fill all the list, so i have:
                                                //-the list of user
                                                //-the list of book of each user
                                                //-the position of each book
                                                //Store all in SortedLocationItem. This class allow to sort all the three list in the same way.
                                                List<SortedLocationItem> tmp = new ArrayList<>();
                                                for (int i = 0; i < sortedLocationItems.size(); i++) {
                                                    if (sortedLocationItems.get(i).getBook().getDistance() < 20 && sortedLocationItems.get(i).getBook().getDistance() >= 0) {
                                                        tmp.add(sortedLocationItems.get(i));
                                                    }
                                                }

                                                //Sort all the books with respect the date and, if the date is the same, with respect to the distance
                                                Collections.sort(tmp, new Comparator<SortedLocationItem>() {
                                                    @Override
                                                    public int compare(SortedLocationItem s1, SortedLocationItem s2) {
                                                        if (s1.getBook().getDate().compareTo(s2.getBook().getDate()) != 0) {
                                                            return -s1.getBook().getDate().compareTo(s2.getBook().getDate());
                                                        } else {
                                                            return s1.getBook().getDistance().compareTo(s2.getBook().getDistance());
                                                        }
                                                    }
                                                });
                                                sortedLocationItems.clear();
                                                booksMatch.clear();
                                                //Take only the first ten (i decide to show only the first ten recently updated book )
                                                for (int i = 0; i < tmp.size() && i < 8; i++) {
                                                    sortedLocationItems.add(tmp.get(i));
                                                    booksMatch.add(tmp.get(i).getBook());
                                                }

                                                //Set the right marker on the map for the selected books.
                                                markers.clear();
                                                for (SortedLocationItem sortedLocationItem : sortedLocationItems) {
                                                    Position overlap = avoidOverlap(markers.values(), new Position(sortedLocationItem.getLatitude(), sortedLocationItem.getLongitude()));
                                                    Marker m = map.addMarker(new MarkerOptions().position(new LatLng(overlap.getLatitude(), overlap.getLongitude())).title(User.capitalizeFirst(sortedLocationItem.getBook().getTitle())).snippet(sortedLocationItem.getSnippet()));
                                                    markers.put(key, m);

                                                    //Evaluate distance for the bok

                                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                    for (Marker marker : markers.values()) {
                                                        builder.include(marker.getPosition());
                                                    }
                                                    LatLngBounds bounds = builder.build();
                                                    int width = getResources().getDisplayMetrics().widthPixels;
                                                    int height = getResources().getDisplayMetrics().heightPixels;
                                                    int padding = (int) (width * 0.20); // offset from edges of the map 10% of screen

                                                    CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(latPhone, longPhone), 12);

                                                    map.animateCamera(cu);
                                                }
                                                progressAnimation.setVisibility(View.GONE);
                                                setAdapter(NO_ORDER);

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            progressAnimation.setVisibility(View.GONE);
                                            emptyResearch.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            emptyResearch.setVisibility(View.VISIBLE);
                            progressAnimation.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                emptyResearch.setVisibility(View.VISIBLE);
                progressAnimation.setVisibility(View.GONE);
            }
        });
    }


    private void setAdapter(int order) {

        //Set the adapter.
        //before to fill all the list view, i sort it in the specified order.
        if (order == DISTANCE) {
            tvOrderType.setText(getString(R.string.closest_to_you));
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return b1.getDistance().compareTo(b2.getDistance());
                }
            });
        } else if (order == RATING) {
            tvOrderType.setText(getString(R.string.most_rating));
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return -b1.getRating().toString().compareTo(b2.getRating().toString());
                }
            });
        } else if (NO_ORDER == order) {
            tvOrderType.setText(getString(R.string.latest_releases_near_you));
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return b1.getDistance().compareTo(b2.getDistance());
                }
            });
        } else if (DATE == order) {
            tvOrderType.setText(R.string.latest_releases);
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return -b1.getDate().toString().compareTo(b2.getDate().toString());
                }
            });
        }
        /*else if (YOUR_CITY == order) {
            tvOrderType.setText(R.string.in_your_city);
            //Choose only the book in our city
            List<Book> tmp = new ArrayList<>();
            for(int i=0; i<bookIds.size(); i++) {
                Geocoder geocoder = new Geocoder(MainPage.this);
                List<Address> addresses;
                try {
                    addresses = geocoder.getFromLocation(markers.get(bookIds.get(i)).getPosition().latitude, markers.get(bookIds.get(i)).getPosition().longitude, 1);
                    Log.d("The locality is: " , addresses.get(0).getLocality());
                    if (addresses.size() > 0) {
                        if (addresses.get(0).getLocality().toLowerCase().equals(user.getCity().getValue().toLowerCase())) {
                            tmp.add(booksMatch.get(i));
                        }
                    }
                } catch (Exception e) {

                }
            }

            booksMatch.clear();
            for(Book book : tmp){
                booksMatch.add(book);
            }
            Collections.sort(booksMatch, new Comparator<Book>() {

                @Override
                public int compare(Book b1, Book b2) {
                    return b1.getDistance().compareTo(b2.getDistance());
                }
            });
        }*/


        final List<String> colors = new ArrayList<>();
        colors.add(new String("#42A5F5"));
        colors.add(new String("#009688"));
        colors.add(new String("#5C6BC0"));


        if (booksMatch.size() == 0) {
            progressAnimation.setVisibility(View.GONE);
            emptyResearch.setVisibility(View.VISIBLE);
        } else {
            progressAnimation.setVisibility(View.GONE);
            emptyResearch.setVisibility(View.GONE);
        }

        //Set the adapter
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
                //fill all the layout
                final Book book = booksMatch.get(position);
                imageView = (ImageView) convertView.findViewById(R.id.image_book_searched);
                Picasso.with(MainPage.this).load(book.getUrlMyImage())
                        .error(R.drawable.ic_error_outline_black_24dp).noFade().into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

                    @Override
                    public void onError() {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                });

                TextView title = (TextView) convertView.findViewById(R.id.title_searched);
                TextView author = (TextView) convertView.findViewById(R.id.author_searched);
                RatingBar rb = (RatingBar) convertView.findViewById(R.id.rating_searched);
                //For setting a text view with two different colors
                TextView owner = (TextView) convertView.findViewById(R.id.shared_name);
                owner.setText(getString(R.string.shared_by) + " " + User.capitalizeSpace(book.getOwnerName()));

                TextView distance = (TextView) convertView.findViewById(R.id.distance);
                LinearLayout ll_distance = (LinearLayout) convertView.findViewById(R.id.ll_location);

                //If the position is not available for some reason (because no permission, because the city wrote by the user is wrong)
                //i hide the evaluation of the distance
                if (book.getDistance() >= 0.0) {
                    ll_distance.setVisibility(View.VISIBLE);
                    distance.setText(Double.toString(book.getDistance()));
                } else {
                    ll_distance.setVisibility(View.GONE);
                }


                title.setText(User.capitalizeFirst(book.getTitle()));
                author.setText(User.capitalizeSpace(book.getAuthor()));
                rb.setRating(new Float(book.getRating()));

               /*CardView cv = (CardView) convertView.findViewById(R.id.adapter_cv_searched);
                cv.setCardBackgroundColor(Color.parseColor(colors.get(position % colors.size())));*/


                LinearLayout llAdapter = (LinearLayout) convertView.findViewById(R.id.ll_adapter_searched_book);

                //If the item is clicked, i show the book
                llAdapter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                        DatabaseReference child = reference.child("users").child(book.getOwner());
                        child.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Intent intent = new Intent(MainPage.this, ShowBookFull.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("book_mp", book);
                                User currentUser = dataSnapshot.getValue(User.class);
                                bundle.putParcelable("user_mp", currentUser);
                                bundle.putParcelable("user_owner", user);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                });

                return convertView;

            }
        });

    }

    //Concatenate all the field of a book
    private String concatenateFieldBook(Book book) {
        return (book.getAuthor() + " " + book.getTitle() + " " + book.getIsbn10() + " " + book.getIsbn13() + " " + book.getPublisher() + " " + book.getCity() + " " + book.getOwnerName()).toLowerCase();
    }

    //Starting from the query string, this function understand what kind of element is searched and show all the possible choices
    //in a list view under the search bar
    private void setAdapterRuntime(final String searchedString, final int tabField) {
        //Query for all the book on the database
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
                    //For each book downloaded, understand if it match with the string searched or not
                    Book book = snapshot.getValue(Book.class);
                    //depends on the filter applied (no one, title, author ecc...), i will try to find a new match depends on it
                    //i use the list booksQuery and books in order to avoid that the same book is show more than one time on the list
                    //So i keep track of all the book showed before
                    if (tabField == ANY) {
                        if (concatenateFieldBook(book).contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(concatenateFieldBook(book), books, ANY)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    } else if (tabField == TITLE) {
                        if (book.getTitle().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(book.getTitle().toLowerCase(), books, TITLE)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    } else if (tabField == AUTHOR) {
                        if (book.getAuthor().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(book.getAuthor().toLowerCase(), books, AUTHOR)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    } else if (tabField == PUBLISHER) {
                        if (book.getPublisher().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(book.getPublisher().toLowerCase(), books, PUBLISHER)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    } else if (tabField == CITY) {
                        if (book.getCity().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(book.getCity().toLowerCase(), books, CITY)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    } else if (tabField == OWNER) {
                        if (book.getOwnerName().toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(book.getOwner().toLowerCase(), books, OWNER)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    } else if (tabField == ISBN) {

                        String isbn = new String("");
                        if (book.getIsbn13().contains(searchedString.toLowerCase())) {
                            isbn = book.getIsbn13();
                        } else {
                            isbn = book.getIsbn10();
                        }

                        if (isbn.toLowerCase().contains(searchedString.toLowerCase()) && !book.getOwner().equals(user.getKey())) {
                            if (!stringAlreadyPresentOnBookList(isbn.toLowerCase(), books, ISBN)) {
                                books.add(book);
                                booksQuery.add(searchedString.toLowerCase());
                            }
                        }
                    }
                }


                //set element to show in adapter.
                stringAlreadyMatched.clear();
                stringRuntime.clear();

                //finally, i have in books all the possible match
                //i store in searchItem the kind of item that i'm searching.
                //If a filter is applied, searchItem will be equal to the filter, instead the function understandSearchItem
                //will understand the kind of item from itself, and it will suggest the user in the choice
                for (int i = 0; i < books.size(); i++) {
                    //Understand the tipology of the item
                    int searchItem;
                    if (tabField == ANY) {
                        searchItem = understandSearchItem(booksQuery.get(i), books.get(i));
                    } else {
                        searchItem = tabField;
                    }
                    String itemString = new String("");
                    switch (searchItem) {
                        case TITLE:
                            itemString = books.get(i).getTitle();
                            break;
                        case AUTHOR:
                            itemString = books.get(i).getAuthor();
                            break;
                        case PUBLISHER:
                            itemString = books.get(i).getPublisher();
                            break;
                        case OWNER:
                            itemString = books.get(i).getOwnerName();
                            break;
                        case CITY:
                            itemString = books.get(i).getCity();
                            break;
                        case ISBN:
                            if (!books.get(i).getIsbn13().isEmpty()) {
                                itemString = books.get(i).getIsbn13();
                            } else {
                                itemString = books.get(i).getIsbn10();
                            }
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


                //Reduce the size of the list (i decide to show only 4 possible books  )
                List<ShowOnAdapter> tmp = new ArrayList<>();
                for (int i = 0; i < 4 && i < stringRuntime.size(); i++) {
                    tmp.add(stringRuntime.get(i));
                }
                stringRuntime.clear();
                for (ShowOnAdapter soa : tmp) {
                    stringRuntime.add(soa);
                }


                //So here i have in stringRuntime 4 possible match. Now I'm ready to set the adapter
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

                        //Depends on what the user search, I set a different image
                        Drawable d = null;
                        textView.setText(stringRuntime.get(position).getText());
                        if (stringRuntime.get(position).getItemType() == AUTHOR) {
                            d = getDrawable(R.drawable.writer);
                        }
                        if (stringRuntime.get(position).getItemType() == OWNER) {
                            d = getDrawable(R.drawable.ic_person_black_24dp);
                        } else if (stringRuntime.get(position).getItemType() == TITLE) {
                            d = getDrawable(R.drawable.book_open_page);
                        } else if (stringRuntime.get(position).getItemType() == CITY) {
                            d = getDrawable(R.drawable.ic_location_city_black_24dp);
                        } else if (stringRuntime.get(position).getItemType() == PUBLISHER) {
                            d = getDrawable(R.drawable.factory);
                        } else if (stringRuntime.get(position).getItemType() == ISBN) {
                            Drawable drawable = getDrawable(R.drawable.barcode);
                            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
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

    //This function understand what kind of item is searching the user
    private int understandSearchItem(String query, Book book) {
        query = query.toLowerCase();
        if (book.getTitle().toLowerCase().contains(query)) {
            return TITLE;
        } else if (book.getAuthor().toLowerCase().contains(query)) {
            return AUTHOR;
        } else if (book.getCity().toLowerCase().contains(query)) {
            return CITY;
        } else if (book.getOwnerName().toLowerCase().contains(query)) {
            return OWNER;
        } else if (book.getPublisher().toLowerCase().contains(query)) {
            return PUBLISHER;
        } else if (book.getIsbn10().toLowerCase().contains(query) || book.getIsbn13().toLowerCase().contains(query)) {
            return ISBN;
        }
        return ANY;

    }


    //check if the string is already present on the list books
    private boolean stringAlreadyPresentOnBookList(String toSearch, ArrayList<Book> books, int type) {

        for (Book book : books) {
            if (concatenateFieldBook(book).toLowerCase().equals(toSearch.toLowerCase()) && type == ANY) {
                return true;
            } else if (book.getTitle().toLowerCase().equals(toSearch.toLowerCase()) && type == TITLE) {
                return true;
            } else if (book.getAuthor().toLowerCase().equals(toSearch.toLowerCase()) && type == AUTHOR) {
                return true;
            } else if (book.getOwnerName().toLowerCase().equals(toSearch.toLowerCase()) && type == OWNER) {
                return true;
            } else if (book.getCity().toLowerCase().equals(toSearch.toLowerCase()) && type == CITY) {
                return true;
            } else if (book.getPublisher().toLowerCase().equals(toSearch.toLowerCase()) && type == PUBLISHER) {
                return true;
            } else if ((book.getIsbn10().toLowerCase().equals(toSearch.toLowerCase()) || book.getIsbn13().toLowerCase().equals(toSearch.toLowerCase())) && type == ISBN) {
                return true;
            }


        }
        return false;
    }

    //Check if the string is already present on a list of strings
    private boolean stringAlreadyPresentOnStringList(String toSearch, ArrayList<String> strings) {
        for (String string : strings) {
            if (string.toLowerCase().equals(toSearch.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    //when the user click the dialog, choose a new order
    @Override
    public void onButtonClicked(int position) {

        if (position == 0) {
            setAdapter(DISTANCE);
        } else if (position == 1) {
            setAdapter(RATING);
        } else if (position == 2) {
            setAdapter(DATE);
        } else if (position == 3) {
            setAdapter(YOUR_CITY);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latPhone = location.getLatitude();
        longPhone = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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

//This class incapsulate different things in order to help me in the sort operation
class Position {
    double latitude, longitude;

    public Position() {
    }

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}


//This class incapsulate different things in order to help me in the sort operation
class SortedLocationItem {
    Book book;
    String snippet;
    double latitude, longitude;

    public SortedLocationItem(Book book, String snippet, double latitude, double longitude) {
        this.book = book;
        this.snippet = snippet;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}

