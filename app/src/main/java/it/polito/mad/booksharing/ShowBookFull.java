package it.polito.mad.booksharing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowBookFull extends AppCompatActivity {

    private TextView title, subtitle, author, owner, publisher, description, publishDate, position;
    private CircleImageView profileImage;
    private ImageView imageBook, imageMyBook;
    private CardView sharedBy;
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
        sharedBy = (CardView)findViewById(R.id.sharedBy);
        sharedBy.setVisibility(View.GONE);
        /*description.setScroller(new Scroller(ShowBookFull.this));
        description.setMaxLines(5);
        description.setVerticalScrollBarEnabled(true);*/

        //This part is useful when the description field over the max number of lines.
        //If the user scroll the description field, the scrollerView is blocked, and with the same principle
        //when the user scroll the scrollView the description field is blocked.
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

        //Fill all the textView of the view with the information about the book.
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
                .error(R.drawable.ic_error_outline_black_24dp).into(imageMyBook, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                imageMyBook.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            @Override
            public void onError() {
                imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        });

        if(book.getUrlImage()==null || book.getUrlImage().isEmpty()){
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
        else{
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
                publisher.setText(bookModified.getPublisher());
                publisher.setText(bookModified.getPublisher() + ", " + bookModified.getYear());
                description.setText(bookModified.getDescription());
                ratingBar.setRating(new Float(bookModified.getRating()));
                Bitmap image = BitmapFactory.decodeFile(user.getImagePath());
                profileImage.setImageBitmap(image);

                book.setUrlImage(bookModified.getUrlImage());
                book.setUrlMyImage(bookModified.getUrlMyImage());

                //Set the images of the book
                imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).noFade().placeholder(R.drawable.progress_animation)
                        .error(R.drawable.ic_error_outline_black_24dp).into(imageMyBook, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        imageMyBook.setScaleType(ImageView.ScaleType.FIT_XY);
                    }

                    @Override
                    public void onError() {
                        imageMyBook.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                });

                if(book.getUrlImage().isEmpty()){
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
        }
        else if(RESULT_CANCELED == resultCode){
            Toast.makeText(ShowBookFull.this, getString(R.string.error_reload_new_book), Toast.LENGTH_SHORT);
        }
    }
}
