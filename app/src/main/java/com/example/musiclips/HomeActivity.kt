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
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var homeFragment: HomeFragment
    private lateinit var myAlbumsFragment: MyAlbumsFragment
    private lateinit var mySongsFragment: MySongsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        homeFragment = HomeFragment.newInstance("", "")
        myAlbumsFragment = MyAlbumsFragment.newInstance("", "")
        mySongsFragment = MySongsFragment.newInstance("", "")
        setUpFragment(homeFragment)

        button_Settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            drawer_layout.closeDrawer(GravityCompat.START)
        }

        btn_menu.setOnClickListener { drawer_layout.openDrawer(GravityCompat.START) }
        navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_Home -> setUpFragment(homeFragment)
                R.id.menu_MyAlbums -> setUpFragment(myAlbumsFragment)
                R.id.menu_MySongs -> setUpFragment(mySongsFragment)
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