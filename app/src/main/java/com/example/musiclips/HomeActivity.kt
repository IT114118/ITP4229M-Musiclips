package com.example.musiclips

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.musiclips.tools.getFileName
import com.example.musiclips.tools.uploadMusicToFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.view.*
import java.io.File
import java.io.FileInputStream


class HomeActivity : AppCompatActivity() {
    val UPLOAD_MUSIC_REQUESTCODE : Int = 10
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        button_Settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btn_menu.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }
        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_MyMusic -> {
                    val intent = Intent(this, MyMusicActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}