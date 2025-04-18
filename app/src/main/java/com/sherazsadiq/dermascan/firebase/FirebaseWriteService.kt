package com.sherazsadiq.dermascan.firebase

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.sherazsadiq.dermascan.chat.ChatMessage

class FirebaseWriteService {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()


    // ------------------- Create User -------------------
    fun createUser(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User creation successful
                    callback(true, null)
                } else {
                    // User creation failed
                    callback(false, task.exception?.message)
                }
            }
    }


    // ------------------- Save User Data -------------------
    fun saveUserData(user: User, callback: (Boolean) -> Unit) {
        val myRef = database.getReference("Users").child("Patients")

        myRef.child(user.UID).child("UserInfo").setValue(user)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    // ------------------- Save Doctor Data -------------------
    fun saveDoctorData(doctor: Doctor, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Users").child("Doctors")

        myRef.child(doctor.UID).child("UserInfo").setValue(doctor)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    // ------------------- Update User Data -------------------
    fun updateUserData(userId: String, userData: Map<String, Any>, callback: (Boolean) -> Unit) {
        val userRef = database.getReference("Users").child("Patients").child(userId).child("UserInfo")
        userRef.updateChildren(userData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    // ------------------- Update Doctor Data -------------------
    fun updateDoctorData(userId: String, userData: Map<String, Any>, callback: (Boolean) -> Unit) {
        val userRef = database.getReference("Users").child("Doctors").child(userId).child("UserInfo")
        userRef.updateChildren(userData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    // ------------------- Upload Scanned Image Data -------------------
    fun uploadScannedImage(userId: String, userType: String, scanData: ScanData, callback: (String?) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Users").child(userType).child(userId).child("Scans")


        val combinedDateTime = scanData.ScanDate + " " + scanData.ScanTime

        myRef.child(combinedDateTime).setValue(scanData)
            .addOnSuccessListener {
                callback("Success")
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    // ------------------- Upload App Feedback -------------------
    fun saveAppFeedback(feedback: AppFeedback, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Feedback")

        // push the feedback to the database
        myRef.push().setValue(feedback)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    // ------------------- Upload Doctor Location -------------------
    fun updateDoctorLocation(userId: String, locData: DocLocation, callback: (Boolean) -> Unit) {
        val userRef = database.getReference("Users").child("Doctors").child(userId).child("UserInfo").child("Location")
        userRef.setValue(locData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }


    fun uploadChatMessages(childID: String, userId: String, userType: String, chatMessages: List<ChatMessage>, callback: (Boolean) -> Unit) {
        val userRef = database.getReference("Users").child(userType).child(userId).child("Chats").child(childID)

        for (message in chatMessages) {
            userRef.push().setValue(message)
                .addOnSuccessListener {
                    // Handle success if needed
                }
                .addOnFailureListener {
                    callback(false)
                    return@addOnFailureListener
                }
        }
        callback(true)
    }
}