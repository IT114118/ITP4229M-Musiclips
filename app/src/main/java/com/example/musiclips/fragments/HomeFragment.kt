package com.example.musiclips.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musiclips.R
import com.example.musiclips.adapters.MusicRecyclerViewAdapter
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.getUnixTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlin.random.Random


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        updateRecommended(rootView)
        updateNewSongs(rootView)
        updateMostViewed(rootView)

        rootView.refreshLayout_Container.setOnRefreshListener {
            updateRecommended(rootView)
            updateNewSongs(rootView)
            updateMostViewed(rootView)
            rootView.refreshLayout_Container.isRefreshing = false
        }

        // Inflate the layout for this fragment
        return rootView
    }

    private fun updateRecommended(view: View) {
        val newRecommended = database.child("songs").orderByChild("uploadTime")
        newRecommended.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var musicModel = mutableListOf<MusicModel>()
                snapshot.children.forEach { user ->
                    user.children.forEach {
                        musicModel.add(it.getValue(MusicModel::class.java)!!)
                    }
                }
                if (context != null) {
                    musicModel.shuffle()//.random(Random(getUnixTime()/1000))
                    musicModel = musicModel.take(5).toMutableList()
                    //rootView.progressBar_LoadSongs.visibility = View.GONE
                    view.recyclerView_Recommended.adapter =
                        MusicRecyclerViewAdapter(context!!, musicModel, 0)
                }
                newRecommended.removeEventListener(this)
            }
        })
    }

    private fun updateNewSongs(view: View) {
        val newSongs = database.child("songs").orderByChild("uploadTime")
        newSongs.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    var musicModel = mutableListOf<MusicModel>()
                    snapshot.children.forEach { user ->
                        user.children.forEach {
                            musicModel.add(it.getValue(MusicModel::class.java)!!)
                        }
                    }
                    if (context != null) {
                        musicModel.sortByDescending { it.uploadTime }
                        musicModel = musicModel.take(5).toMutableList()
                        //rootView.progressBar_LoadSongs.visibility = View.GONE
                        view.recyclerView_NewSongs.adapter =
                            MusicRecyclerViewAdapter(context!!, musicModel, 1)
                    }
                    newSongs.removeEventListener(this)
                }
            })
    }

    private fun updateMostViewed(view: View) {
        val mostViewed = database.child("songs").orderByChild("views")
        mostViewed.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                var musicModel = mutableListOf<MusicModel>()
                snapshot.children.forEach { user ->
                    user.children.forEach {
                        musicModel.add(it.getValue(MusicModel::class.java)!!)
                    }
                }
                if (context != null) {
                    musicModel.sortByDescending { it.views }
                    //rootView.progressBar_LoadSongs.visibility = View.GONE
                    musicModel = musicModel.take(5).toMutableList()
                    view.recyclerView_MostViewed.adapter =
                        MusicRecyclerViewAdapter(context!!, musicModel, 2)
                }
                mostViewed.removeEventListener(this)
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                HomeFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
