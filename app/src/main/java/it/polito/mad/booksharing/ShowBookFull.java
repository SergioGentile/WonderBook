package it.polito.mad.booksharing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
