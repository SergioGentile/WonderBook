package it.polito.mad.booksharing

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Transaction
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.internal.FirebaseAppHelper.getUid
import android.system.Os.remove
import com.google.firebase.database.MutableData


class AddReview : AppCompatActivity() {

    var rating: RatingBar? = null
    var tvReview: EditText? = null
    var userLogged: User? = null
    var userToReview: User? = null
    var userPicture: ImageView? = null
    var usertv: TextView? = null
    var titleBook: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)
        rating = findViewById(R.id.rating_reviews)
        tvReview = findViewById(R.id.edtReview)
        userPicture = findViewById(R.id.profileImage)
        usertv = findViewById(R.id.usertoReview)

        //Dal bundle

        userLogged = intent.extras.getParcelable("user_logged")
        userToReview = intent.extras.getParcelable("user_to_review")
        titleBook = intent.getStringExtra("titleBook");



        usertv?.text = userToReview?.name?.value!! + " " + userToReview?.surname?.value!!

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
            if (rating?.rating!!.compareTo(0) != 0) {
                //Upload all on firebase
                var status: String
                status = intent.getStringExtra("status")

                val review = Review(tvReview?.text.toString(), status, rating?.rating!!, userLogged?.name?.value!!, userLogged?.surname?.value!!, userLogged?.key!!, userLogged?.user_image_url!!, titleBook!!)

                if (!tvReview?.text!!.isEmpty()) {
                    FirebaseDatabase.getInstance().getReference("users").child(userToReview?.key).child("reviews").push().setValue(review)
                }
                var dbRef: DatabaseReference? = FirebaseDatabase.getInstance().getReference("users").child(userToReview?.key);

                dbRef?.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData?): Transaction.Result {
                        val user = mutableData?.getValue<User>(User::class.java) ?: return Transaction.success(mutableData)!!

                        var numRev: Int? = user.numRev
                        var numStars: Float? = rating?.rating
                        var lastScore: Float? = user.numStars * user.numRev
                        numRev = numRev!! + 1
                        numStars = (lastScore!! + numStars!!) / (numRev)
                        user.numRev = numRev
                        user.numStars = numStars

                        // Set value and report transaction success
                        mutableData.child("numStars").value = numStars
                        mutableData.child("numRev").value = numRev
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(databaseError: DatabaseError?, b: Boolean,
                                            dataSnapshot: DataSnapshot?) {

                    }
                })
                finish()
            } else {
                Toast.makeText(applicationContext, getString(R.string.please_insert_score), Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onBackPressed() {

        if (rating?.rating!!.compareTo(0) == 0) {
            Toast.makeText(applicationContext, getString(R.string.please_insert_score), Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
            finish()
        }
    }

}
