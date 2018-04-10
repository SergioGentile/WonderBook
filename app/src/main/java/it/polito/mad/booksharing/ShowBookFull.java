package it.polito.mad.booksharing;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ShowBookFull extends AppCompatActivity {

    TextView title, author, owner, year, description, publishDate;
    ImageView imageBook, imageMyBook;
    RatingBar ratingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book_full);

        title = (TextView)findViewById(R.id.shTitle);
        author = (TextView)findViewById(R.id.shAuthor);
        owner = (TextView)findViewById(R.id.shOwner);
        year = (TextView)findViewById(R.id.shYear);
        description = (TextView)findViewById(R.id.shDescription);
        publishDate = (TextView)findViewById(R.id.publishDate);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);


        imageMyBook = (ImageView)findViewById(R.id.shMyImage);
        imageBook = (ImageView)findViewById(R.id.shImage);

        Book book = getIntent().getParcelableExtra("book");
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        owner.setText(book.getOwner());
        year.setText(book.getYear());
        description.setText(book.getDescription());
        ratingBar.setRating(new Float(book.getRating()));
        publishDate.setText(book.getDate());

        Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).into(imageMyBook);
        Picasso.with(ShowBookFull.this).load(book.getUrlImage()).into(imageBook);
    }
}
