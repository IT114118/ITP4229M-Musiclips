package com.example.musiclips.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.musiclips.R
import com.example.musiclips.activity_playsong
import com.example.musiclips.fragments.MySongsFragment
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class SongsRecyclerViewAdapter(val context: Context, val musicModels: List<MusicModel>, val fragment: MySongsFragment?) : RecyclerView.Adapter<SongsRecyclerViewAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(musicModels[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.songs_model_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.musicModels.count()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val linearLayout_SongItem = itemView.findViewById<LinearLayout>(R.id.linearLayout_SongItem)
        private val imageView_SongImage = itemView.findViewById<ShapeableImageView>(R.id.imageView_SongImage)
        private val textView_Title = itemView.findViewById<TextView>(R.id.textView_Title)
        private val textView_Desc = itemView.findViewById<TextView>(R.id.textView_Desc)
        private val imageView_MenuPopUp = itemView.findViewById<ShapeableImageView>(R.id.imageView_MenuPopUp)

        fun bind(musicModel: MusicModel) {
            DoAsync {
                val bitmap = getBitmapFromURL(musicModel.imageUrl)
                if (bitmap != null) {
                    imageView_SongImage.post {
                        imageView_SongImage.setImageBitmap(bitmap)
                    }
                }
            }

            textView_Title.text = musicModel.title
            textView_Desc.text = musicModel.desc

            linearLayout_SongItem.setOnClickListener {
                val intent = Intent(context, activity_playsong::class.java)
                intent.putExtra("AUTHOR_ID", musicModel.authorId)
                intent.putExtra("DESC", musicModel.desc)
                intent.putExtra("IMAGE_URL", musicModel.imageUrl)
                intent.putExtra("ITEM_KEY", musicModel.itemKey)
                intent.putExtra("SONG_URL", musicModel.songsUrl)
                intent.putExtra("TITLE", musicModel.title)
                intent.putExtra("UPLOAD_TIME", musicModel.uploadTime)
                intent.putExtra("VIEWS", musicModel.views)
                startActivity(context, intent, null)
            }

            if (fragment == null) {
                imageView_MenuPopUp.visibility = View.GONE
            } else {
                imageView_MenuPopUp.setOnClickListener {
                    PopupMenu(context, itemView, Gravity.RIGHT).apply {
                        setOnMenuItemClickListener { item ->
                            when (item?.itemId) {
                                R.id.item_ChangeIcon -> {
                                    val intent = Intent()
                                    intent.putExtra("ITEM_KEY", musicModel.itemKey)
                                    fragment.onActivityResult(
                                        fragment::SHOW_INPUT_IMAGE.get(),
                                        Activity.RESULT_OK,
                                        intent
                                    )
                                    true
                                }
                                R.id.item_ChangeTitle -> {
                                    val database = Firebase.database.reference
                                    val dialogLayout = LayoutInflater.from(context)
                                        .inflate(R.layout.alert_dialog_edittext, null)
                                    val editText =
                                        dialogLayout.findViewById<EditText>(R.id.editText)
                                    editText.setText(musicModel.title)
                                    editText.setSelection(editText.text.length);
                                    AlertDialog.Builder(
                                        context,
                                        AlertDialog.THEME_DEVICE_DEFAULT_DARK
                                    )
                                        .setTitle(context.getString(R.string.change_song_name))
                                        .setView(dialogLayout)
                                        .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                                            dialog.cancel()
                                        }
                                        .setPositiveButton(context.getString(R.string.save)) { _, _ ->
                                            if (validateSongNameField(null, editText, null)) {
                                                database
                                                    .child("songs")
                                                    .child(musicModel.authorId)
                                                    .child(musicModel.itemKey)
                                                    .child("title")
                                                    .setValue(editText.text.toString())
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.this_field_is_required),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                        .show()
                                    true
                                }
                                R.id.item_Delete -> {
                                    AlertDialog.Builder(
                                        context,
                                        AlertDialog.THEME_DEVICE_DEFAULT_DARK
                                    )
                                        .setTitle(context.getString(R.string.delete) + " ${musicModel.title}?")
                                        .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                                            dialog.cancel()
                                        }
                                        .setPositiveButton(context.getString(R.string.delete)) { _, _ ->
                                            val auth = FirebaseAuth.getInstance()
                                            val database = Firebase.database.reference
                                            database
                                                .child("songs")
                                                .child(auth.currentUser!!.uid)
                                                .child(musicModel.itemKey)
                                                .removeValue()
                                        }
                                        .show()
                                    true
                                }
                                else -> false
                            }
                        }
                        inflate(R.menu.menu_songitem)
                        show()
                    }
                }
            }
        }
    }
}