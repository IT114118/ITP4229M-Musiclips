package com.example.musiclips.firebase

import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileInputStream

class Storage {

    fun uploadMusic(userId: String, fileName: String) {
        val storageRef = FirebaseStorage.getInstance().reference;
        val fileRef = storageRef.child("mountains.jpg")
        val mountainImagesRef = storageRef.child("images/mountains.jpg")

// While the file names are the same, the references point to different files
        //mountainsRef.name == mountainImagesRef.name // true
        //mountainsRef.path == mountainImagesRef.path // false


        val stream = FileInputStream(File("path/to/images/rivers.jpg"))

        var uploadTask = fileRef.putStream(stream)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
        }
    }
}