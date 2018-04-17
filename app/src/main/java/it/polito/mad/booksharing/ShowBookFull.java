package it.polito.mad.booksharing;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowBookFull extends AppCompatActivity {

    private TextView title, subtitle, author, owner, publisher, description, publishDate, position;
    private CircleImageView profileImage;
    private ImageView imageBook, imageMyBook;
    private ImageButton btnEdit;
    private RatingBar ratingBar;
    private Book book;
    private User user;
    private String key;
    private ScrollView sv;
    private Animator mCurrentAnimator;
    private ImageView expandedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book_full);

        title = (TextView) findViewById(R.id.shTitle);
        subtitle = (TextView) findViewById(R.id.shSubtitle);
        author = (TextView) findViewById(R.id.shAuthor);
        owner = (TextView) findViewById(R.id.shOwner);
        owner = (TextView) findViewById(R.id.shOwner);
        publisher = (TextView) findViewById(R.id.shPublisher);
        description = (TextView) findViewById(R.id.shDescription);
        sv = (ScrollView) findViewById(R.id.scrollSh);

        /*description.setScroller(new Scroller(ShowBookFull.this));
        description.setMaxLines(5);
        description.setVerticalScrollBarEnabled(true);*/
        description.setMovementMethod(new ScrollingMovementMethod());


        sv.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(description.getLineCount() >= description.getMaxLines()){
                    description.getParent().requestDisallowInterceptTouchEvent(false);
                }


                return false;
            }
        });

        description.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(description.getLineCount() >= description.getMaxLines()) {
                    description.getParent().requestDisallowInterceptTouchEvent(true);
                }
                return false;
            }
        });

        publishDate = (TextView) findViewById(R.id.publishDate);
        position = (TextView) findViewById(R.id.shPosition);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnEdit = (ImageButton) findViewById(R.id.btnEdit);
        imageMyBook = (ImageView) findViewById(R.id.shMyImage);
        imageBook = (ImageView) findViewById(R.id.shImage);
        profileImage = (CircleImageView) findViewById(R.id.profileImage);
        book = getIntent().getParcelableExtra("book");
        user = getIntent().getParcelableExtra("user");
        key = getIntent().getExtras().getString("key");
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
        owner.setText(user.getName().getValue() + " " + user.getSurname().getValue());
        String street = "";
        if (user.getStreet().getStatus().equals("public")) {
            street = ", " + user.getStreet().getValue();
        }
        position.setText(user.getCity().getValue() + street);
        publisher.setText(book.getPublisher() + ", " + book.getYear());
        description.setText(book.getDescription());
        ratingBar.setRating(new Float(book.getRating()));
        publishDate.setText(book.getDate());

        Bitmap image = BitmapFactory.decodeFile(user.getImagePath());
        profileImage.setImageBitmap(image);

        Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).noFade().placeholder(R.drawable.progress_animation)
                .error(R.drawable.ic_error_outline_black_24dp).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageMyBook, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                imageMyBook.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            @Override
            public void onError() {
                imageBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        });
        Picasso.with(ShowBookFull.this).load(book.getUrlImage()).into(imageBook);

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

        imageMyBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomImage();
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
                                        View.Y,startBounds.top))
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
            Log.d("ACTIVITY RESULT", "Called, value of modify: " + data.getExtras().getBoolean("modified", false));
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
                        subtitle.setText(bookModified.getSubtitle());
                    }
                } else {
                    subtitle.setVisibility(View.GONE);
                }
                author.setText(bookModified.getAuthor());
                publisher.setText(bookModified.getPublisher());
                publisher.setText(bookModified.getPublisher() + ", " + bookModified.getYear());
                description.setText(bookModified.getDescription());
                ratingBar.setRating(new Float(bookModified.getRating()));
                Bitmap image = BitmapFactory.decodeFile(user.getImagePath());
                profileImage.setImageBitmap(image);

            }
        }
    }
}
