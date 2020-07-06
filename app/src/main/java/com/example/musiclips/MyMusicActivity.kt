package com.example.musiclips

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import com.example.musiclips.tools.getFileName
import com.example.musiclips.tools.uploadMusicToFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_my_music.*
import java.io.File
import java.io.FileInputStream

class MyMusicActivity : AppCompatActivity() {
    val UPLOAD_MUSIC_REQUESTCODE : Int = 10
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_music)

        auth = FirebaseAuth.getInstance()

        button_Upload.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "audio/*"
            startActivityForResult(intent, UPLOAD_MUSIC_REQUESTCODE)
        }

        button_Settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btn_menu.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }
        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_Home -> {

                }
            }

            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            UPLOAD_MUSIC_REQUESTCODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val audioUri = data!!.data!!
                    val fileInputStream = if (audioUri.scheme.equals("content")) {
                        contentResolver.openInputStream(audioUri)!!
                    } else {
                        FileInputStream(File(audioUri.path!!))
                    }

                    uploadMusicToFirebaseStorage(
                        auth.currentUser!!,
                        getFileName(contentResolver, audioUri),
                        fileInputStream
                    )
                }
            }
        }
    }
}