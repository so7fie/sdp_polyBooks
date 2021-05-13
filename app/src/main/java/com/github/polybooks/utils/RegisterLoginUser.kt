package com.github.polybooks.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.github.polybooks.EXTRA_MESSAGE
import com.github.polybooks.LoginActivity
import com.github.polybooks.RegisterActivity
import com.github.polybooks.UserProfileActivity
import com.google.firebase.auth.FirebaseUser

fun updateUI(user: FirebaseUser?, origin: Context) {
    if(user != null) {
        val intent = Intent(origin, UserProfileActivity::class.java).apply {
            putExtra(EXTRA_MESSAGE, user.displayName)
        }
        startActivity(origin, intent, null)
    }
}

fun tellTheUser(message: String, user: FirebaseUser?, origin: Context){
    Toast.makeText(origin, message,
        Toast.LENGTH_SHORT).show()
    updateUI(user, origin)
}

fun failedUser(user: FirebaseUser?, origin: Context) {
    tellTheUser("Authentication failed.", user, origin)
}

fun successUser(user: FirebaseUser?, origin: Context){
    tellTheUser("Successful authentication.", user, origin)
}