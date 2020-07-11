package com.example.musiclips

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musiclips.tools.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.File
import java.io.FileInputStream


class SettingsActivity : AppCompatActivity() {
    val PICK_IMAGE : Int = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        textView_DisplayName.text = auth.currentUser!!.displayName
        textView_DisplayName2.text = auth.currentUser!!.displayName
        textView_Email.text = auth.currentUser!!.email

        database
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("displayName")
            .setValue(auth.currentUser!!.displayName.toString())

        // Set progress bar color
        progressBar_DisplayName.indeterminateDrawable
            .setColorFilter(ContextCompat.getColor(this, R.color.colorTheme), PorterDuff.Mode.SRC_IN)
        progressBar_Avatar.indeterminateDrawable
            .setColorFilter(ContextCompat.getColor(this, R.color.colorTheme), PorterDuff.Mode.SRC_IN)

        if (auth.currentUser!!.photoUrl != null) {
            DoAsync {
                val bitmap = getBitmapFromURL(auth.currentUser!!.photoUrl.toString())
                if (bitmap != null) {
                    imageView_Avatar.post {
                        imageView_Avatar.setImageBitmap(bitmap)
                    }

                    database
                        .child("users")
                        .child(auth.currentUser!!.uid)
                        .child("photoUrl")
                        .setValue(auth.currentUser!!.photoUrl.toString())
                }
            }
        }

        textView_logout.setOnClickListener { logout() }
        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }

        layout_ChangeDisplayName.setOnClickListener {
            layout_ChangeDisplayName.isEnabled = false
            val dialogLayout = layoutInflater.inflate(R.layout.alert_dialog_edittext, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editText)
            editText.setText(textView_DisplayName2.text)
            editText.setSelection(editText.text.length);
            AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setTitle(this.getString(R.string.change_display_name))
                .setView(dialogLayout)
                .setOnCancelListener { layout_ChangeDisplayName.isEnabled = true }
                .setNegativeButton(this.getString(R.string.cancel)) { dialog, _ ->
                    dialog.cancel()
                    layout_ChangeDisplayName.isEnabled = true
                }
                .setPositiveButton(this.getString(R.string.save)) { _, _ ->
                    if (validateDisplayNameField(null, editText, null)) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(editText.text.toString())
                            .build()
                        progressBar_DisplayName.visibility = View.VISIBLE
                        auth.currentUser!!.updateProfile(profileUpdates)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    textView_DisplayName.text = auth.currentUser!!.displayName
                                    textView_DisplayName2.text = auth.currentUser!!.displayName
                                } else {
                                    println("DEBUG: " + it.exception?.localizedMessage)
                                }
                                layout_ChangeDisplayName.isEnabled = true
                                progressBar_DisplayName.visibility = View.GONE
                            }
                    } else {
                        Toast.makeText(applicationContext,
                            applicationContext.getString(R.string.the_display_name_length_error),
                            Toast.LENGTH_LONG).show()
                        layout_ChangeDisplayName.isEnabled = true
                    }

                }
                .show()
        }

        layout_ChangeAvatar.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICK_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    layout_ChangeAvatar.isEnabled = false

                    val photoUri = data!!.data!!
                    val fileInputStream = if (photoUri.scheme.equals("content")) {
                        contentResolver.openInputStream(photoUri)!!
                    } else {
                        FileInputStream(File(photoUri.path!!))
                    }

                    val uploadTask = uploadAvatarToFirebaseStorage(
                        auth.currentUser!!,
                        getFileName(contentResolver, photoUri),
                        fileInputStream
                    )

                    uploadTask.addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build()
                            progressBar_Avatar.visibility = View.VISIBLE
                            auth.currentUser!!.updateProfile(profileUpdates)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        DoAsync {
                                            val bitmap = getBitmapFromURL(auth.currentUser!!.photoUrl.toString())
                                            if (bitmap != null) {
                                                imageView_Avatar.post {
                                                    imageView_Avatar.setImageBitmap(bitmap)
                                                }

                                                database
                                                    .child("users")
                                                    .child(auth.currentUser!!.uid)
                                                    .child("photoUrl")
                                                    .setValue(auth.currentUser!!.photoUrl.toString())
                                            }
                                        }
                                    } else {
                                        println("DEBUG: " + it.exception?.localizedMessage)
                                    }
                                    layout_ChangeAvatar.isEnabled = true
                                    progressBar_Avatar.visibility = View.GONE
                                }
                        }
                    }
                    .addOnFailureListener {
                        layout_ChangeAvatar.isEnabled = true
                    }
                }
            }
        }
    }

    private fun logout() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
            mGoogleSignInClient.signOut()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}