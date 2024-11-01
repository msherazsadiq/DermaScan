package com.sherazsadiq.dermascan.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FirebaseReadService {
    private val database = FirebaseDatabase.getInstance()

    // ------------------- Fetch All Patients -------------------
    fun fetchPatients(callback: (List<User>?, String?) -> Unit) {
        val ref = database.getReference("Users").child("Patients")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val patientList = mutableListOf<User>()
                for (patientSnapshot in snapshot.children) {
                    val user = patientSnapshot.child("UserInfo").getValue(User::class.java)
                    user?.let { patientList.add(it) }
                }
                callback(patientList, null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, error.message)
            }
        })
    }


    // ------------------- Fetch List of Unapproved Doctors -------------------
    fun fetchUnapprovedDoctors(callback: (List<Doctor>?, String?) -> Unit) {
        val ref = database.getReference("Users").child("Doctors")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unapprovedList = mutableListOf<Doctor>()
                Log.d("FetchUnapprovedDoctors", "Data snapshot: ${snapshot.value}")

                for (doctorSnapshot in snapshot.children) {
                    // Access the 'approved' attribute under 'UserInfo'
                    val approved = doctorSnapshot.child("UserInfo/approved").getValue(Boolean::class.java)

                    if (approved == false) {  // Add to list if 'approved' is false
                        val doctor = doctorSnapshot.child("UserInfo").getValue(Doctor::class.java)
                        if (doctor != null) {
                            unapprovedList.add(doctor)
                            Log.d("FetchUnapprovedDoctors", "Doctor added: ${doctor.UID}")
                        } else {
                            Log.w("FetchUnapprovedDoctors", "Null doctor object found")
                        }
                    }
                }

                Log.d("FetchUnapprovedDoctors", "Total unapproved doctors: ${unapprovedList.size}")
                callback(unapprovedList, null)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchUnapprovedDoctors", "Database error: ${error.message}")
                callback(null, error.message)
            }
        })
    }


    // ------------------- Fetch List of Approved Doctors -------------------
    fun fetchApprovedDoctors(callback: (List<Doctor>?, String?) -> Unit) {
        val ref = database.getReference("Users").child("Doctors")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unapprovedList = mutableListOf<Doctor>()
                Log.d("FetchApprovedDoctors", "Data snapshot: ${snapshot.value}")

                for (doctorSnapshot in snapshot.children) {
                    // Access the 'approved' attribute under 'UserInfo'
                    val approved = doctorSnapshot.child("UserInfo/approved").getValue(Boolean::class.java)

                    if (approved == true) {  // Add to list if 'approved' is true
                        val doctor = doctorSnapshot.child("UserInfo").getValue(Doctor::class.java)
                        if (doctor != null) {
                            unapprovedList.add(doctor)
                            Log.d("FetchApprovedDoctors", "Doctor added: ${doctor.UID}")
                        } else {
                            Log.w("FetchApprovedDoctors", "Null doctor object found")
                        }
                    }
                }

                Log.d("FetchApprovedDoctors", "Total unapproved doctors: ${unapprovedList.size}")
                callback(unapprovedList, null)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchApprovedDoctors", "Database error: ${error.message}")
                callback(null, error.message)
            }
        })
    }

    // ------------------- Fetch Complete List of All Doctors -------------------
    fun fetchAllDoctors(callback: (List<Doctor>?, String?) -> Unit) {
        val ref = database.getReference("Users").child("Doctors")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctorList = mutableListOf<Doctor>()
                Log.d("FetchAllDoctors", "Data snapshot: ${snapshot.value}")

                for (doctorSnapshot in snapshot.children) {
                    val doctor = doctorSnapshot.child("UserInfo").getValue(Doctor::class.java)
                    if (doctor != null) {
                        doctorList.add(doctor)
                        Log.d("FetchAllDoctors", "Doctor added: ${doctor.UID}")
                    } else {
                        Log.w("FetchAllDoctors", "Null doctor object found")
                    }
                }

                Log.d("FetchAllDoctors", "Total doctors: ${doctorList.size}")
                callback(doctorList, null)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchAllDoctors", "Database error: ${error.message}")
                callback(null, error.message)
            }
        })
    }
}
