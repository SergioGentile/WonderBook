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
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowBookFull extends AppCompatActivity {

    private TextView title, subtitle, author, publisher, description, publishDate;
    private ImageView imageBook, imageMyBook;
    private ImageButton btnEdit;
    private RatingBar ratingBar;
    private Book book;
    private User user;
    private String key;
    private ScrollView sv;
    private Animator mCurrentAnimator;
    private ImageView expandedImage;

    private Toolbar toolbar;
    private TextView available;

    private void setBitmapFromFirebase(final CircleImageView image){

        final long time_sd= System.currentTimeMillis();
        StorageReference riversRef = FirebaseStorage.getInstance().getReference();
        StorageReference userPictureRef = riversRef.child("userImgProfile/" + user.getKey() + "/picture." + User.COMPRESS_FORMAT_STR);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(User.imageDir, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            return;
        }
        //Create of the destination path
        File userPicture = new File(directory, User.profileImgName.replace("profile.", "profile_samb."));
        userPictureRef.getFile(userPicture).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                long time_ed= System.currentTimeMillis();
                Log.d("Time to download", time_ed - time_sd+ "");
                Bitmap imageDown = BitmapFactory.decodeFile(user.getImagePath().replace("profile.", "profile_samb."));
                image.setImageBitmap(imageDown);
                long time_d= System.currentTimeMillis();
                Log.d("Time to decode", time_d - time_ed+ "");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_my_book_full);

        title = (TextView) findViewById(R.id.shTitle);
        subtitle = (TextView) findViewById(R.id.shSubtitle);
        author = (TextView) findViewById(R.id.shAuthor);
        publisher = (TextView) findViewById(R.id.shPublisher);
        description = (TextView) findViewById(R.id.shDescription);
        sv = (ScrollView) findViewById(R.id.scrollSh);
        /*description.setScroller(new Scroller(ShowBookFull.this));
        description.setMaxLines(5);
        description.setVerticalScrollBarEnabled(true);*/

        toolbar = (Toolbar) findViewById(R.id.toolbarShowProfile);
        setSupportActionBar(toolbar);

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

        publishDate = (TextView) findViewById(R.id.publishDate);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnEdit = (ImageButton) findViewById(R.id.btnEdit);
        imageMyBook = (ImageView) findViewById(R.id.shMyImage);
        imageBook = (ImageView) findViewById(R.id.shImage);
        available = (TextView) findViewById(R.id.tvState);

        //Fill all the textView of the view with the information about the book.
        if(getIntent().getExtras()!=null && getIntent().getExtras().getParcelable("book_mp")!=null){
            book = getIntent().getExtras().getParcelable("book_mp");
            user = getIntent().getExtras().getParcelable("user_mp");
            btnEdit.setVisibility(View.GONE);
            CardView cvSharedBy = (CardView) findViewById(R.id.card_shared_by);
            cvSharedBy.setVisibility(View.VISIBLE);
            CircleImageView imageSharedBy = (CircleImageView) findViewById(R.id.image_shared_by);
            setBitmapFromFirebase(imageSharedBy);
            TextView tvSharedByName = (TextView) findViewById(R.id.name_shared_by);
            tvSharedByName.setText(user.getName().getValue() + " " + user.getSurname().getValue());
            TextView tvSharedByLocation = (TextView) findViewById(R.id.location_shared_by);
            String currentLocation = new String(user.getCity().getValue());
            if(user.getStreet().getStatus().equals("public")){
                currentLocation+=", " + user.getStreet().getValue();
            }
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

        }else{
            book = getIntent().getParcelableExtra("book");
            user = getIntent().getParcelableExtra("user");
            key = getIntent().getExtras().getString("key");
        }

        title.setText(book.getTitle());
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
        author.setText(book.getAuthor());
        String street = "";
        if (user.getStreet().getStatus().equals("public")) {
            street = ", " + user.getStreet().getValue();
        }
        publisher.setText(book.getPublisher() + ", " + book.getYear());
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
            Picasso.with(ShowBookFull.this).load(book.getUrlImage()).noFade().into(imageBook);
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
    }

    private void zoomImage() {

        //Take the reference of the field
        expandedImage = (ImageView) findViewById(R.id.expanded_image_bookfull);

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
                title.setText(bookModified.getTitle());
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
                author.setText(bookModified.getAuthor());
                publisher.setText(bookModified.getPublisher() + ", " + bookModified.getYear());
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
