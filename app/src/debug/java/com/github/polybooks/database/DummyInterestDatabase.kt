package com.github.polybooks.database

import com.github.polybooks.core.*
import java.util.concurrent.CompletableFuture

/**
 * !! DO NOT USE THIS OBJECT DIRECTLY. You should use Database.interestDatabase instead.
 * */
object DummyInterestDatabase: InterestDatabase {


    val MOCK_TOPICS: List<Topic> = listOf(
        Topic("Biology"),
        Topic("Computer Science"),
        Topic("Architecture")
    )

    val mockCourses: List<Course> = listOf(
        Course("COM-101"),
        Course("CS-306"),
        Course("CS-323"),
        Course("EE-280"),
        Course("MSE-210"),
        Course("HUM-201"),
        Course("DH-405"),
        Course("ENV-444"),
        Course("MICRO-511")
    )

    val mockSemesters: List<Semester> = listOf(

        Semester("IN", "BA1"),
        Semester("SV", "BA1"),
        Semester("GC", "MA2"),
        Semester("SC", "BA6"),
        Semester("MT", "BA2"),
        Semester("MX", "BA3"),
        Semester("AR", "MA1"),
        Semester("CD", "BA4"),
        Semester("ENV", "BA5")
    )

    /**
     * Add a new field document to the fields collection
     */
    override fun addTopic(topic: Topic): CompletableFuture<Topic> {
        TODO("Not yet implemented")
    }

    /**
     * Add a new semester document to the semesters collection
     */
    override fun addSemester(semester: Semester): CompletableFuture<Semester> {
        TODO("Not yet implemented")
    }

    /**
     * Add a new course document to the courses collection
     */
    override fun addCourse(course: Course): CompletableFuture<Course> {
        TODO("Not yet implemented")
    }

    override fun listAllTopics(): CompletableFuture<List<Topic>> {
        return CompletableFuture.supplyAsync {
            MOCK_TOPICS
        }
    }

    override fun listAllSemesters(): CompletableFuture<List<Semester>> {
        return CompletableFuture.supplyAsync {
            mockSemesters
        }
    }

    override fun listAllCourses(): CompletableFuture<List<Course>> {
        return CompletableFuture.supplyAsync {
            mockCourses
        }
    }

    override fun getCurrentUserInterests(): CompletableFuture<List<Interest>> {
        return CompletableFuture.supplyAsync{
            listOf(MOCK_TOPICS.subList(0,1), mockSemesters.subList(1,3), mockCourses.subList(0,4)).flatten()
        }
    }

    override fun setCurrentUserInterests(interests: List<Interest>): CompletableFuture<List<Interest>> {
        return CompletableFuture.supplyAsync{ emptyList<Interest>()}
    }
}