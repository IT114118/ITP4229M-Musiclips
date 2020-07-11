package com.example.musiclips.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.example.musiclips.UserProfileActivity
import com.example.musiclips.models.UserModel
import com.example.musiclips.tools.DoAsync
import com.example.musiclips.tools.getBitmapFromURL
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class UsersRecyclerViewAdapter(val context: Context, val userModels: List<UserModel>, val showMenu: Boolean) : RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(userModels[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_model_list_item, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = this.userModels.count()

    inner class ViewHolder(itemView: View, pContext: Context): RecyclerView.ViewHolder(itemView) {
        private val linearLayout_UserItem = itemView.findViewById<LinearLayout>(R.id.linearLayout_UserItem)
        private val imageView_UserImage = itemView.findViewById<ShapeableImageView>(R.id.imageView_UserImage)
        private val textView_DisplayName = itemView.findViewById<TextView>(R.id.textView_DisplayName)
        private val imageView_MenuPopUp = itemView.findViewById<ShapeableImageView>(R.id.imageView_MenuPopUp2)
        private val pContext = pContext

        fun bind(userModel: UserModel) {
            DoAsync {
                val bitmap = getBitmapFromURL(userModel.imageUrl)
                if (bitmap != null) {
                    imageView_UserImage.post {
                        imageView_UserImage.setImageBitmap(bitmap)
                    }
                }
            }

            textView_DisplayName.text = userModel.displayName

            linearLayout_UserItem.setOnClickListener {
                val intent = Intent(context, UserProfileActivity::class.java)
                intent.putExtra("AUTHOR_ID", userModel.userId)
                ContextCompat.startActivity(context, intent, null)
            }

            if (!showMenu) {
                imageView_MenuPopUp.visibility = View.GONE
            } else {
                imageView_MenuPopUp.setOnClickListener {
                    PopupMenu(pContext, itemView, Gravity.RIGHT).apply {
                        setOnMenuItemClickListener { item ->
                            when (item?.itemId) {
                                R.id.item_Unfollow -> {
                                    AlertDialog.Builder(
                                        pContext,
                                        AlertDialog.THEME_DEVICE_DEFAULT_DARK
                                    )
                                        .setTitle(context.getString(R.string.unfollow) + " ${userModel.displayName}?")
                                        .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                                            dialog.cancel()
                                        }
                                        .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                                            val auth = FirebaseAuth.getInstance()
                                            val database = Firebase.database.reference
                                            database
                                                .child("users")
                                                .child(auth.currentUser!!.uid)
                                                .child("following")
                                                .child(userModel.userId)
                                                .removeValue()

                                            database
                                                .child("users")
                                                .child(userModel.userId)
                                                .child("followers")
                                                .child(auth.currentUser!!.uid)
                                                .removeValue()
                                        }
                                        .show()
                                    true
                                }
                                else -> false
                            }
                        }
                        inflate(R.menu.menu_useritem)
                        show()
                    }
                }
            }
        }
    }
}