package com.github.polybooks.adapter

import android.content.Context
import com.github.polybooks.core.Course
import com.github.polybooks.core.Field
import com.github.polybooks.core.Interest
import com.github.polybooks.core.Semester
import com.github.polybooks.database.DummyInterestDatabase

/**
 * An adapter when filtering by Interest, which is a dynamic list of filtering values
 *
 * @param itemViewId   view id in the xml layout of a value item
 * @param enumInstance any instance of class T (needed to be able to use methods of T)
 * @see   ParameterAdapter
 */
class InterestsParameterAdapter<T: Interest>(
    itemViewId: Int,
    private val interestType: Interest
):
    ParameterAdapter<T>(
        itemViewId,
        listOf()
    ) {
    init {
        val setV = { values: Any -> setValues(values as List<T>) }
        when (interestType) {
            Interest.COURSE -> DummyInterestDatabase.getInstance().listAllCourses().thenAccept(setV)
            Interest.FIELD -> DummyInterestDatabase.getInstance().listAllFields().thenAccept(setV)
            Interest.SEMESTER -> DummyInterestDatabase.getInstance().listAllSemesters().thenAccept(setV)
        }
    }

    enum class Interest {
        COURSE, FIELD, SEMESTER
    }

    override fun getItemViewType(): Int {
        return VIEW_TYPE_CHECKBOX
    }

    override fun getValueName(value: T, context: Context?): String {
        return when (interestType) {
            Interest.COURSE -> (value as Course).courseName
            Interest.FIELD -> (value as Field).fieldName
            Interest.SEMESTER -> {
                val v = value as Semester
                v.section + "-" + v.semester
            }
        }
    }
}