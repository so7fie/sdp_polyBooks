package com.github.polybooks.adapter.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.github.polybooks.R
import com.github.polybooks.core.Interest
import com.github.polybooks.database.Database
import com.github.polybooks.utils.StringsManip.getName
import com.github.polybooks.utils.firebaseUserToUser
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.CompletableFuture

class InterestAdapter: RecyclerView.Adapter<InterestAdapter.InterestHolder>() {

    private val user = firebaseUserToUser(FirebaseAuth.getInstance().currentUser)
    private var userInterests = Database.interestDatabase.getUserInterests(user)
        .thenApply { triple -> (triple.first + triple.second + triple.third).toSet() }
    private val interests: CompletableFuture<List<Interest>> =
        Database.interestDatabase.listAllInterests()
            .thenApply { triple -> triple.first + triple.second + triple.third }

    class InterestHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val button: CheckBox = itemView.findViewById(R.id.parameter_value_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestHolder {
        val v: View =
            LayoutInflater.from(parent.context).inflate(R.layout.sortby_item, parent, false)
        return InterestHolder(v)
    }

    override fun onBindViewHolder(holder: InterestHolder, position: Int) {
        val interest: Interest = interests.get()[position]
        holder.button.text = getName(interest)
        holder.button.isChecked = selected(interest)
        holder.button.setOnClickListener {
            if (selected(interest)) {
                userInterests = userInterests.thenApply { i -> i.minusElement(interest) }
            } else {
                userInterests = userInterests.thenApply { i -> i.plusElement(interest) }
            }
        }
    }

    override fun getItemCount(): Int {
        return interests.get().size
    }

    private fun selected(interest: Interest): Boolean {
        return userInterests.get().contains(interest)
    }

    fun updateUserInterests(): Unit {
        Database.interestDatabase.setUserInterests(
            user,
            userInterests.get().toList()
        )
    }
}