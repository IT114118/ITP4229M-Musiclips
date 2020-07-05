package com.example.musiclips

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set up Back button listener -> MainActivity.kt
        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }
    }
}