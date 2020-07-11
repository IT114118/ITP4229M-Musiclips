package com.example.musiclips

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.musiclips.adapters.SongsRecyclerViewAdapter
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.DoAsync
import com.example.musiclips.tools.getBitmapFromURL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user_profile.*


class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var isFollowing: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }

        val extras = intent.extras
        if (extras != null) {
            val authorId = extras.getString("AUTHOR_ID")!!
            if (authorId == auth.currentUser!!.uid) {
                button_Follow.visibility = View.GONE
            }

            // Set avatar
            database
                .child("users")
                .child(authorId)
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

            // Set Display Name
            textView_DisplayName.text = ""
            database
                .child("users")
                .child(authorId)
                .child("displayName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val displayName = snapshot.value
                        if (displayName != null) {
                            textView_DisplayName.text = displayName.toString()
                        }
                    }
                })

            // Set Songs list and count
            database
                .child("songs")
                .child(authorId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val musicModel = mutableListOf<MusicModel>()
                        snapshot.children.reversed().forEach {
                            musicModel.add(it.getValue(MusicModel::class.java)!!)
                        }
                            //rootView.progressBar_LoadSongs.visibility = View.GONE
                        recyclerView_Songs.adapter =
                            SongsRecyclerViewAdapter(applicationContext, musicModel, null)
                        textView_SongsCount.text = musicModel.size.toString()
                    }
                })

            database
                .child("users")
                .child(authorId)
                .child("followers")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        textView_FollowersCount.text = snapshot.children.count().toString()
                    }
                })

            database
                .child("users")
                .child(authorId)
                .child("following")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        textView_FollowingCount.text = snapshot.children.count().toString()
                    }
                })

            // Set up Follow button
            button_Follow.isEnabled = false
            button_Follow.text = getString(R.string.loading)
            val followingText = "✔ " + getString(R.string.following)
            database
                .child("users")
                .child(auth.currentUser!!.uid)
                .child("following")
                .child(authorId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isFollowed = snapshot.value
                        if (isFollowed == null) {
                            button_Follow.text = getString(R.string.follow)
                        } else {
                            button_Follow.text = followingText
                        }

                        button_Follow.isEnabled = true
                        button_Follow.setOnClickListener {
                            if (button_Follow.text == getString(R.string.follow)) {
                                database
                                    .child("users")
                                    .child(auth.currentUser!!.uid)
                                    .child("following")
                                    .child(authorId)
                                    .setValue("1")

                                database
                                    .child("users")
                                    .child(authorId)
                                    .child("followers")
                                    .child(auth.currentUser!!.uid)
                                    .setValue("1")

                                button_Follow.text = followingText
                            } else {
                                database
                                    .child("users")
                                    .child(auth.currentUser!!.uid)
                                    .child("following")
                                    .child(authorId)
                                    .removeValue()

                                database
                                    .child("users")
                                    .child(authorId)
                                    .child("followers")
                                    .child(auth.currentUser!!.uid)
                                    .removeValue()

                                button_Follow.text = getString(R.string.follow)
                            }
                        }
                    }
                })
        }
    }
}