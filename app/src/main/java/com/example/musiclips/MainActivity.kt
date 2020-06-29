package com.example.musiclips

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    val UPLOAD_MUSIC_REQUESTCODE : Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_Upload.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "audio/*"
            startActivityForResult(intent, UPLOAD_MUSIC_REQUESTCODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            UPLOAD_MUSIC_REQUESTCODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val audioUri = data!!.data!!

                    val fileInputStream = if (audioUri.scheme.equals("content")) {
                        contentResolver.openInputStream(audioUri)!!
                    } else {
                        FileInputStream(File(audioUri.getPath()!!))
                    }

                    val uploadTask = uploadMusic("test", getFileName(audioUri), fileInputStream)
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result
    }

    private fun uploadMusic(userId: String, fileName: String, stream: InputStream): UploadTask {
        val storageRef = FirebaseStorage.getInstance().reference;
        val fileRef = storageRef.child(fileName)
        val mountainImagesRef = storageRef.child("${userId}/${fileName}")

// While the file names are the same, the references point to different files
        //mountainsRef.name == mountainImagesRef.name // true
        //mountainsRef.path == mountainImagesRef.path // false
        return fileRef.putStream(stream);
        fileRef.putStream(stream).addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }
}