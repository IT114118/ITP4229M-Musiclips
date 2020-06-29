package com.example.musiclips.firebase

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileInputStream

class MusicStorage {

    fun uploadMusic(userId: String, fileName: String, filePath: String): UploadTask {
        val storageRef = FirebaseStorage.getInstance().reference;
        val fileRef = storageRef.child(fileName)
        val mountainImagesRef = storageRef.child("${userId}/${fileName}")

// While the file names are the same, the references point to different files
        //mountainsRef.name == mountainImagesRef.name // true
        //mountainsRef.path == mountainImagesRef.path // false
        val stream = FileInputStream(File(filePath))

        return fileRef.putStream(stream);
        fileRef.putStream(stream).addOnFailureListener {
            // Handle unsuccessful uploads

        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }
}