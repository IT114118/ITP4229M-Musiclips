package com.example.musiclips.tools

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.musiclips.HomeActivity
import com.example.musiclips.R
import com.google.firebase.auth.FirebaseUser

fun addRemoveWarningListener(context: Context, editText: EditText, textView: TextView) {
    editText.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val color = ContextCompat.getColor(context, R.color.colorText)
            editText.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            textView.text = ""
        }
    })
}

fun getIntentToHomeActivity(context: Context, firebaseUser: FirebaseUser) : Intent {
    val displayName = if (firebaseUser.displayName != null) firebaseUser.displayName else ""
    val emailAddress = firebaseUser.email
    val imageUrl = if (firebaseUser.photoUrl != null) firebaseUser.photoUrl.toString() else ""
    val profilePreferences = context.getSharedPreferences("account", Context.MODE_PRIVATE)
    profilePreferences.edit()
        .putString("displayName", displayName)
        .putString("emailAddress", emailAddress)
        .putString("imageUrl", imageUrl)
        .apply()

    val intent = Intent(context, HomeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return intent
}

fun isValidEmail(target: CharSequence): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}