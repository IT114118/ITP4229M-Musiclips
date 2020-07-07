package com.example.musiclips.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.PorterDuff
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.musiclips.R
import com.example.musiclips.adapters.SongsRecyclerViewAdapter
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.getFileName
import com.example.musiclips.tools.validateSongNameField
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_my_songs.view.*
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit


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
        val rootView = inflater.inflate(R.layout.fragment_my_songs, container, false)
        rootView.progressBar_LoadSongs.indeterminateDrawable
            .setColorFilter(ContextCompat.getColor(context!!, R.color.colorTheme), PorterDuff.Mode.SRC_IN)

        database
            .child("songs")
            .child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val musicModel = mutableListOf<MusicModel>()
                    val children = snapshot.children
                    children.forEach {
                        musicModel.add(it.getValue(MusicModel::class.java)!!)
                    }
                    rootView.progressBar_LoadSongs.visibility = View.GONE
                    rootView.recyclerView_MySongs.adapter = SongsRecyclerViewAdapter(context!!, musicModel)
                }
            })

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
                    val stream = if (audioUri.scheme.equals("content")) {
                        contentResolver.openInputStream(audioUri)!!
                    } else {
                        FileInputStream(File(audioUri.path!!))
                    }

                    // Get audio time string
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(context, audioUri)
                    val durationStr =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val millSecond = durationStr.toLong()
                    val hms = if (TimeUnit.MILLISECONDS.toHours(millSecond) == 0.toLong()) {
                        String.format(
                            "%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(millSecond) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millSecond)
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(millSecond) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millSecond)
                            )
                        )
                    } else {
                        String.format(
                            "%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(millSecond),
                            TimeUnit.MILLISECONDS.toMinutes(millSecond) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millSecond)
                            ),
                            TimeUnit.MILLISECONDS.toSeconds(millSecond) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millSecond)
                            )
                        )
                    }
                    println("LENGTH: " + hms)

                    val dialogLayout = layoutInflater.inflate(R.layout.alert_dialog_edittext, null)
                    val editText = dialogLayout.findViewById<EditText>(R.id.editText)
                    editText.setText("Untitled Song")
                    editText.setSelection(editText.text.length);
                    AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setTitle("New song name")
                        .setView(dialogLayout)
                        //.setOnCancelListener { layout_ChangeDisplayName.isEnabled = true }
                        .setNegativeButton(this.getString(R.string.cancel)) { dialog, _ ->
                            dialog.cancel()
                            //layout_ChangeDisplayName.isEnabled = true
                        }
                        .setPositiveButton("Upload") { _, _ ->
                            if (validateSongNameField(null, editText, null)) {
                                //progressBar_DisplayName.visibility = View.VISIBLE

                                //layout_ChangeDisplayName.isEnabled = true
                                //progressBar_DisplayName.visibility = View.GONE

                                val uid = auth.currentUser!!.uid
                                val ref = database.child("songs").child(uid).push()
                                val storageRef = FirebaseStorage.getInstance().reference
                                storageRef
                                    .child("songs/${uid}/${ref.key}/${getFileName(contentResolver, audioUri)}")
                                    .putStream(stream)
                                    .addOnFailureListener {}
                                    .addOnSuccessListener {
                                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                                            val musicModel = MusicModel(
                                                editText.text.toString(),
                                                "Duration: $hms",
                                                auth.currentUser!!.photoUrl.toString(),
                                                uri.toString(),
                                                auth.currentUser!!.uid,
                                                ref.key!!
                                            )
                                            ref.setValue(musicModel)
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    getString(R.string.this_field_is_required),
                                    Toast.LENGTH_LONG
                                ).show()
                                //layout_ChangeDisplayName.isEnabled = true
                            }

                        }
                        .show()
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