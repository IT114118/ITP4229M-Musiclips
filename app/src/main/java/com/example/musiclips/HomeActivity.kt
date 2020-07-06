package com.example.musiclips

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.musiclips.tools.getFileName
import com.example.musiclips.tools.uploadMusicToFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import java.io.File
import java.io.FileInputStream


class HomeActivity : AppCompatActivity() {
    val UPLOAD_MUSIC_REQUESTCODE : Int = 10
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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

        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        btn_menu.setOnClickListener {
            drawer.openDrawer(GravityCompat.START)
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