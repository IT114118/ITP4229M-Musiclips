package com.example.musiclips.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musiclips.R
import com.example.musiclips.models.MusicModel

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
        private val textView_Title = itemView.findViewById<TextView>(R.id.textView_Title)
        private val textView_Desc = itemView.findViewById<TextView>(R.id.textView_Desc)

        fun bind(musicModel: MusicModel) {
            textView_Title.text = musicModel.title
            textView_Desc.text = musicModel.desc
        }
    }
}