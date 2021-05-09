package com.github.polybooks

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.polybooks.core.Book
import com.github.polybooks.core.Sale
import com.github.polybooks.core.SaleState
import com.github.polybooks.core.database.BooksAdapter
import com.github.polybooks.core.database.SalesAdapter
import com.github.polybooks.core.database.implementation.FBBookDatabase
import com.github.polybooks.core.database.implementation.OLBookDatabase
import com.github.polybooks.core.database.implementation.SaleDatabase
import com.github.polybooks.core.database.interfaces.BookSettings
import com.github.polybooks.core.database.interfaces.Query
import com.github.polybooks.core.database.interfaces.SaleQuery
import com.github.polybooks.core.database.interfaces.SaleSettings
import com.github.polybooks.utils.setupNavbar
import com.github.polybooks.utils.url2json
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity to list all active sales
 * Pass boolean extra with id R.string.list_is_sale as false for books
 */

//TODO change name
class ListSalesActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_SALE_QUERY_SETTINGS: String = "saleQuerySettings"
        const val EXTRA_BOOKS_QUERY_SETTINGS: String = "bookQuerySettings"
        private const val TAG = "ListActivity"
    }

    private lateinit var mRecycler: RecyclerView
    private lateinit var mAdapter: RecyclerView.Adapter<*>
    private val mLayout: RecyclerView.LayoutManager = LinearLayoutManager(this)
    private var isSales: Boolean = true

    private val firestore = FirebaseFirestore.getInstance()
    private val olBookDB = OLBookDatabase { string -> url2json(string) }
    private val bookDB = FBBookDatabase(firestore, olBookDB)
    private val salesDB = SaleDatabase(firestore, bookDB)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_database)

        isSales = intent.extras?.getBoolean(getString(R.string.list_is_sale), true) ?: true

        findViewById<TextView>(R.id.sale_or_book).text = if (isSales) getString(R.string.sale) else getString(R.string.books)
        findViewById<Button>(R.id.filter_button).setOnClickListener { onFilterButtonClick() }
        val query: Query<*> =
            if (isSales) {
                intent.getSerializableExtra(EXTRA_SALE_QUERY_SETTINGS)
                    ?.let {
                        salesDB.querySales()
                            .fromSettings(intent.getSerializableExtra(EXTRA_SALE_QUERY_SETTINGS) as SaleSettings)
                    }
                    ?: salesDB.querySales().searchByState(setOf(SaleState.ACTIVE))
            } else {
                intent.getSerializableExtra(EXTRA_BOOKS_QUERY_SETTINGS)
                    ?.let {
                        bookDB.queryBooks()
                            .fromSettings(intent.getSerializableExtra(EXTRA_BOOKS_QUERY_SETTINGS) as BookSettings)
                    }
                    ?: bookDB.queryBooks()
            }

        mRecycler = findViewById(R.id.recyclerView)
        mRecycler.setHasFixedSize(true)

        // Links the database api to the recyclerView
        mAdapter = if (isSales) SalesAdapter(emptyList()) else BooksAdapter(emptyList())
        mRecycler.layoutManager = mLayout
        mRecycler.adapter = mAdapter

        query.getAll().thenAccept { list -> this.updateAdapter(list) }

        setupNavbar(findViewById(R.id.bottom_navigation), this)
    }

    private fun onFilterButtonClick() {
        if (isSales)
            startActivity(Intent(this, FilteringSalesActivity::class.java))
        else
            startActivity(Intent(this, FilteringBooksActivity::class.java))
    }

    private fun updateAdapter(list: List<*>) {
        runOnUiThread {
            mAdapter =
                if (isSales) SalesAdapter(list as List<Sale>) else BooksAdapter(list as List<Book>)
            mRecycler.adapter = mAdapter
        }
    }
}
