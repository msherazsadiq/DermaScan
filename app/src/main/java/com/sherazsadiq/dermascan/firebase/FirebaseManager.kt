package com.sherazsadiq.dermascan.firebase

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sherazsadiq.dermascan.admin.DoctorDetailsActivity

class FirebaseManager {

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance()

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


    fun uploadImageToStorage(imageUri: Uri, userId: String, userType: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference.child("$userId/ProfileImage.jpg")

        // Always upload the new image
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update the user's profile image URL in the database
                    val userRef = database.getReference("Users").child(userType).child(userId).child("UserInfo")
                    val updateData = mapOf("profilePic" to uri.toString()) // Use map for update
                    userRef.updateChildren(updateData)
                        .addOnSuccessListener {
                            callback(uri.toString())
                        }
                        .addOnFailureListener {
                            callback(null)
                        }
                }
            }
            .addOnFailureListener {
                callback(null)
            }
    }








}