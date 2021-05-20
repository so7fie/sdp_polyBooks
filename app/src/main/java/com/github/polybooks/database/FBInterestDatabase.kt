package com.github.polybooks.database

import com.github.polybooks.core.*
import com.github.polybooks.utils.StringsManip.mergeSectionAndSemester
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestoreSettings
import java.util.concurrent.CompletableFuture


/**
 * The chosen structure for firebase is one collection for field, one for semester and one for courses.
 * Each of them will hold documents whose attribute is the name of the interest.
 * It might seem unnecessary to have 3 root level collections for interests,
 * but it is by far the best option if we potentially want each interest to hold the list of books and user associated with it
 * As each document will be able to have a book collection and a user collection.
 * Using snapshotListener here does not feel necessary as interestsare rarely changing.
 */
object FBInterestDatabase: InterestDatabase {

    /**
     * get the singleton instance of FBInterestDatabase
     * but also enable the cache
     */
    fun getInstance(): InterestDatabase {
        val settings: FirebaseFirestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings
        return this
    }



    private const val TAG: String = "FBInterestDatabase"

    // Names of the collections in Firestore
    private const val fieldCollection: String = "fieldInterest"
    private const val semesterCollection: String = "semesterInterest"
    private const val courseCollection: String = "courseInterest"

    /**
     * Add a new field document to the fields collection
     */
    fun addField(field: Field) : CompletableFuture<Field>{
        val future = CompletableFuture<Field>()

        FirebaseFirestore.getInstance()
            .collection(fieldCollection)
            .document(field.name)
            .set(field, SetOptions.merge())
            .addOnSuccessListener { future.complete(field) }
            .addOnFailureListener { future.completeExceptionally(DatabaseException("Failed to insert ${field.name} into Database because of: $it")) }


        return future
    }

    /**
     * Add a new semester document to the semesters collection
     */
    fun addSemester(semester: Semester) : CompletableFuture<Semester>{
        val future = CompletableFuture<Semester>()

        FirebaseFirestore.getInstance()
            .collection(semesterCollection)
            .document(mergeSectionAndSemester(semester))
            .set(semester, SetOptions.merge())
            .addOnSuccessListener { future.complete(semester) }
            .addOnFailureListener { future.completeExceptionally(DatabaseException("Failed to insert ${mergeSectionAndSemester(semester)} into Database because of: $it")) }

        return future
    }

    /**
     * Add a new course document to the courses collection
     */
    fun addCourse(course: Course) : CompletableFuture<Course> {
        val future = CompletableFuture<Course>()

        FirebaseFirestore.getInstance()
            .collection(courseCollection)
            .document(course.name)
            .set(course, SetOptions.merge())
            .addOnSuccessListener { future.complete(course) }
            .addOnFailureListener { future.completeExceptionally(DatabaseException("Failed to insert ${course.name} into Database because of: $it")) }

        return future
    }

    /**
     * Remove a field
     * Warning! If it has sub-collections they are currently not deleted
     * Deleting sub-collections is not implemented yet because we are not using sub-collections here.
     * A valid argument could also be that interests are very rarely removed
     * So it could be fine to removed them from the console (automatically deleting all the sub-collections)
     * Instead of using a function for it.
     */
    fun removeField(field: Field) : CompletableFuture<Boolean>  {
        val future = CompletableFuture<Boolean>()

        FirebaseFirestore.getInstance()
            .collection(fieldCollection)
            .document(field.name)
            .delete()
            .addOnSuccessListener { future.complete(true) }
            .addOnFailureListener { future.completeExceptionally(DatabaseException("Could not delete ${field.name} because of: $it")) }

        return future
    }
/*
    /**
     * Remove a semester and all its potential sub-collections (users and books)
     */
    fun removeSemester(semester: Semester) : CompletableFuture<Boolean>  {

    }

    /**
     * Remove a course and all its potential sub-collections (users and books)
     */
    fun removeCourse(course: Course) : CompletableFuture<Boolean>  {

    }

 */

    // TODO maybe add listAllBooksOfCourse(course), etc. and listAllUsersInterestedIn(interest) if relevant
    // TODO Also addUserInterest() and removeUserInterest() instead of setUserInterest()

    /**
     * List all the Fields in the database.
     * */
    override fun listAllFields(): CompletableFuture<List<Field>> {
        val future = CompletableFuture<List<Field>>()

        FirebaseFirestore.getInstance()
            .collection(fieldCollection)
            .get()
            .addOnSuccessListener { documentSnapshots ->
                if (documentSnapshots.isEmpty) {
                    future.complete(mutableListOf<Field>())
                } else {
                    // TODO I'm probably breaking all the purpose of futures here
                    val fieldsList: MutableList<Field> = mutableListOf()
                    val fields = documentSnapshots.toObjects(Field::class.java)
                    fieldsList.addAll(fields)
                    future.complete(fieldsList)
                }

            }
            .addOnFailureListener { future.completeExceptionally(DatabaseException("Could not retrieve the list of all fields because of: $it")) }

        return future


    }




    /**
     * List all the Semesters in the database.
     * */
    override fun listAllSemesters(): CompletableFuture<List<Semester>> {
        TODO("Not yet implemented")
    }

    /**
     * List all the Courses in the database.
     * */
    override fun listAllCourses(): CompletableFuture<List<Course>> {
        TODO("Not yet implemented")
    }

    /**
     * Get the interests of the specified user
     * TODO: Might need to add an authentication token to restrict authenticated users to only modify their interests.
     * */
    override fun getUserInterests(user: User): CompletableFuture<Triple<List<Field>, List<Semester>, List<Course>>> {
        TODO("Not yet implemented")
    }

    /**
     * Sets the interests of the specified user.
     * TODO: Might need to add an authentication token to restrict authenticated users to only modify their interests.
     * @return A Future to receive confirmation of success/failure asynchronously
     * */
    override fun setUserInterests(user: User, interests: List<Interest>): CompletableFuture<Unit> {
        TODO("Not yet implemented")
    }

}