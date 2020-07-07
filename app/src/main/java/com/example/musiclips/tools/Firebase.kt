package com.example.musiclips.tools

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.provider.OpenableColumns
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit


fun uploadAvatarToFirebaseStorage(firebaseUser: FirebaseUser, fileName: String, stream: InputStream) : UploadTask {
    val storageRef = FirebaseStorage.getInstance().reference
    return storageRef.child("avatars/${firebaseUser.uid}/${fileName}").putStream(stream)
}

fun getBitmapFromURL(src: String?): Bitmap? {
    return try {
        val url = URL(src)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        null
    }
}

fun uploadMusicToFirebaseStorage(firebaseUser: FirebaseUser, fileName: String, stream: InputStream) : UploadTask {
    val storageRef = FirebaseStorage.getInstance().reference
    return storageRef.child("storage/${firebaseUser.uid}/music/${fileName}").putStream(stream)


    //fileRef.putStream(stream).addOnFailureListener {
        // Handle unsuccessful uploads

    //}.addOnSuccessListener {
        // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
        // ...
    //}
}

class DoAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    init { execute() }

    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
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
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}

fun getHMSString(millSecond: Long) : String {
    return if (TimeUnit.MILLISECONDS.toHours(millSecond) == 0.toLong()) {
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
}

fun getUnixTime() : Long {
    return System.currentTimeMillis() / 1000L
}