package com.example.musiclips

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musiclips.tools.*
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

        // Set up Log in button listener -> HomeActivity.kt
        button_Login.setOnClickListener {
            val emailValid = validateEmailField(this, editText_Email, textView_EmailWarn)
            val passwordValid = validatePasswordField(this, editText_Password, textView_PasswordWarn, false)

            if (emailValid && passwordValid) {
                auth.signInWithEmailAndPassword(editText_Email.text.toString(), editText_Password.text.toString())
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
            }
        }

        addWarningListenerEmail(this, editText_Email, textView_EmailWarn, getString(R.string.enter_a_valid_email_address))
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