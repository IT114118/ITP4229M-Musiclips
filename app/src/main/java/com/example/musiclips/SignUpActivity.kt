package com.example.musiclips

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musiclips.tools.addRemoveWarningListener
import com.example.musiclips.tools.getIntentToHomeActivity
import com.example.musiclips.tools.isValidEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.button_Back
import kotlinx.android.synthetic.main.activity_sign_up.editText_Email
import kotlinx.android.synthetic.main.activity_sign_up.editText_Password
import kotlinx.android.synthetic.main.activity_sign_up.textView_EmailWarn
import kotlinx.android.synthetic.main.activity_sign_up.textView_PasswordWarn


class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        // Set up Back button listener -> MainActivity.kt
        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }

        editText_Email.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                if (editText_Email.text.isNotBlank() and !isValidEmail(editText_Email.text)) {
                    val color = ContextCompat.getColor(applicationContext, R.color.colorRed)
                    textView_EmailWarn.text = getString(R.string.enter_a_valid_email_address)
                    editText_Email.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                }
            }
        }

        button_SignUp.setOnClickListener {
            val displayName = editText_DisplayName.text.toString()
            val emailAddress = editText_Email.text.toString()
            val password = editText_Password.text.toString()
            val confirmPassword = editText_ConfirmPassword.text.toString()

            if (!displayName.isBlank()
                && !emailAddress.isBlank()
                && !password.isBlank()
                && !confirmPassword.isBlank()
                && password == confirmPassword) {

                auth.createUserWithEmailAndPassword(emailAddress, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName).build();

                            auth.currentUser!!.updateProfile(profileUpdates)
                                .addOnCompleteListener {
                                    if (task.isSuccessful) {
                                        startActivity(getIntentToHomeActivity(this, auth.currentUser!!))
                                        finish()
                                    } else {
                                        println("DEBUG: " + task.exception?.localizedMessage)
                                    }
                                }
                        } else {
                            println("DEBUG: " + task.exception?.localizedMessage)
                        }
                    }
            }
        }

        addRemoveWarningListener(this, editText_DisplayName, textView_DisplayNameWarn)
        addRemoveWarningListener(this, editText_Email, textView_EmailWarn)
        addRemoveWarningListener(this, editText_Password, textView_PasswordWarn)
        addRemoveWarningListener(this, editText_ConfirmPassword, textView_ConfirmPasswordWarn)
    }
}