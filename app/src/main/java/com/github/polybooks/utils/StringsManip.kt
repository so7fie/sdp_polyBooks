package com.github.polybooks.utils

import java.util.*

/**
 * A utility object class for all manner of strings manipulation
 */
object StringsManip {
    private const val ISBN13_FORMAT = """[0-9]{13}"""
    private const val ISBN10_FORMAT = """[0-9]{9}[0-9X]"""
    private const val ISBN_FORMAT = """($ISBN10_FORMAT)|($ISBN13_FORMAT)"""

    /**
     * listAuthorsToString takes a nullable list of Strings as parameters
     * and return a single string formatted under the concatenation of all strings of the list
     * separated by ", " except for the last one separated by ", and " (Oxford comma)
     * Empty strings are returned if the list is null or empty
     */
    fun listAuthorsToString(list: List<String>?): String {
        fun authorsFromListRec(acc: StringJoiner, list: List<String>): String {
            return if (list.size == 1) acc.add("and ${list[0]}").toString()
            else authorsFromListRec(acc.add(list[0]), list.drop(1))
        }

        return when {
            (list == null || list.isEmpty()) -> ""
            (list.size == 1) -> list[0]
            else -> authorsFromListRec(StringJoiner(", "), list)
        }
    }

    fun isbnHasCorrectFormat(isbn: String): Boolean {
        return isbn.matches(Regex(ISBN_FORMAT))
    }
}
