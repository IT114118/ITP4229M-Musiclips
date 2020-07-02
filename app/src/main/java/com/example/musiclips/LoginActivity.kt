package com.example.musiclips

import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musiclips.tools.addRemoveWarningListener
import com.example.musiclips.tools.getIntentToHomeActivity
import com.example.musiclips.tools.isValidEmail
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private val RC_SIGN_IN : Int = 10
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Set up Google button listener
        button_Google.setOnClickListener {
            // Set up Google Auth
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
        }

        // Set up Back button listener -> MainActivity.kt
        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }

        // Set up Log in button listener -> LoginActivity.kt
        button_Login.setOnClickListener {
            val emailAddress = editText_Email.text
            val password = editText_Password.text
            if (emailAddress.isEmpty() or password.isEmpty()) {
                var color = if (emailAddress.isEmpty()) {
                    textView_EmailWarn.text = getString(R.string.this_field_is_required)
                    ContextCompat.getColor(applicationContext, R.color.colorRed)
                } else {
                    ContextCompat.getColor(applicationContext, R.color.colorText)
                }
                editText_Email.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

                color = if (password.isEmpty()) {
                    textView_PasswordWarn.text = getString(R.string.this_field_is_required)
                    ContextCompat.getColor(applicationContext, R.color.colorRed)
                } else {
                    ContextCompat.getColor(applicationContext, R.color.colorText)
                }
                editText_Password.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            } else if (!isValidEmail(emailAddress)) {
                val color = ContextCompat.getColor(applicationContext, R.color.colorRed)
                textView_EmailWarn.text = getString(R.string.enter_a_valid_email_address)
                editText_Email.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            } else {
                auth.signInWithEmailAndPassword(
                    emailAddress.toString(),
                    password.toString()
                ).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(getIntentToHomeActivity(this, auth.currentUser!!))
                        finish()
                    } else {
                        AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                            .setTitle(R.string.we_couldnt_log_you_in)
                            .setMessage(R.string.try_sign_in_again_later)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                }
            }
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

        addRemoveWarningListener(this, editText_Email, textView_EmailWarn)
        addRemoveWarningListener(this, editText_Password, textView_PasswordWarn)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(getIntentToHomeActivity(this, auth.currentUser!!))
                            finish()
                        } else {
                            AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                                .setTitle(R.string.we_couldnt_log_you_in)
                                .setMessage(R.string.try_sign_in_again_later)
                                .setPositiveButton(android.R.string.ok, null)
                                .show()
                        }
                    }

            } catch (e: ApiException) {
                println("DEBUG: signInResult:failed code=" + e.statusCode)
            }
        }
    }
}