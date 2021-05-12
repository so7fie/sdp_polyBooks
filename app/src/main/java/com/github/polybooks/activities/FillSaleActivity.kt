package com.github.polybooks.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.polybooks.R
import com.github.polybooks.utils.setupNavbar
import com.github.polybooks.core.*
import com.github.polybooks.database.implementation.OLBookDatabase
import com.github.polybooks.database.implementation.SaleDatabase
import com.github.polybooks.utils.StringsManip.isbnHasCorrectFormat
import com.github.polybooks.utils.StringsManip.listAuthorsToString
import com.github.polybooks.utils.UIManip.disableButton
import com.github.polybooks.utils.UIManip.enableButton
import com.github.polybooks.utils.url2json
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
import java.text.DateFormat
import java.util.concurrent.CompletableFuture

/**
 * This activity receives the ISBN, either manually inputted from AddSale or deduced from the scanned barcode,
 * shows the retrieved data, but does not allow modification of it, only confirmation,
 * and offers some additional manual fields such as price, condition, etc.
 */
class FillSaleActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    // TODO I would imagine that in the future, the dbs are global constants, but while writing this class, I'll instantiate one locally
    private val firestore = FirebaseFirestore.getInstance()
    private val bookDB = OLBookDatabase { string -> url2json(string) }
    private val salesDB = SaleDatabase(firestore, bookDB)

    private lateinit var bookFuture: CompletableFuture<Book?>
    private var bookConditionSelected: BookCondition? = null
    private val dateFormat: DateFormat = DateFormat.getDateInstance(DateFormat.LONG)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_sale_fancy)

        // Get the Intent that started this activity and extract the string
        val stringISBN = intent.getStringExtra(ISBN)

        // Check if ISBN in our database: (could check ISBN validity before)

        // Retrieve book data and display it if possible, else redirect with error toast
        if(!stringISBN.isNullOrEmpty() && isbnHasCorrectFormat(stringISBN)) {
            try {
                bookFuture = bookDB.getBook(stringISBN)
                val book = bookFuture.get()
                if (book != null) {
                    fillBookData(book)
                } else {
                    redirectToAddSaleWithToast("Book matching the ISBN could not be found")
                }
            } catch (e: Exception) {
                redirectToAddSaleWithToast("An error occurred, please try again")
            }
        } else {
            redirectToAddSaleWithToast("Please provide an ISBN")
        }


        // Drop-down menu for condition
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

        // Listener on fill-in book price to trigger confirm button
        findViewById<EditText>(R.id.filled_price).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                handleConfirmButton()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        // Disable confirm button until filled
        disableButton(findViewById(R.id.confirm_sale_button), applicationContext)
        setupNavbar(findViewById(R.id.bottom_navigation), this)
    }

    /**
     * Fill the UI with the data about a book
     */
    private fun fillBookData(book: Book) {
        findViewById<TextView>(R.id.filled_authors).apply {
            text = listAuthorsToString(book.authors)
        }
        findViewById<TextView>(R.id.filled_title).apply { text = book.title }
        findViewById<TextView>(R.id.filled_edition).apply { text = book.edition ?: "" }
        // TODO language is ideally not converted to a string but to a flag
        // findViewById<TextView>(R.id.filled_language).apply { text = book.language ?: "" }
        findViewById<TextView>(R.id.filled_publisher).apply { text = book.publisher ?: "" }
        findViewById<TextView>(R.id.filled_publish_date).apply {
            text = dateFormat.format(book.publishDate!!.toDate()) ?: ""
        }
        // TODO whole lines could be removed from UI when argument is null instead of placeholding with default value
        findViewById<TextView>(R.id.filled_format).apply { text = book.format ?: "" }
    }

    /**
     * Called when an issue happen to convert the issue to a nicer UX flow than crashing
     * Showing a message about the error and redirecting to a previous activity
     */
    private fun redirectToAddSaleWithToast(message: String) {
        Log.w("BookFuture", message)
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
        // TODO only enable redirect on release build variant because it causes tests to fail (don't redirect on test/debug builds)
        /*
        val intent = Intent(this, AddSaleActivity::class.java)
        startActivity(intent)
         */
    }


    // TODO all the picture stuff.


    /**
     * Create a sale in the database with the relevant data from the activity
     * (i.e. book, condition, user, price, date)
     */
    fun confirmSale(view: View) {
        salesDB.addSale(
            bookISBN = bookFuture.get()!!.isbn,
            seller = LoggedUser(123456, "Alice"), //TODO handle real User
            price = findViewById<EditText>(R.id.filled_price).text.toString().toFloat(),
            condition = bookConditionSelected!!,
            state = SaleState.ACTIVE,
            image = null
        )
        //TODO handle success or failure of the addSale

        // TODO determine to which activity we land, but probably not MainActivity but rather a confirmation page
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun handleConfirmButton() {
        if (bookConditionSelected != null && findViewById<EditText>(R.id.filled_price).text.toString().isNotEmpty()) {
            enableButton(findViewById(R.id.confirm_sale_button), applicationContext)
        } else {
            disableButton(findViewById(R.id.confirm_sale_button), applicationContext)
        }
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
        when (parent?.getItemAtPosition(position).toString()) {
            "Select" -> bookConditionSelected = null
            "New" -> bookConditionSelected = BookCondition.NEW
            "Good" -> bookConditionSelected = BookCondition.GOOD
            "Worn" -> bookConditionSelected = BookCondition.WORN
            else -> {
                Toast.makeText(
                    applicationContext,
                    "Error in selecting the Book Condition",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        handleConfirmButton()
    }

    /**
     * Callback method to be invoked when the selection disappears from this
     * view. The selection can disappear for instance when touch is activated
     * or when the adapter becomes empty.
     *
     * @param parent The AdapterView that now contains no selected item.
     */
    override fun onNothingSelected(parent: AdapterView<*>?) {
        disableButton(findViewById(R.id.confirm_sale_button), applicationContext)
    }
}