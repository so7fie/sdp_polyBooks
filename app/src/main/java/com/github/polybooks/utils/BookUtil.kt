package com.github.polybooks.utils

import com.github.polybooks.core.Book
import com.github.polybooks.core.Course
import com.github.polybooks.core.Field
import com.github.polybooks.core.Semester
import com.github.polybooks.utils.StringsManip.isbnHasCorrectFormat


fun anonymousBook(title: String): Book =
        Book("", null, title, null, null, null, null, null)

//takes a string and try to interpret it as an isbn
fun regulariseISBN(userISBN : String) : String? {
        val regularised = userISBN.replace("[- ]".toRegex(), "")
        return if (isbnHasCorrectFormat(userISBN)) regularised
        else null
}