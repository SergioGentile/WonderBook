package it.polito.mad.booksharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowBookFull extends AppCompatActivity {

    private TextView title, subtitle, author, publisher, description, publishDate;
    private ImageView imageBook, imageMyBook;
    private ImageButton btnEdit, btnContact;
    private RatingBar ratingBar;
    private Book book;
    private User user;
    private String key;
    private ScrollView sv;
    private Animator mCurrentAnimator;
    private ImageView expandedImage;
    private ImageButton contactUser;
    private FloatingActionButton fab;

    private Toolbar toolbar;
    private TextView available;

    private void setBitmapFromFirebase(final CircleImageView image) {

        final long time_sd = System.currentTimeMillis();

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir

        Picasso.with(ShowBookFull.this)
                .load(user.getUser_image_url()).noFade()
                .error(R.drawable.ic_error_outline_black_24dp)
                .into(image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        long time_ed = System.currentTimeMillis();
                        //Download publisher picture and save it inside the local storage
                        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();
                        ContextWrapper cw = new ContextWrapper(getApplicationContext());
                        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
                        File userPicture = new File(directory, User.profileImgName.replace("profile.", "profile_samb."));
                        FileOutputStream outStream = null;
                        try {
                            outStream = new FileOutputStream(userPicture);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                            outStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.d("Time to download", time_ed - time_sd + "");
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_my_book_full);

        title = findViewById(R.id.shTitle);
        subtitle = findViewById(R.id.shSubtitle);
        author = findViewById(R.id.shAuthor);
        publisher = findViewById(R.id.shPublisher);
        description = findViewById(R.id.shDescription);
        sv = findViewById(R.id.scrollSh);

        toolbar = findViewById(R.id.toolbarShowProfile);
        setSupportActionBar(toolbar);

        contactUser = findViewById(R.id.contact_user);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        contactUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start activity for chat
                //Check if a chat between me and the other user exist
                final User sender = getIntent().getExtras().getParcelable("user_owner");
                final User receiver = getIntent().getExtras().getParcelable("user_mp");
                //User1: the owner of the phone (sender)
                //User2: the owner of the book (receiver)
                final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats");
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String keyChat = null;
                        if (dataSnapshot.exists()) {
                            //Chat list already exist
                            //Find previous chat between peer
                            boolean found = false;
                            for (DataSnapshot dataPeer : dataSnapshot.getChildren()) {
                                Peer peer = dataPeer.getValue(Peer.class);
                                if (peer.getReceiverInformation().getKey().equals(receiver.getKey())) {
                                    //Chat already exist
                                    found = true;
                                    keyChat = dataPeer.getKey();
                                }
                            }
                            if (!found) {
                                keyChat = createInstanceOfChat(sender, receiver);
                            }
                        } else {
                            keyChat = createInstanceOfChat(sender, receiver);
                        }

                        //Here i have a chat with key keyChat
                        Intent intent = new Intent(ShowBookFull.this, ChatPage.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("sender", sender);
                        bundle.putParcelable("receiver", receiver);
                        intent.putExtra("key_chat", keyChat);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        //This part is useful when the description field over the max number of lines.
        //If the user scroll the description field, the scrollerView is blocked, and with the same principle
        //when the user scroll the scrollView the description field is blocked.
        description.setMovementMethod(new ScrollingMovementMethod());
        sv.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (description.getLineCount() >= description.getMaxLines()) {
                    description.getParent().requestDisallowInterceptTouchEvent(false);
                }


                return false;
            }
        });

        description.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (description.getLineCount() >= description.getMaxLines()) {
                    description.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        publishDate = findViewById(R.id.publishDate);
        ratingBar = findViewById(R.id.ratingBar);
        btnEdit = findViewById(R.id.btnEdit);
        btnContact = findViewById(R.id.contact_user);
        imageMyBook = findViewById(R.id.shMyImage);
        imageBook = findViewById(R.id.shImage);
        available = findViewById(R.id.tvState);
        fab = (FloatingActionButton) findViewById(R.id.fabRequest);

        //Fill all the textView of the view with the information about the book.
        CardView cvSharedBy = findViewById(R.id.card_shared_by);
        if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("book_mp") != null) {
            book = getIntent().getExtras().getParcelable("book_mp");
            user = getIntent().getExtras().getParcelable("user_mp");
            btnEdit.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            cvSharedBy.setVisibility(View.VISIBLE);
            CircleImageView imageSharedBy = findViewById(R.id.image_shared_by);
            setBitmapFromFirebase(imageSharedBy);
            TextView tvSharedByName = findViewById(R.id.name_shared_by);
            tvSharedByName.setText(user.getName().getValue() + " " + user.getSurname().getValue());
            TextView tvSharedByLocation = findViewById(R.id.location_shared_by);
            String city = book.getCity();
            if (book.getCity().length() >= 2) {
                city = book.getCity().substring(0, 1).toUpperCase() + book.getCity().substring(1);
            }
            String currentLocation = city;
            currentLocation += ", " + book.getStreet();
            tvSharedByLocation.setText(currentLocation);

            cvSharedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ShowBookFull.this, ShowProfile.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user_mp", user);
                    bundle.putParcelable("user_owner", getIntent().getExtras().getParcelable("user_owner"));
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

        } else {
            fab.setVisibility(View.GONE);
            cvSharedBy.setVisibility(View.GONE);
            book = getIntent().getParcelableExtra("book");
            user = getIntent().getParcelableExtra("user");
            key = getIntent().getExtras().getString("key");
        }

        title.setText(User.capitalizeFirst(book.getTitle()));
        if (book.getSubtitle() != null) {
            if (book.getSubtitle().isEmpty()) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(book.getSubtitle());
            }
        } else {
            subtitle.setVisibility(View.GONE);
        }
        author.setText(User.capitalizeSpace(book.getAuthor()));
        publisher.setText(User.capitalizeFirst(book.getPublisher() + ", " + book.getYear()));
        description.setText(book.getDescription());
        ratingBar.setRating(new Float(book.getRating()));
        publishDate.setText(book.getDate());
        if (book.isAvailable()) {
            available.setText(getString(R.string.available_upper));
            available.setTextColor(getColor(R.color.available));
        } else {
            available.setText(getString(R.string.unavailable_upper));
            available.setTextColor(getColor(R.color.unavailable));
        }


        Bitmap image = BitmapFactory.decodeFile(user.getImagePath());

        imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).noFade().placeholder(R.drawable.progress_animation)
                .error(R.drawable.ic_error_outline_black_24dp).into(imageMyBook, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                imageMyBook.setScaleType(ImageView.ScaleType.FIT_XY);
                imageMyBook.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        zoomImage();
                    }
                });
            }

            @Override
            public void onError() {
                imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        });

        if (book.getUrlImage() == null || book.getUrlImage().isEmpty()) {
            imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).noFade().placeholder(R.drawable.progress_animation)
                    .error(R.drawable.ic_error_outline_black_24dp).into(imageBook, new com.squareup.picasso.Callback() {
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
            imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            Picasso.with(ShowBookFull.this).load(book.getUrlImage()).noFade().placeholder(R.drawable.progress_animation).into(imageBook, new com.squareup.picasso.Callback() {
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


        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowBookFull.this, AddBook.class).putExtra("edit", true);
                Bundle bundle = new Bundle();
                bundle.putParcelable("user", user);
                bundle.putParcelable("book", book);
                intent.putExtras(bundle);
                intent.putExtra("key", key);
                startActivityForResult(intent, 0);
            }
        });

        if(fab.getVisibility() == View.VISIBLE) {
            if (book.isAvailable()) {
                fab.setVisibility(View.VISIBLE);
            } else {
                fab.setVisibility(View.GONE);
            }
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User userLogged = getIntent().getExtras().getParcelable("user_owner");
                Log.d("User", userLogged.getName().getValue());
                FirebaseDatabase.getInstance().getReference("users").child(userLogged.getKey()).child("requests").child("outcoming").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot dsReq : dataSnapshot.getChildren()) {
                                Request request = dsReq.getValue(Request.class);
                                if (request.getKeyBook().equals(book.getKey()) && request.getStatus().equals(Request.SENDED)) {
                                    Toast.makeText(ShowBookFull.this, getString(R.string.request_already_sent), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            Intent intent = new Intent(ShowBookFull.this, AddNewRequest.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("book", book);
                            bundle.putParcelable("userOwner", getIntent().getExtras().getParcelable("user_mp"));
                            bundle.putParcelable("userLogged", getIntent().getExtras().getParcelable("user_owner"));
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(ShowBookFull.this, AddNewRequest.class);
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("book", book);
                            bundle.putParcelable("userOwner", getIntent().getExtras().getParcelable("user_mp"));
                            bundle.putParcelable("userLogged", getIntent().getExtras().getParcelable("user_owner"));
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private String createInstanceOfChat(User sender, User receiver) {
        //Create chat list
        //For the user1
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(sender.getKey()).child("chats");
        DatabaseReference instanceReference1 = databaseReference.push();
        String key = instanceReference1.getKey();
        instanceReference1.setValue(new Peer(receiver, key));

        //For the receiver: i put the sender
        DatabaseReference instanceReference2 = firebaseDatabase.getReference("users").child(receiver.getKey()).child("chats").child(key);
        instanceReference2.setValue(new Peer(sender, key));

        return key;
    }

    private void zoomImage() {

        //Take the reference of the field
        expandedImage = findViewById(R.id.expanded_image_bookfull);

        BitmapDrawable drawable = (BitmapDrawable) imageMyBook.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        expandedImage.setImageBitmap(bitmap);

        //Structure I need to perform the zoom

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).

        imageMyBook.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container_bookfull)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.

        ScrollView cv = findViewById(R.id.scrollSh);
        cv.setVisibility(View.GONE);
        expandedImage.setVisibility(View.VISIBLE);


        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImage.setPivotX(0f);
        expandedImage.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImage, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImage,
                        View.SCALE_Y, startScale, 1f));
        set.setDuration(100);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        expandedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImage, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImage,
                                        View.Y, startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImage,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImage,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(100);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        imageMyBook.setImageDrawable(expandedImage.getDrawable());
                        expandedImage.setVisibility(View.INVISIBLE);
                        ScrollView cv = findViewById(R.id.scrollSh);
                        cv.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        imageMyBook.setImageDrawable(expandedImage.getDrawable());
                        expandedImage.setVisibility(View.INVISIBLE);
                        ScrollView cv = findViewById(R.id.scrollSh);
                        cv.setVisibility(View.VISIBLE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data.getExtras().getBoolean("cancelled", false)) {
                finish();
            }
            if (data.getExtras().getBoolean("modified", false)) {
                Book bookModified = data.getExtras().getParcelable("book");
                title.setText(User.capitalizeFirst(bookModified.getTitle()));
                if (bookModified.getSubtitle() != null) {
                    if (bookModified.getSubtitle().isEmpty()) {
                        subtitle.setVisibility(View.GONE);

                    } else {
                        subtitle.setVisibility(View.VISIBLE);
                    }
                    book.setSubtitle(bookModified.getSubtitle());
                    subtitle.setText(bookModified.getSubtitle());
                } else {
                    subtitle.setVisibility(View.GONE);
                }
                author.setText(User.capitalizeSpace(bookModified.getAuthor()));
                publisher.setText(User.capitalizeFirst(bookModified.getPublisher() + ", " + bookModified.getYear()));
                description.setText(bookModified.getDescription());
                ratingBar.setRating(new Float(bookModified.getRating()));
                if (bookModified.isAvailable()) {
                    available.setText(getString(R.string.available_upper));
                    available.setTextColor(getColor(R.color.available));
                } else {
                    available.setText(getString(R.string.unavailable_upper));
                    available.setTextColor(getColor(R.color.unavailable));
                }
                Bitmap image = BitmapFactory.decodeFile(user.getImagePath());

                book.setTitle(bookModified.getTitle());
                book.setAuthor(bookModified.getAuthor());
                book.setSubtitle(bookModified.getSubtitle());
                book.setIsbn13(bookModified.getIsbn13());
                book.setIsbn10(bookModified.getIsbn10());
                book.setYear(bookModified.getYear());
                book.setDescription(bookModified.getDescription());
                book.setOwner(bookModified.getOwner());
                book.setPublisher(bookModified.getPublisher());
                book.setRating(bookModified.getRating());
                book.setUrlImage(bookModified.getUrlImage());
                book.setUrlMyImage(bookModified.getUrlMyImage());
                book.setAvailable(bookModified.isAvailable());
                book.setCap(bookModified.getCap());
                book.setCity(bookModified.getCity());
                book.setStreet(bookModified.getStreet());
                book.setOwnerName(bookModified.getOwnerName());

                //Set the images of the book
                imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).noFade().placeholder(R.drawable.progress_animation)
                        .error(R.drawable.ic_error_outline_black_24dp).into(imageMyBook, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        imageMyBook.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageMyBook.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                zoomImage();
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                });


                if (book.getUrlImage().isEmpty()) {
                    imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).noFade().placeholder(R.drawable.progress_animation)
                            .error(R.drawable.ic_error_outline_black_24dp).into(imageBook, new com.squareup.picasso.Callback() {
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


            }
        } else if (RESULT_CANCELED == resultCode) {
            Toast.makeText(ShowBookFull.this, getString(R.string.error_reload_new_book), Toast.LENGTH_SHORT);
        }
    }
}