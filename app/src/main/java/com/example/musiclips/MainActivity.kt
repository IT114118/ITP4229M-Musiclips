package com.example.musiclips

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val RC_SIGN_IN : Int = 10
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Close Splash Screen
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Set up Google Auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Go to HomeActivity if last login success
        if (auth.currentUser != null) {
            val displayName = if (auth.currentUser!!.displayName == null) "" else auth.currentUser!!.displayName!!
            val photoUrl = if (auth.currentUser!!.photoUrl == null) "" else auth.currentUser!!.photoUrl.toString()
            intentToHomeActivity(displayName, auth.currentUser!!.email!!, photoUrl)
        }

        // Set up Google button listener
        button_Google.setOnClickListener {
            startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN)
        }

        // Set up Email button listener -> SignUpActivity.kt
        button_Email.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // Set up Log in button listener -> LoginActivity.kt
        textView_Login.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btn_test.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                println("DEBUG: signInResult:failed code=" + e.statusCode)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val displayName = if (auth.currentUser!!.displayName == null) "" else auth.currentUser!!.displayName!!
                    val photoUrl = if (auth.currentUser!!.photoUrl == null) "" else auth.currentUser!!.photoUrl.toString()
                    intentToHomeActivity(displayName, auth.currentUser!!.email!!, photoUrl)
                } else {
                    // TODO print error
                }
            }
    }

    private fun intentToHomeActivity(displayName : String, emailAddress: String, imageUrl: String) {
        val profilePreferences = getSharedPreferences("account", Context.MODE_PRIVATE)
        profilePreferences.edit()
            .putString("displayName", displayName)
            .putString("emailAddress", emailAddress)
            .putString("imageUrl", imageUrl)
            .apply()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}