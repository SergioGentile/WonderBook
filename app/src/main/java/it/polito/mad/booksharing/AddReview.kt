package it.polito.mad.booksharing

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream

class AddReview : AppCompatActivity() {

    var rating: RatingBar? = null
    var tvReview: EditText? = null
    var userLogged: User? = null
    var userToReview: User? = null
    var userPicture: ImageView? = null
    var usertv: TextView?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)
        rating = findViewById(R.id.rating_reviews)
        tvReview = findViewById(R.id.edtReview)
        userPicture = findViewById(R.id.profileImage)
        usertv = findViewById(R.id.usertoReview)

        //Dal bundle
        val tvTitle = "HarryPotter"
        userLogged = intent.extras.getParcelable("user_logged")
        userToReview = intent.extras.getParcelable("user_to_review")

        usertv?.text =  userToReview?.name?.value!!+" "+userToReview?.surname?.value!!

        Picasso.with(this@AddReview)
                .load(userToReview?.user_image_url!!).noFade()
                .error(R.drawable.ic_error_outline_black_24dp)
                .into(userPicture, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                    }

                    override fun onError() {
                    }
                })

        var fab: Button = findViewById(R.id.fab)
        fab.setOnClickListener(View.OnClickListener {
            //Upload all on firebase
            var status: String
            status = intent.getStringExtra("status")

            val review  = Review(tvTitle, tvReview?.text.toString(), status, rating?.rating!!, userLogged?.name?.value!!, userLogged?.surname?.value!!, userLogged?.key!! ,userLogged?.user_image_url!! )
            FirebaseDatabase.getInstance().getReference("users").child(userToReview?.key).child("reviews").push().setValue(review)
            var numRev: Int = userToReview?.numRev!!
            var numStars: Float = rating?.rating!!
            var lastScore: Float = userToReview?.numStars!! * userToReview?.numRev!!
            numRev++
            numStars = (lastScore + numStars)/(numRev)
            FirebaseDatabase.getInstance().getReference("users").child(userToReview?.key).child("numStars").setValue(numStars)
            FirebaseDatabase.getInstance().getReference("users").child(userToReview?.key).child("numRev").setValue(numRev)
            finish()
        })
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
