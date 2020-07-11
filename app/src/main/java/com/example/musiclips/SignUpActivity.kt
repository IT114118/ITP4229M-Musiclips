package com.example.musiclips

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musiclips.tools.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.button_Back
import kotlinx.android.synthetic.main.activity_sign_up.editText_Email
import kotlinx.android.synthetic.main.activity_sign_up.editText_Password
import kotlinx.android.synthetic.main.activity_sign_up.textView_EmailWarn
import kotlinx.android.synthetic.main.activity_sign_up.textView_PasswordWarn


class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        // Set up Back button listener -> MainActivity.kt
        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }

        button_SignUp.setOnClickListener {
            val displayNameValid = validateDisplayNameField(this, editText_DisplayName, textView_DisplayNameWarn)
            val emailValid = validateEmailField(this, editText_Email, textView_EmailWarn)
            val passwordValid = validatePasswordField(this, editText_Password, textView_PasswordWarn, true)
            val confirmPasswordValid = if (passwordValid) {
                if (editText_Password.text.toString() == editText_ConfirmPassword.text.toString()) {
                    true
                } else {
                    val warnText = getString(R.string.the_confirm_password_error)
                    addWarning(this, editText_ConfirmPassword, textView_ConfirmPasswordWarn, warnText)
                    false
                }
            } else {
                if (editText_Password.text.isEmpty()) {
                    val warnText = getString(R.string.this_field_is_required)
                    addWarning(this, editText_ConfirmPassword, textView_ConfirmPasswordWarn, warnText)
                }
                false
            }

            if (displayNameValid && emailValid && passwordValid && confirmPasswordValid) {
                auth.createUserWithEmailAndPassword(editText_Email.text.toString().trim(), editText_Password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(editText_DisplayName.text.toString()).build();

                            auth.currentUser!!.updateProfile(profileUpdates)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        database
                                            .child("users")
                                            .child(auth.currentUser!!.uid)
                                            .child("displayName")
                                            .setValue(auth.currentUser!!.displayName.toString())

                                        database
                                            .child("users")
                                            .child(auth.currentUser!!.uid)
                                            .child("photoUrl")
                                            .setValue("https://firebasestorage.googleapis.com/v0/b/itp4229m-musiclips.appspot.com/o/default%2Ficon.png?alt=media")

                                        startActivity(getIntentToHomeActivity(this, auth.currentUser!!))
                                        finish()
                                    } else {
                                        println("DEBUG: " + it.exception?.localizedMessage)
                                    }
                                }
                        } else {
                            val warnText = getString(R.string.the_email_taken_error)
                            addWarning(this, editText_Email, textView_EmailWarn, warnText)
                            println("DEBUG: " + task.exception?.localizedMessage)
                        }
                    }
            }
        }

        addWarningListenerEmail(this, editText_Email, textView_EmailWarn, getString(R.string.enter_a_valid_email_address))
        addRemoveWarningListener(this, editText_DisplayName, textView_DisplayNameWarn)
        addRemoveWarningListener(this, editText_Email, textView_EmailWarn)
        addRemoveWarningListener(this, editText_Password, textView_PasswordWarn)
        addRemoveWarningListener(this, editText_ConfirmPassword, textView_ConfirmPasswordWarn)
    }
}