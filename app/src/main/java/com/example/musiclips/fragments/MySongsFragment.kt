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
import com.example.musiclips.tools.*
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

    val UPLOAD_MUSIC_REQUESTCODE: Int = 10
    val SHOW_INPUT_IMAGE: Int = 11
    val CHANGE_ICON_REQUESTCODE: Int = 12
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
                    snapshot.children.reversed().forEach {
                        musicModel.add(it.getValue(MusicModel::class.java)!!)
                    }
                    if (context != null) {
                        rootView.progressBar_LoadSongs.visibility = View.GONE
                        rootView.recyclerView_MySongs.adapter =
                            SongsRecyclerViewAdapter(context!!, musicModel, this@MySongsFragment)
                    }
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

    var tempKey: String = ""
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
                    val hms = getHMSString(millSecond)

                    val dialogLayout = layoutInflater.inflate(R.layout.alert_dialog_edittext, null)
                    val editText = dialogLayout.findViewById<EditText>(R.id.editText)
                    editText.setText(this.getString(R.string.untitled_song))
                    editText.setSelection(editText.text.length);
                    AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setTitle("New song name")
                        .setView(dialogLayout)
                        .setNegativeButton(this.getString(R.string.cancel)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .setPositiveButton(this.getString(R.string.upload)) { _, _ ->
                            if (validateSongNameField(null, editText, null)) {
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
                                                "$hms • ${auth.currentUser!!.displayName}",
                                                "https://firebasestorage.googleapis.com/v0/b/itp4229m-musiclips.appspot.com/o/default%2Ficon.png?alt=media",
                                                //auth.currentUser!!.photoUrl.toString(),
                                                uri.toString(),
                                                auth.currentUser!!.uid,
                                                ref.key!!,
                                                getUnixTime()
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
                            }

                        }
                        .show()
                }
            }
            SHOW_INPUT_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    tempKey = data!!.extras!!.getString("ITEM_KEY")!!
                    val intent = Intent()
                    intent.type = "image/*"
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        CHANGE_ICON_REQUESTCODE
                    )
                }
            }
            CHANGE_ICON_REQUESTCODE -> {
                if (resultCode == Activity.RESULT_OK && tempKey.length != 0) {
                    val photoUri = data!!.data!!
                    val stream = if (photoUri.scheme.equals("content")) {
                        context!!.contentResolver.openInputStream(photoUri)!!
                    } else {
                        FileInputStream(File(photoUri.path!!))
                    }

                    val key = tempKey
                    tempKey = ""
                    val uid = auth.currentUser!!.uid
                    val storageRef = FirebaseStorage.getInstance().reference
                    storageRef
                        .child("songs/${uid}/${key}/imageUrl/${getFileName(context!!.contentResolver, photoUri)}")
                        .putStream(stream)
                        .addOnFailureListener {}
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener { uri ->
                                Firebase.database.reference
                                    .child("songs")
                                    .child(uid)
                                    .child(key)
                                    .child("imageUrl")
                                    .setValue(uri.toString())
                            }
                        }
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