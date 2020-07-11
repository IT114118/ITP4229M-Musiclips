package com.example.musiclips

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.musiclips.fragments.HomeFragment
import com.example.musiclips.fragments.MyAlbumsFragment
import com.example.musiclips.fragments.MySongsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var homeFragment: HomeFragment
    private lateinit var myAlbumsFragment: MyAlbumsFragment
    private lateinit var mySongsFragment: MySongsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        val displayNameRef = database.child("users")
            .child(auth.currentUser!!.uid).child("displayName")
        displayNameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    displayNameRef.removeEventListener(this)
                    if (auth.currentUser!!.displayName != null) {
                        displayNameRef.setValue(auth.currentUser!!.displayName.toString())
                    }
                }
            }
        })

        val photoUrlRef = database.child("users")
            .child(auth.currentUser!!.uid).child("photoUrl")
        photoUrlRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    photoUrlRef.removeEventListener(this)
                    if (auth.currentUser!!.photoUrl != null) {
                        photoUrlRef.setValue(auth.currentUser!!.photoUrl.toString())
                    }
                }
            }
        })

        homeFragment = HomeFragment.newInstance("", "")
        myAlbumsFragment = MyAlbumsFragment.newInstance("", "")
        mySongsFragment = MySongsFragment.newInstance("", "")
        setUpFragment(homeFragment)

        button_MyProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            intent.putExtra("AUTHOR_ID", auth.currentUser!!.uid)
            startActivity(intent)
        }

        button_Settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btn_menu.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }
        navigation_view.setCheckedItem(R.id.menu_Home) // Set default selected menu item
        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_Home -> { setUpFragment(homeFragment) }
                /*R.id.menu_MyAlbums -> setUpFragment(myAlbumsFragment)*/
                R.id.menu_MySongs -> { setUpFragment(mySongsFragment) }
            }

            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setUpFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout_Container, fragment)
            .addToBackStack(fragment.toString())
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }
}