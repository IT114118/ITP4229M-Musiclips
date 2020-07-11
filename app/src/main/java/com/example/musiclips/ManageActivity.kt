package com.example.musiclips

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.musiclips.adapters.SongsRecyclerViewAdapter
import com.example.musiclips.adapters.UsersRecyclerViewAdapter
import com.example.musiclips.models.MusicModel
import com.example.musiclips.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_manage.*

class ManageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        button_Back.setOnClickListener {
            onBackPressed()
            finish()
        }

        recyclerView_Followers.visibility = View.GONE
        recyclerView_Following.visibility = View.VISIBLE
        scrollView_Followers.visibility = View.GONE
        scrollView_Following.visibility = View.VISIBLE

        button_ManageFollowers.setOnClickListener {
            recyclerView_Followers.visibility = View.VISIBLE
            recyclerView_Following.visibility = View.GONE
            scrollView_Followers.visibility = View.VISIBLE
            scrollView_Following.visibility = View.GONE
            view_Followers_Highlight.setBackgroundResource(R.color.colorText)
            view_Following_Highlight.setBackgroundResource(R.color.colorGray)
        }

        button_ManageFollowing.setOnClickListener {
            recyclerView_Followers.visibility = View.GONE
            recyclerView_Following.visibility = View.VISIBLE
            scrollView_Followers.visibility = View.GONE
            scrollView_Following.visibility = View.VISIBLE
            view_Followers_Highlight.setBackgroundResource(R.color.colorGray)
            view_Following_Highlight.setBackgroundResource(R.color.colorText)
        }

        database
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("followers")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userIds = mutableListOf<String>()
                    snapshot.children.forEach { userIds.add(it.key.toString()) }

                    database
                        .child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userModels = mutableListOf<UserModel>()
                                snapshot.children.forEach { userId ->
                                    if (userIds.contains(userId.key.toString())) {
                                        println("DEBUG: 2 " + userId.key.toString())
                                        val userModel = UserModel()
                                        userModel.userId = userId.key.toString()
                                        val displayName = userId.child("displayName").value
                                        if (displayName != null) { userModel.displayName = displayName.toString() }
                                        val imageUrl = userId.child("photoUrl").value
                                        if (imageUrl != null) { userModel.imageUrl = imageUrl.toString() }
                                        userModels.add(userModel)
                                    }
                                }

                                //rootView.progressBar_LoadSongs.visibility = View.GONE
                                recyclerView_Followers.adapter =
                                    UsersRecyclerViewAdapter(applicationContext, userModels, false)
                            }
                        })
                }
            })

        database
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userIds = mutableListOf<String>()
                    snapshot.children.forEach { userIds.add(it.key.toString()) }

                    database
                        .child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userModels = mutableListOf<UserModel>()
                                snapshot.children.forEach { userId ->
                                    if (userIds.contains(userId.key.toString())) {
                                        println("DEBUG: 2 " + userId.key.toString())
                                        val userModel = UserModel()
                                        userModel.userId = userId.key.toString()
                                        val displayName = userId.child("displayName").value
                                        if (displayName != null) { userModel.displayName = displayName.toString() }
                                        val imageUrl = userId.child("photoUrl").value
                                        if (imageUrl != null) { userModel.imageUrl = imageUrl.toString() }
                                        userModels.add(userModel)
                                    }
                                }

                                //rootView.progressBar_LoadSongs.visibility = View.GONE
                                recyclerView_Following.adapter =
                                    UsersRecyclerViewAdapter(applicationContext, userModels, true)
                            }
                        })
                }
            })

        // Set Display Name
        textView_DisplayName.text = ""
        database
            .child("users")
            .child(auth.currentUser!!.uid)
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

        database
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("followers")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    textView_FollowersCount.text = snapshot.children.count().toString()
                }
            })

        database
            .child("users")
            .child(auth.currentUser!!.uid)
            .child("following")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    textView_FollowingCount.text = snapshot.children.count().toString()
                }
            })

    }
}