package com.example.musiclips

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musiclips.adapters.UsersRecyclerViewAdapter
import com.example.musiclips.models.UserModel
import com.example.musiclips.tools.getIntentToHomeActivity
import com.example.musiclips.tools.showServerError
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manage.*


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
            startActivity(getIntentToHomeActivity(this, auth.currentUser!!))
            finish()
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
                            showServerError(this)
                        }
                    }
            } catch (e: ApiException) {
                println("DEBUG: signInResult:failed code=" + e.statusCode)
                showServerError(this)
            }
        }
    }
}