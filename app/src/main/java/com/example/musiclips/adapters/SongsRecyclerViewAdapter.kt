package com.example.musiclips.adapters

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musiclips.R
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.DoAsync
import com.example.musiclips.tools.getBitmapFromURL
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SongsRecyclerViewAdapter(val context: Context, val musicModels: List<MusicModel>) : RecyclerView.Adapter<SongsRecyclerViewAdapter.ViewHolder>() {
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
                println("TITLE: " + musicModel.title)
            }

            imageView_MenuPopUp.setOnClickListener {
                PopupMenu(context, itemView, Gravity.RIGHT).apply {
                    setOnMenuItemClickListener { item ->
                        when (item?.itemId) {
                            R.id.item_Delete -> {
                                val auth = FirebaseAuth.getInstance()
                                val database = Firebase.database.reference
                                database
                                    .child("songs")
                                    .child(auth.currentUser!!.uid)
                                    .child(musicModel.itemKey)
                                    .removeValue()
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