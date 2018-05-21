package it.polito.mad.booksharing

import android.util.Log
import java.util.*

/**
 * Created by sergiogentile on 20/05/18.
 */
data class Review(var title: String = "",
                  var comment: String = "",
                  var state: String = "",
                  var rating: Float = Float.MAX_VALUE,
                  var name: String = "",
                  var surname: String = "",
                  var keyUser: String = "",
                  var imageUser: String = "",
                  var date: Long = Date().time){

    companion object {
        var land: String = "land"
        var borrow: String = "borrow"
    }


}