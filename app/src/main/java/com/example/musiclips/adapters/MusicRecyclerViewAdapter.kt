package com.example.musiclips.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.musiclips.R
import com.example.musiclips.activity_playsong
import com.example.musiclips.models.MusicModel
import com.example.musiclips.tools.DoAsync
import com.example.musiclips.tools.getBitmapFromURL
import com.google.android.material.imageview.ShapeableImageView

class MusicRecyclerViewAdapter(val context: Context, val musicModels: List<MusicModel>) : RecyclerView.Adapter<MusicRecyclerViewAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(musicModels[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.music_model_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = this.musicModels.count()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val linearLayout_SongItem = itemView.findViewById<LinearLayout>(R.id.linearLayout_SongItem)
        private val imageView_MusicImage = itemView.findViewById<ShapeableImageView>(R.id.imageView_MusicImage)
        private val textView_Title = itemView.findViewById<TextView>(R.id.textView_Title)
        private val textView_Desc = itemView.findViewById<TextView>(R.id.textView_Desc)

        fun bind(musicModel: MusicModel) {
            DoAsync {
                val bitmap = getBitmapFromURL(musicModel.imageUrl)
                if (bitmap != null) {
                    imageView_MusicImage.post {
                        imageView_MusicImage.setImageBitmap(bitmap)
                    }
                }
            }

            linearLayout_SongItem.setOnClickListener {
                if (musicModel.songsUrl != "") {
                    val intent = Intent(context, activity_playsong::class.java)
                    intent.putExtra("AUTHOR_ID", musicModel.authorId)
                    intent.putExtra("DESC", musicModel.desc)
                    intent.putExtra("IMAGE_URL", musicModel.imageUrl)
                    intent.putExtra("ITEM_KEY", musicModel.itemKey)
                    intent.putExtra("SONG_URL", musicModel.songsUrl)
                    intent.putExtra("TITLE", musicModel.title)
                    intent.putExtra("UPLOAD_TIME", musicModel.uploadTime)
                    intent.putExtra("VIEWS", musicModel.views)
                    ContextCompat.startActivity(context, intent, null)
                }
            }

            textView_Title.text = musicModel.title
            textView_Desc.text = musicModel.desc
        }
    }
}