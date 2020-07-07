package com.example.musiclips.tools

import android.app.AlertDialog
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
            removeWarning(context, editText, textView)
        }
    })
}

fun addWarningListenerEmail(context: Context, editText: EditText, textView: TextView, warnText: String) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (!hasFocus and editText.text.isNotBlank() and !isValidEmail(editText.text)) {
            addWarning(context, editText, textView, warnText)
        }
    }
}

fun addWarning(context: Context, editText: EditText, textView: TextView, warnText: String) {
    val color = ContextCompat.getColor(context, R.color.colorRed)
    editText.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    textView.text = warnText
}

fun removeWarning(context: Context, editText: EditText, textView: TextView) {
    val color = ContextCompat.getColor(context, R.color.colorText)
    editText.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    textView.text = ""
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

fun validateDisplayNameField(context: Context?, editText: EditText, textView: TextView?) : Boolean {
    if (editText.text.isEmpty()) {
        if (context != null && textView != null) {
            val warnText = context.getString(R.string.this_field_is_required)
            addWarning(context, editText, textView, warnText)
        }
        return false
    }

    if (editText.text.length < 3) {
        if (context != null && textView != null) {
            val warnText = context.getString(R.string.the_display_name_length_error)
            addWarning(context, editText, textView, warnText)
        }
        return false
    }

    return true
}

fun validateSongNameField(context: Context?, editText: EditText, textView: TextView?) : Boolean {
    if (editText.text.isEmpty()) {
        if (context != null && textView != null) {
            val warnText = context.getString(R.string.this_field_is_required)
            addWarning(context, editText, textView, warnText)
        }
        return false
    }

    return true
}

fun validateEmailField(context: Context, editText: EditText, textView: TextView) : Boolean {
    if (editText.text.isEmpty()) {
        val warnText = context.getString(R.string.this_field_is_required)
        addWarning(context, editText, textView, warnText)
        return false
    }

    if (!isValidEmail(editText.text)) {
        val warnText = context.getString(R.string.enter_a_valid_email_address)
        addWarning(context, editText, textView, warnText)
        return false
    }

    return true
}

fun validatePasswordField(context: Context, editText: EditText, textView: TextView, checkLength: Boolean) : Boolean {
    if (editText.text.isEmpty()) {
        val warnText = context.getString(R.string.this_field_is_required)
        addWarning(context, editText, textView, warnText)
        return false
    }

    if (checkLength && editText.text.length < 6) {
        val warnText = context.getString(R.string.the_password_length_error)
        addWarning(context, editText, textView, warnText)
        return false
    }

    return true
}

fun isValidEmail(target: CharSequence): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}

fun showServerError(context: Context) {
    AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
        .setTitle(R.string.we_couldnt_log_you_in)
        .setMessage(R.string.try_sign_in_again_later)
        .setPositiveButton(android.R.string.ok, null)
        .show()
}