package it.polito.mad.booksharing;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class ShowBookFull extends AppCompatActivity {

    TextView title, author, owner, publisher, description, publishDate;
    ImageView imageBook, imageMyBook;
    ImageButton btnEdit;
    RatingBar ratingBar;
    Book book;
    String key;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book_full);

        title = (TextView)findViewById(R.id.shTitle);
        author = (TextView)findViewById(R.id.shAuthor);
        owner = (TextView)findViewById(R.id.shOwner);
        publisher = (TextView)findViewById(R.id.shPublisher);
        description = (TextView)findViewById(R.id.shDescription);
        publishDate = (TextView)findViewById(R.id.publishDate);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        btnEdit = (ImageButton) findViewById(R.id.btnEdit);
        imageMyBook = (ImageView)findViewById(R.id.shMyImage);
        imageBook = (ImageView)findViewById(R.id.shImage);

        book = getIntent().getParcelableExtra("book");
        key = getIntent().getExtras().getString("key");
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        owner.setText(book.getOwner());
        publisher.setText(book.getPublisher() + ", " + book.getYear());
        description.setText(book.getDescription());
        ratingBar.setRating(new Float(book.getRating()));

        Picasso.with(ShowBookFull.this).load(book.getUrlMyImage()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).into(imageMyBook);
        Picasso.with(ShowBookFull.this).load(book.getUrlImage()).into(imageBook);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowBookFull.this, AddBook.class).putExtra("edit", true);
                intent.putExtra("book", book);
                intent.putExtra("key", key);
                startActivity(intent);
                finish();
            }
        });
    }
}
