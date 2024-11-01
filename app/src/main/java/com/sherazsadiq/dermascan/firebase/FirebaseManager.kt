package com.sherazsadiq.dermascan.firebase

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sherazsadiq.dermascan.admin.DoctorDetailsActivity

class FirebaseManager {

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun sendPasswordResetEmail(email: String, callback: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null)    // Reset email sent successfully
            } else {
                Log.e(TAG, "Error sending password reset email: ${task.exception?.message}")
                callback(false, task.exception?.message)
            }
        }
    }



    fun uploadFile(fileUri: String, fileName: String, doctorId: String): Task<Uri> {
        val storageRef = storage.reference.child("documents/$doctorId/$fileName.pdf")
        val file = Uri.parse(fileUri)
        return storageRef.putFile(file).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }
    }

    fun getFileUrl(doctorId: String, fileName: String, callback: (String?) -> Unit) {
        val storageRef: StorageReference = storage.reference.child("documents/$doctorId/$fileName.pdf")

        Log.d("FirebaseStorage", "Attempting to retrieve file URL for: $fileName at path: ${storageRef.path}")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FirebaseStorage", "Successfully retrieved file URL: $uri")
            // Use the callback to return the URL
            callback(uri.toString())
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Error getting file URL: ${exception.localizedMessage}")
            // Call the callback with null on failure
            callback(null)
        }
    }




}