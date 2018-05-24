package it.polito.mad.booksharing

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.Toolbar
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebase.ui.database.FirebaseListAdapter
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.util.zip.Inflater
import android.support.v4.content.ContextCompat
import android.view.WindowManager


class ShowReviews : AppCompatActivity() {

    var userLogged: User? = null
    var userToReview: User? = null
    var listOfReviews: ListView? = null
    val ANY = 0;
    val BORROW = 2
    val LAND = 1
    val reviews: MutableList<Review> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_reviews)

        userLogged = intent.extras.getParcelable("user_logged")
        userToReview = intent.extras.getParcelable("user_to_review")
        listOfReviews = findViewById(R.id.list_of_reviews)

        var toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish();
        })


        var tabLayout: TabLayout = findViewById(R.id.tabsReviews)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                var toolbar: Toolbar = findViewById(R.id.toolbar)

                val window = getWindow()
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

                if (tab?.position == ANY) {
                    toolbar.setBackgroundColor(getColor(R.color.colorPrimary))
                    window.setStatusBarColor(getColor(R.color.colorPrimaryDark))
                    tabLayout.setBackgroundColor(getColor(R.color.colorPrimary))
                } else if (tab?.position == LAND) {
                    toolbar.setBackgroundColor(getColor(R.color.land))
                    window.setStatusBarColor(getColor(R.color.landDark))
                    tabLayout.setBackgroundColor(getColor(R.color.land))
                } else if (tab?.position == BORROW) {
                    toolbar.setBackgroundColor(getColor(R.color.borrow))
                    window.setStatusBarColor(getColor(R.color.borrowDark))
                    tabLayout.setBackgroundColor(getColor(R.color.borrow))
                }
                showReviews(tab?.position!!)
            }
        })



        showReviews(ANY)


    }


    private fun showReviews(type: Int) {
        var databaseReference: DatabaseReference?
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userToReview?.key).child("reviews")


        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshots: DataSnapshot) {
                if (dataSnapshots.exists()) {
                    reviews.clear()
                    listOfReviews?.adapter = null
                    //Download the review
                    for (reviewDataSnapshot: DataSnapshot? in dataSnapshots.children) {
                        var review = reviewDataSnapshot?.getValue(Review::class.java)!!
                        review.key = reviewDataSnapshot.key
                        if (ANY == type) {
                            reviews.add(review)
                        } else if (LAND == type) {
                            if (review.state.equals("land")) {
                                reviews.add(review)
                            }
                        } else if (BORROW == type) {
                            if (review.state.equals("borrow")) {
                                reviews.add(review)
                            }
                        }
                    }
                    listOfReviews?.adapter = AdapterReviews(reviews, this@ShowReviews)

                    //Check if something change and update it if necessary. Do it in background
                    AsyncTask.execute(Runnable {
                        for (review in reviews) {
                            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(review.keyUser)
                            databaseReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        val user = dataSnapshot?.getValue(User::class.java)!!
                                        if (!review.name.equals(user.name.value)) {
                                            //Update the name
                                            FirebaseDatabase.getInstance().getReference("users").child(userLogged?.key).child("reviews").child(review.key).child("name").setValue(user.name.value)
                                            review.name = user.name.value
                                        }
                                        if (!review.surname.equals(user.surname.value)) {
                                            //Update the name
                                            FirebaseDatabase.getInstance().getReference("users").child(userLogged?.key).child("reviews").child(review.key).child("surname").setValue(user.surname.value)
                                            review.surname = user.name.value
                                        }
                                        if (!review.imageUser.equals(user.user_image_url)) {
                                            //Update the name
                                            FirebaseDatabase.getInstance().getReference("users").child(userLogged?.key).child("reviews").child(review.key).child("imageUser").setValue(user.user_image_url)
                                            review.imageUser = user.user_image_url
                                        }

                                    }

                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })
                        }
                    })

                } else {
                    //Print something to say: no reviews exists for that user

                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}


open class AdapterReviews() : BaseAdapter() {

    private var reviews: MutableList<Review>? = null
    private var inflater: LayoutInflater? = null
    var context: Context? = null

    constructor(reviews: MutableList<Review>, context: Context) : this() {
        this.reviews = reviews
        this.context = context
        inflater = LayoutInflater.from(this.context)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val v: View?
        if (convertView == null) {
            v = this.inflater?.inflate(R.layout.adapter_review, parent, false)
        } else {
            v = convertView
        }


        var profileImage: CircleImageView = v?.findViewById(R.id.profileImage)!!
        var rating: RatingBar = v?.findViewById(R.id.rating_reviews)!!
        var name: TextView = v?.findViewById(R.id.name)!!
        var reviewText: TextView = v?.findViewById(R.id.reviewText)!!
        var reviewDate: TextView = v?.findViewById(R.id.date_reviews)
        var reviewType: TextView = v?.findViewById(R.id.type)
        var viewLine: View = v?.findViewById(R.id.viewLine)
        var review = reviews?.get(position)


        Picasso.with(context).load(review?.imageUser).into(profileImage)

        rating.rating = review?.rating!!
        reviewText.text = review?.comment
        reviewDate.text = getDate(review?.date)
        if (review.state.equals(Review.borrow)) {
            reviewType.text = "${context?.getString(R.string.borrow_the_book)} \"${review?.bookName}\""
            name.text = " ${review?.name} ${review?.surname}"
            viewLine.setBackgroundColor(context?.getColor(R.color.borrow)!!)
        } else {
            reviewType.text = "${context?.getString(R.string.lend_the_book)} \"${review?.bookName}\""
            name.text = "${review?.name} ${review?.surname}"
            viewLine.setBackgroundColor(context?.getColor(R.color.land)!!)

        }

        return v!!

    }

    override fun getItem(position: Int): Any {
        return reviews?.get(position)!!
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return reviews?.size!!
    }

    private fun getDate(timestamp: Long): String {
        return DateFormat.format("dd/MM/yyyy", timestamp).toString()
    }

}


