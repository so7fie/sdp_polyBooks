package com.github.polybooks.database


import com.github.polybooks.utils.url2json
import junit.framework.AssertionFailedError
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class OLBookDBTests {

    @Test
    fun canGetBookByISBN() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("9782376863069")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
        assertEquals("Liavek", book.title)
        assertEquals("9782376863069", book.isbn)
        assertEquals("ACTUSF", book.publisher)
        assertNotNull(book.authors)
        assertEquals("paperback", book.format)
        assertNotNull(book.publishDate)
        val publishDate = Date(2020 -1900,6,3)
        assertEquals(publishDate, book.publishDate!!.toDate())
    }

    @Test
    fun canGetBookByISBN2() {
        //check for a book that does not have a precise publish date
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("9781603090476")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
    }

    @Test
    fun canGetLanguage() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("9781603090476")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
        assertEquals("English", book.language)
    }

    @Test
    fun canGetEdition() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("0030137314")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
        assertEquals("2d ed.", book.edition)
    }

    @Test
    fun canGetBookWithNoFieldFullTitle() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("9780156881807")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
    }

    @Test
    fun weirdISBNFormatStillWork() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("  978-2376863069 ")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
        assertEquals("Liavek", book.title)
        assertEquals("9782376863069", book.isbn)
        assertEquals("ACTUSF", book.publisher)
        assertNotNull(book.authors)
        assertEquals("paperback", book.format)
        assertNotNull(book.publishDate)
        val publishDate = Date(2020 -1900,6,3)
        assertEquals(publishDate, book.publishDate!!.toDate())
    }

    @Test
    fun isbn10alsoWorks() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("2376863066")
        val book = future.get() ?: throw AssertionFailedError("Book was not found")
        assertEquals("Liavek", book.title)
        assertEquals("9782376863069", book.isbn)
        assertEquals("ACTUSF", book.publisher)
        assertNotNull(book.authors)
        assertEquals("paperback", book.format)
        assertNotNull(book.publishDate)
        val publishDate = Date(2020 -1900,6,3)
        assertEquals(publishDate, book.publishDate!!.toDate())
    }

    @Test
    fun wrongISBNyieldsEmptyList() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.queryBooks().searchByISBN(setOf("1234567890666")).getAll()
        val books = future.get()

        assertEquals(0, books.size)
    }

    @Test
    fun authorsAreCorrect() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.getBook("9782376863069")
        val book = future.get()!!
        assertEquals(2, book.authors!!.size)
        assertEquals("Steven Brust", book.authors!![0])
        assertEquals("Megan Lindholm", book.authors!![1])
    }

    @Test
    fun getMultipleBooksWorks() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.queryBooks().searchByISBN(setOf("9782376863069", "9781985086593")).getAll()
        val books = future.get()
        assertEquals(2, books.size)
    }

    @Test
    fun getMultipleBooksWorks2() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val future = olDB.queryBooks().searchByISBN(setOf("9782376863069", "9781985086593", "1234567890666")).getAll()
        val books = future.get()
        assertEquals(2, books.size)
    }


    @Test
    fun rejectsWrongISBN1() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        try {
            olDB.queryBooks().searchByISBN(setOf("this is no ISBN"))
        } catch (e : IllegalArgumentException) {
            //success !
            return
        } catch (e : Exception) {
            throw AssertionFailedError("Expected IllegalArgumentException, got ${e.javaClass}")
        }
        throw AssertionFailedError("Expected IllegalArgumentException, got nothing")
    }

    @Test
    fun rejectsWrongISBN2() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        try {
            olDB.queryBooks().searchByISBN(setOf("1234"))
        } catch (e : IllegalArgumentException) {
            //success !
            return
        } catch (e : Exception) {
            throw AssertionFailedError("Expected IllegalArgumentException, got ${e.javaClass}")
        }
        throw AssertionFailedError("Expected IllegalArgumentException, got nothing")
    }


    @Test
    fun onlyIncludeXdontFail() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        olDB.queryBooks().onlyIncludeInterests(Collections.emptyList())
        olDB.queryBooks().searchByTitle("title")
    }

    @Test
    fun getSettingsAndFromSettingsMatch() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val settings = BookSettings(
                BookOrdering.DEFAULT,
                listOf("9782376863069", "1234567890666"),
                "A Book",
                null // TODO update when interests are ready
        )
        assertEquals(
                settings,
                olDB.queryBooks().fromSettings(settings).getSettings()
        )
    }

    @Test
    fun settingsModifiesStateOfQuery() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val settings = BookSettings(
                BookOrdering.DEFAULT,
                listOf("9782376863069", "1234567890666"),null, null
        )
        assertNotEquals(
                olDB.queryBooks().fromSettings(settings).getAll().get().size,
                olDB.queryBooks().searchByISBN(setOf("9782376863069", "9781985086593")).getAll().get().size
        )
    }

    @Test
    fun settingsQueriesTheSameWayAsSearchByISBN() {
        val olDB = OLBookDatabase { url -> url2json(url) }
        val settings = BookSettings(
                BookOrdering.DEFAULT,
                listOf("9782376863069", "9781985086593"),null, null
        )

        assertEquals(
                olDB.queryBooks().fromSettings(settings).getAll().get().size,
                olDB.queryBooks().searchByISBN(setOf("9782376863069", "9781985086593")).getAll().get().size
        )
    }

}
