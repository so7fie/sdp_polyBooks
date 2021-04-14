package com.github.polybooks

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.polybooks.core.Book
import com.github.polybooks.core.BookCondition
import com.github.polybooks.core.Sale
import com.github.polybooks.core.SaleState
import com.github.polybooks.core.database.implementation.SaleDatabase
import com.github.polybooks.core.database.interfaces.BookDatabase
import com.google.firebase.Timestamp
import java.text.DateFormat
import java.util.concurrent.CompletableFuture

/**
 * This activity receives the ISBN, either manually inputted from AddSale or deduced from the scanned barcode,
 * shows the retrieved data, but does not allow modification of it, only confirmation,
 * and offers some additional manual fields such as price, condition, etc.
 */
class FillSaleActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    // TODO I would imagine that in the future, the dbs are global constants, but while writing this class, I'll instantiate one locally
    private val salesDB = SaleDatabase()
    private val bookDB = BookDatabase()

    private lateinit var dateFromBookToSale: Timestamp
    private lateinit var bookConditionSelected: BookCondition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_sale_fancy)



        val DFormat: DateFormat = DateFormat.getDateInstance(DateFormat.LONG)

        // Get the Intent that started this activity and extract the string
        val stringISBN = intent.getStringExtra(ISBN)
        // TODO for testing purpose, the ISBN will temporarily be displayed in the publisher field
        findViewById<TextView>(R.id.filled_publisher)         .apply { text = stringISBN }

        // Check if ISBN in our database: (could check ISBN validity before)

        val book: CompletableFuture<Book> = bookDB.getBook(stringISBN)

        book.thenApply { book ->
            {
                findViewById<TextView>(R.id.filled_authors)         .apply { text = book.authors?.get(0) ?: "" } //TODO update that to either transform the list to string, and just store the string of authors
                findViewById<TextView>(R.id.filled_title)           .apply { text = book.title }
                findViewById<TextView>(R.id.filled_edition)         .apply { text = book.edition }
                findViewById<TextView>(R.id.filled_language)        .apply { text = book.language }
                findViewById<TextView>(R.id.filled_publisher)       .apply { text = book.publisher }
                findViewById<TextView>(R.id.filled_publish_date)    .apply { text = DFormat.format(book.publishDate?.toDate()) }
                findViewById<TextView>(R.id.filled_format)          .apply { text = book.format }

                dateFromBookToSale = book.publishDate!!
            }
        }


        val spinner: Spinner = findViewById(R.id.filled_condition)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.condition_options_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = this

        /* Other spinner implementation
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                Toast.makeText(this@MainActivity,
                    getString(R.string.selected_item) + " " +
                            "" + languages[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }*/

        /*
        // Old version of doing the future, can handle errors, but probably not compatible with Firebase, and I think the interface is handling the
        // case when our DB does not have the book already.

        val book: CompletableFuture<Book> = bookDB.getBook(stringISBN)
        book.handle { (book, err) ->
        {
            if (err == null) {
    // Yes: Retrieve from our database data about the book
                findViewById<TextView>(R.id.filled_authors)         .apply { text = book.authors }
                findViewById<TextView>(R.id.filled_title)           .apply { text = book.title }
                findViewById<TextView>(R.id.filled_edition)         .apply { text = book.edition }
                findViewById<TextView>(R.id.filled_language)        .apply { text = book.language }
                findViewById<TextView>(R.id.filled_publisher)       .apply { text = book.publisher }
                findViewById<TextView>(R.id.filled_publish_date)    .apply { text = book.publishDate }
                findViewById<TextView>(R.id.filled_format)          .apply { text = book.format }
            } else {
    // No: Use Google Books to convert stringISBN to JSON with relevant data, and also add to our database
                // If Google Books fails to find the ISBN, pop-out an error message about invalid ISBN and go back to AddSale page
            }
        }
         */
    }


    // TODO all the picture stuff.

    fun confirmSale(view: View) {
        // TODO determine to which activity we land, but probably not MainActivity but rather a confirmation page
        // store Sale in our database
        val sale: Sale = Sale(
                findViewById<TextView>(R.id.filled_title).text.toString(),
                123, // TODO userID???
                findViewById<EditText>(R.id.filled_price).text.toString().toFloat(),
                bookConditionSelected,
                //findViewById<Spinner>(R.id.filled_condition).getSelectedItem().toString(),
                dateFromBookToSale,
                SaleState.ACTIVE
        )
        salesDB.addSale(sale)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /**
     *
     * Callback method to be invoked when an item in this view has been
     * selected. This callback is invoked only when the newly selected
     * position is different from the previously selected position or if
     * there was no selected item.
     *
     * Implementers can call getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param parent The AdapterView where the selection happened
     * @param view The view within the AdapterView that was clicked
     * @param position The position of the view in the adapter
     * @param id The row id of the item that is selected
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val stringCondition: String = parent?.getItemAtPosition(position).toString()
        when (stringCondition) {
            "New" -> bookConditionSelected = BookCondition.NEW
            "Good" -> bookConditionSelected = BookCondition.GOOD
            "Worn" -> bookConditionSelected = BookCondition.WORN
            else -> {
                Toast.makeText(applicationContext,"Error in selecting the Book Condtion",Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}
