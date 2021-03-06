package com.github.polybooks.core

import android.content.Context
import com.github.polybooks.R
import com.github.polybooks.utils.FieldWithName
import java.util.Date
import java.io.Serializable

typealias Image = android.media.Image

/**
 * The Sale class contains all the information about a book on sale.
 * @property book The isbn13 of the book that is being sold
 * @property seller The ID of the seller
 * @property price The price in CHF at which the book is being sold
 * @property condition The condition of the book (see {@link BookCondition})
 * @property date The date on which the Sale has been issued/published
 * @property state The state of the Sale (see {@link SaleState})
 * */

data class Sale(
    val book : Book,
    val seller : User,
    val price : Float,
    val condition : BookCondition,
    val date : Date,
    val state : SaleState,
    val image : Image?
) : Serializable


/**
 * The condition of a book (as in "in great condition").
 * */
enum class BookCondition: FieldWithName {
    NEW,
    GOOD,
    WORN;

    override fun fieldName(c: Context?): String {
        return c?.resources?.getStringArray(R.array.sale_book_conditions_array)?.get(ordinal)
            ?: name
    }
}

/**
 * The State of a Sale.
 * Active means that the offer to buy the book is on the table.
 * Retracted means that the book is no longer on sale because the seller retracted the offer.
 * Concluded means that the book has been sold and is therefore no longer on sale.
 * */
enum class SaleState: FieldWithName {
    ACTIVE,
    RETRACTED,
    CONCLUDED;

    override fun fieldName(c: Context?): String {
        return c?.resources?.getStringArray(R.array.sale_states_array)?.get(ordinal)
            ?: name
    }
}

/**
 * Allows access to the name of a field
 */
enum class SaleFields(val fieldName: String) {
    BOOK_ISBN("book_isbn"),
    CONDITION("condition"),
    PRICE("price"),
    PUBLICATION_DATE("date"),
    SELLER("seller"),
    STATE("state"),
    IMAGE("image")
}