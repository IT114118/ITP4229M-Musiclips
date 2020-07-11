package com.example.musiclips.fragments

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.musiclips.R
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
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)
        rootView.progressBar_Loading.visibility = View.GONE
        rootView.progressBar_Loading.indeterminateDrawable
            .setColorFilter(ContextCompat.getColor(context!!, R.color.colorTheme), PorterDuff.Mode.SRC_IN)

        rootView.recyclerView_Users.visibility = View.GONE
        rootView.recyclerView_Songs.visibility = View.VISIBLE
        rootView.scrollView_Users.visibility = View.GONE
        rootView.scrollView_Songs.visibility = View.VISIBLE

        rootView.editText_Search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val searchText = rootView.editText_Search.text.toString().toLowerCase(Locale.ROOT)
                if (searchText.isBlank()) {
                    rootView.recyclerView_Songs.adapter = SongsRecyclerViewAdapter(context!!,  mutableListOf<MusicModel>(), null)
                    rootView.recyclerView_Users.adapter = UsersRecyclerViewAdapter(context!!, mutableListOf<UserModel>(), false)
                } else if (rootView.scrollView_Songs.visibility == View.VISIBLE) {
                    rootView.progressBar_Loading.visibility = View.VISIBLE
                    database
                        .child("songs")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var musicModels = mutableListOf<MusicModel>()
                                snapshot.children.forEach { key ->
                                    key.children.forEach {
                                        musicModels.add(it.getValue(MusicModel::class.java)!!)
                                    }
                                }
                                musicModels = musicModels.filter { it.title.toLowerCase(Locale.ROOT).contains(searchText) }.toMutableList()
                                if (context != null) {
                                    rootView.progressBar_Loading.visibility = View.GONE
                                    rootView.recyclerView_Songs.adapter =
                                        SongsRecyclerViewAdapter(context!!, musicModels, null)
                                }
                            }
                        })
                } else {
                    rootView.progressBar_Loading.visibility = View.VISIBLE
                    database
                        .child("users")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val uid = auth.currentUser!!.uid
                                val userModels = mutableListOf<UserModel>()
                                snapshot.children.forEach { userId ->
                                    if (userId.key.toString() != uid) {
                                        val displayName = userId.child("displayName").value
                                        if (displayName != null && displayName.toString()
                                                .toLowerCase(Locale.ROOT).contains(searchText)
                                        ) {
                                            val userModel = UserModel()
                                            userModel.userId = userId.key.toString()
                                            userModel.displayName = displayName.toString()
                                            val imageUrl = userId.child("photoUrl").value
                                            if (imageUrl != null) {
                                                userModel.imageUrl = imageUrl.toString()
                                            }
                                            userModels.add(userModel)
                                        }
                                    }
                                }

                                if (context != null) {
                                    rootView.progressBar_Loading.visibility = View.GONE
                                    rootView.recyclerView_Users.adapter =
                                        UsersRecyclerViewAdapter(context!!, userModels, false)
                                }
                            }
                        })
                }
            }
        })

        rootView.button_ManageFollowers.setOnClickListener {
            rootView.recyclerView_Users.visibility = View.VISIBLE
            rootView.recyclerView_Songs.visibility = View.GONE
            rootView.scrollView_Users.visibility = View.VISIBLE
            rootView.scrollView_Songs.visibility = View.GONE
            rootView.view_Users_Highlight.setBackgroundResource(R.color.colorText)
            rootView.view_Songs_Highlight.setBackgroundResource(R.color.colorGray)
            rootView.editText_Search.setText(rootView.editText_Search.text.toString())
        }

        rootView.button_ManageFollowing.setOnClickListener {
            rootView.recyclerView_Users.visibility = View.GONE
            rootView.recyclerView_Songs.visibility = View.VISIBLE
            rootView.scrollView_Users.visibility = View.GONE
            rootView.scrollView_Songs.visibility = View.VISIBLE
            rootView.view_Users_Highlight.setBackgroundResource(R.color.colorGray)
            rootView.view_Songs_Highlight.setBackgroundResource(R.color.colorText)
            rootView.editText_Search.setText(rootView.editText_Search.text.toString())
        }

        // Inflate the layout for this fragment
        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SearchFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}