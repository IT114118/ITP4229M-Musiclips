package com.example.musiclips

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.musiclips.tools.DoAsync
import com.example.musiclips.tools.getBitmapFromURL
import com.example.musiclips.tools.getHMSString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_playsong.*


class activity_playsong : AppCompatActivity() {
    lateinit var mediaPlayer : MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playsong)

        val auth = FirebaseAuth.getInstance()
        val database = Firebase.database.reference

        val extras = intent.extras
        if (extras != null) {
            val authorId = extras.getString("AUTHOR_ID")
            val desc = extras.getString("DESC")
            val imageUrl = extras.getString("IMAGE_URL")
            val itemKey = extras.getString("ITEM_KEY")
            val songUrl = extras.getString("SONG_URL")
            val title = extras.getString("TITLE")
            val uploadTime = extras.getLong("UPLOAD_TIME")
            val views = extras.getInt("VIEWS")

            if (extras.getInt("ADD_VIEW") == 1) {
                Firebase.database.reference
                    .child("songs")
                    .child(authorId.toString())
                    .child(itemKey.toString())
                    .child("views")
                    .setValue(views + 1)
            }

            button_ViewProfile.setOnClickListener {
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra("AUTHOR_ID", authorId)
                startActivity(intent)
            }

            database
                .child("users")
                .child(authorId.toString())
                .child("photoUrl")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val imageUrl = snapshot.value
                        if (imageUrl != null) {
                            DoAsync {
                                val bitmap = getBitmapFromURL(imageUrl.toString())
                                if (bitmap != null) {
                                    imageView_Avatar.post {
                                        imageView_Avatar.setImageBitmap(bitmap)
                                    }
                                }
                            }
                        }
                    }
                })

            textView_SongAuthor.text = ""
            database
                .child("users")
                .child(authorId.toString())
                .child("displayName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val displayName = snapshot.value
                        if (displayName != null) {
                            textView_SongAuthor.text = displayName.toString()
                        }
                    }
                })

            textView_SongTitle.text = title

            DoAsync {
                val bitmap = getBitmapFromURL(imageUrl)
                if (bitmap != null) {
                    imageView_SongImage.post {
                        imageView_SongImage.setImageBitmap(bitmap)
                    }
                }
            }

            button_Back.setOnClickListener {
                onBackPressed()
                finish()
            }

            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(this,  Uri.parse(songUrl))
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener {
                seekBar.max = mediaPlayer.duration / 1000
                textView_Duration.text = getHMSString((mediaPlayer.duration).toLong())
                btn_play.setImageResource(R.drawable.pause)
                mediaPlayer.start()

                btn_backward.setOnClickListener { mediaPlayer.seekTo(mediaPlayer.currentPosition - 5000) }
                btn_forward.setOnClickListener { mediaPlayer.seekTo(mediaPlayer.currentPosition + 5000) }
                btn_play.setOnClickListener {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                        btn_play.setImageResource(R.drawable.play)
                    } else {
                        mediaPlayer.start()
                        btn_play.setImageResource(R.drawable.pause)
                    }
                }

                val seekBarUpdate: Runnable = object : Runnable {
                    override fun run() {
                        textView_CurrentTime.text =
                            getHMSString((mediaPlayer.currentPosition).toLong())
                        seekBar.progress = mediaPlayer.currentPosition / 1000
                        seekBar.postDelayed(this, 1)
                    }
                }
                seekBarUpdate.run()
            }
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.seekTo(0)
                btn_play.setImageResource(R.drawable.play)
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    if (b) { mediaPlayer.seekTo(i * 1000) }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
        btn_play.setImageResource(R.drawable.play)
    }
}