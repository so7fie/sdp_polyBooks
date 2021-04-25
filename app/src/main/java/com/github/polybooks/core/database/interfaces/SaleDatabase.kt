package com.github.polybooks.core.database.interfaces

import android.content.Context
import com.github.polybooks.R
import com.github.polybooks.core.BookCondition
import com.github.polybooks.core.Interest
import com.github.polybooks.core.Sale
import com.github.polybooks.core.SaleState
import com.github.polybooks.utils.FieldWithName
import java.io.Serializable
import java.util.concurrent.CompletableFuture

/**
 * Provides the API for accessing sales in a Database.
 * */
interface SaleDatabase {

    fun getCollectionName(): String = "sale"

    /**
     * Create a new query for Sales. It originally matches all sales.
     * */
    fun querySales(): SaleQuery

    /**
     * Get all the sales in the database
     * */
    fun listAllSales(): CompletableFuture<List<Sale>> = querySales().getAll()

    /**
     * A method for getting sales by batches of at most N sales. The batches are indexed by ordered pages.
     * @param numberOfSales The maximum number of sales per page
     * @param page The index of the page
     * @param ordering The ordering for the pages and sales within the pages (see {@link SaleOrdering})
     * */
    fun getNSales(
        numberOfSales: Int,
        page: Int,
        ordering: SaleOrdering
    ): CompletableFuture<List<Sale>> = querySales().withOrdering(ordering).getN(numberOfSales, page)

    /**
     * Add the given sale to the database
     * @param sale The sale to insert
     */
    fun addSale(sale: Sale): Unit

    /**
     * Delete the given sale to the database
     * @param sale The sale to delete
     */
    fun deleteSale(sale: Sale): Unit
}

/**
 * A SaleQuery is a builder for a query to the database that will yield Sales.
 * Most methods return themselves for function chaining.
 * */
interface SaleQuery: Query<Sale> {

    /**
     * Set this query to only include sales that satisfy the given interests.
     * */
    fun onlyIncludeInterests(interests: Set<Interest>): SaleQuery

    /**
     * Set this query to only search for sales with book's title that are like the given one.
     *  If called successively only the last call is taken into account
     * */
    fun searchByTitle(title: String): SaleQuery

    /**
     *  Set this query to only search for sales in the given states.
     *  If called successively only the last call is taken into account
     *  (see {@link SaleState})
     * */
    fun searchByState(state: Set<SaleState>): SaleQuery

    /**
     * Set this query to only search for sales of books in the given condition.
     * If called successively only the last call is taken into account
     * (see {@link BookCondition})
     * */
    fun searchByCondition(condition: Set<BookCondition>): SaleQuery

    /**
     * Set this query to only search for sales above a certain price.
     * */
    fun searchByMinPrice(min: Float): SaleQuery

    /**
     * Set this query to only search for sales below a certain price.
     * */
    fun searchByMaxPrice(max: Float): SaleQuery

    /**
     * Set this query to only search for sales within the given price range.
     * */
    fun searchByPrice(min: Float, max: Float): SaleQuery

    /**
     * Set this query to order books with the given ordering.
     * (see {@link BookOrdering})
     * */
    fun withOrdering(ordering: SaleOrdering): SaleQuery

    /**
     * Set this query to get sales of books associated with the given isbn13.
     * (ignoring other filters)
     * */
    fun searchByISBN(isbn13: String): SaleQuery

    /**
     * Get Settings from the book
     * */
    fun getSettings(): SaleSettings

    /**
     * Reset this query using the given settings
     */
    fun fromSettings(settings: SaleSettings): SaleQuery

}

/**
 * The Settings contains the values for all the possible query parameters (ig. ordering, price).
 * In contrary to a Query object, it is independent to the state of the database and thus it
 * implement Serializable and can be passed as parameter between activities.
 *
 * To define a Query, a SaleSettings can be used along with fromSettings in substitution to
 * calling the other methods (ig. searchByPrice)
 */
data class SaleSettings(
    val ordering: SaleOrdering,
    val isbn: String?,
    val title: String?,
    val interests: Set<Interest>?,
    val states: Set<SaleState>?,
    val conditions: Set<BookCondition>?,
    val minPrice: Float?,
    val maxPrice: Float?
): Serializable

/**
 * Defines an ordering for books. DEFAULT is implementation defined.
 * */
enum class SaleOrdering(private val stringId: Int): FieldWithName {
    // FIXME adapt stringIDs
    DEFAULT(R.string.reset),
    TITLE_INC(R.string.title_inc),
    TITLE_DEC(R.string.title_dec),
    PRICE_INC(R.string.price_inc),
    PRICE_DEC(R.string.price_dec),
    PUBLISH_DATE_INC(R.string.publish_date_inc),
    PUBLISH_DATE_DEC(R.string.publish_date_dec);

    override fun fieldName(c: Context?): String {
        return c?.getString(stringId) ?: name
    }
}
