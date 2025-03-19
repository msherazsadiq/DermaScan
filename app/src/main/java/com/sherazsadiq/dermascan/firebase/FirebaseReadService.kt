package com.sherazsadiq.dermascan.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class FirebaseReadService {
    private val database = FirebaseDatabase.getInstance()

    // ------------------- Fetch User  -------------------
    fun fetchCurrentUser(uid: String, callback: (Any?, String?) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Users")

        // First, check in Patients
        val patientRef = database.child("Patients").child(uid).child("UserInfo")
        patientRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        callback(user, null) // Return the patient object
                    } else {
                        callback(null, "Failed to fetch patient data")
                    }
                } else {
                    // If not found in Patients, check in Doctors
                    val doctorRef = database.child("Doctors").child(uid).child("UserInfo")
                    doctorRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val doctor = snapshot.getValue(Doctor::class.java)
                                if (doctor != null) {
                                    callback(doctor, null) // Return the doctor object
                                } else {
                                    callback(null, "Failed to fetch doctor data")
                                }
                            } else {
                                callback(null, "User not found in both Patients and Doctors")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(null, "Doctor fetch cancelled: ${error.message}")
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, "Patient fetch cancelled: ${error.message}")
            }
        })
    }


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

    // ------------------- Fetch Approved and completed profile Doctors -------------------
    fun fetchApprovedAndCompleteProfileDoctors(callback: (List<Doctor>?, String?) -> Unit) {
        val ref = database.getReference("Users").child("Doctors")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val unapprovedList = mutableListOf<Doctor>()
                Log.d("FetchApprovedDoctors", "Data snapshot: ${snapshot.value}")

                for (doctorSnapshot in snapshot.children) {
                    // Access the 'approved' attribute under 'UserInfo'
                    val approved = doctorSnapshot.child("UserInfo/approved").getValue(Boolean::class.java)
                    val isProfileComplete = doctorSnapshot.child("UserInfo/profileComplete").getValue(Boolean::class.java)

                    if (approved == true && isProfileComplete == true) {  // Add to list if 'approved' is true
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

    // ------------------- Fetch All Scan Disease -------------------
    fun fetchAllScanDisease(uid: String, userType: String, callback: (List<ScanData>?, String?) -> Unit) {
        val ref = database.getReference("Users").child(userType).child(uid).child("Scans")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val scanList = mutableListOf<ScanData>()
                Log.d("FetchAllScanDisease", "Data snapshot: ${snapshot.value}")

                for (scanSnapshot in snapshot.children) {
                    val scan = scanSnapshot.getValue(ScanData::class.java)
                    if (scan != null) {
                        scanList.add(scan)
                        val combinedDateTime = "${scan.ScanDate} ${scan.ScanTime}"
                        Log.d("FetchAllScanDisease", "Scan added: $combinedDateTime")
                    } else {
                        Log.w("FetchAllScanDisease", "Null scan object found")
                    }
                }

                Log.d("FetchAllScanDisease", "Total scans: ${scanList.size}")
                callback(scanList, null)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchAllScanDisease", "Database error: ${error.message}")
                callback(null, error.message)
            }
        })


    }

    // ------------------- Fetch LLM Model API -------------------
    fun fetchApiUrl(callback: (String?, String?) -> Unit) {
        val ref = database.getReference("ngrok_url")  // Directly referencing ngrok_url

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val url = snapshot.getValue(String::class.java)
                if (url != null) {
                    callback(url, null)
                } else {
                    callback(null, "Failed to fetch API URL")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, error.message)
            }
        })
    }

}
