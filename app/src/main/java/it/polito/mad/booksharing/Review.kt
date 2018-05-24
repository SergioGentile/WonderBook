package it.polito.mad.booksharing

import android.util.Log
import java.util.*

/**
 * Created by sergiogentile on 20/05/18.
 */
data class Review(var comment: String = "",
                  var state: String = "",
                  var rating: Float = Float.MAX_VALUE,
                  var name: String = "",
                  var surname: String = "",
                  var keyUser: String = "",
                  var imageUser: String = "",
                  var bookName: String ="",
                  var date: Long = Date().time,
                  var key: String = ""){

    companion object {
        var land: String = "land"
        var borrow: String = "borrow"
    }


}