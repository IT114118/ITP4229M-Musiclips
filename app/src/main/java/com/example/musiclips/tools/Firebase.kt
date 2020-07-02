package com.example.musiclips.tools

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.InputStream
/*
fun uploadAvatarToFirebaseStorage(firebaseUser: FirebaseUser, fileName: String, stream: InputStream) : UploadTask {
    val storageRef = FirebaseStorage.getInstance().reference
    val uploadTask = storageRef
        .child("avatars/${firebaseUser.uid}/${fileName}")
        .putStream(stream)
        .addOnFailureListener {

        }.addOnSuccessListener {

        }

    val database = FirebaseDatabase.getInstance().reference;
    //database.child("users").child(userId).setValue(user)
    return uploadTask
}
*/
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

fun getUserMusic(firebaseUser: FirebaseUser) {

}