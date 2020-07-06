package com.example.musiclips.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musiclips.R
import com.example.musiclips.adapters.SongsRecyclerViewAdapter
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.getFileName
import com.example.musiclips.tools.uploadMusicToFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_songs.view.*
import java.io.File
import java.io.FileInputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MySongsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MySongsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val UPLOAD_MUSIC_REQUESTCODE : Int = 10
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_my_songs, container, false)

        val models = mutableListOf<MusicModel>()
        for (x in 0..10) {
            models.add(MusicModel("Title " + x, "descrtipdsffs...", "url...", ""))
        }

        rootView.recyclerView_MySongs.adapter = SongsRecyclerViewAdapter(context!!, models)
        rootView.button_Upload.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "audio/*"
            startActivityForResult(intent, UPLOAD_MUSIC_REQUESTCODE)
        }

        // Inflate the layout for this fragment
        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            UPLOAD_MUSIC_REQUESTCODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val contentResolver = activity!!.contentResolver
                    val audioUri = data!!.data!!
                    val fileInputStream = if (audioUri.scheme.equals("content")) {
                        contentResolver.openInputStream(audioUri)!!
                    } else {
                        FileInputStream(File(audioUri.path!!))
                    }

                    /*
                    val storageRef = FirebaseStorage.getInstance().reference
                    storageRef
                        .child("storage/${firebaseUser.uid}/songs/${fileName}")
                        .putStream(stream)
                        .

                    uploadMusicToFirebaseStorage(
                        auth.currentUser!!,
                        getFileName(contentResolver, audioUri),
                        fileInputStream
                    )*/
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MySongsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MySongsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}