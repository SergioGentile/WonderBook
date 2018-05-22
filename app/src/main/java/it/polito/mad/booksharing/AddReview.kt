package it.polito.mad.booksharing

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RatingBar
import com.google.firebase.database.FirebaseDatabase

class AddReview : AppCompatActivity() {

    var rating: RatingBar? = null
    var tvTitle: EditText? = null
    var tvReview: EditText? = null
    var radioBoxLand: RadioButton? = null
    var radioBoxBorrow: RadioButton? = null
    var userLogged: User? = null
    var userToReview: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_review)
        rating = findViewById(R.id.rating_reviews)
        tvTitle = findViewById(R.id.edtTitle)
        tvReview = findViewById(R.id.edtReview)
        radioBoxLand = findViewById(R.id.land)
        radioBoxBorrow = findViewById(R.id.borrow)

        userLogged = intent.extras.getParcelable("user_logged")
        userToReview = intent.extras.getParcelable("user_to_review")

        var button: ImageButton = findViewById(R.id.btnDone)
        button.setOnClickListener(View.OnClickListener {
            //Upload all on firebase
            var status: String
            status = intent.getStringExtra("status")

            val review  = Review(tvTitle?.text.toString(), tvReview?.text.toString(), status, rating?.rating!!, userLogged?.name?.value!!, userLogged?.surname?.value!!, userLogged?.key!! ,userLogged?.user_image_url!! )
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
