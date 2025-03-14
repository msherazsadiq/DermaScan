package com.sherazsadiq.dermascan.scan

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*


class ScanHistoryActivity : AppCompatActivity() {
    private var currentUserData: Any? = null
    private var userType: String? = null

    private lateinit var adapter: ScanHistoryAdapter
    private val firebaseReadService = FirebaseReadService()

    private lateinit var scanHistoryRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor(this)

        setContentView(R.layout.activity_scan_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bckBtn = findViewById<FrameLayout>(R.id.backButton)
        bckBtn.setOnClickListener {
            finish()
        }

        scanHistoryRecyclerView = findViewById<RecyclerView>(R.id.scanHistoryRecyclerView)
        scanHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ScanHistoryAdapter(emptyList())
        scanHistoryRecyclerView.adapter = adapter

        fetchUserData()

    }


    private fun fetchScanHistory(uid: String, userType: String) {
        firebaseReadService.fetchAllScanDisease(uid, userType) { scanList, error ->
            if (scanList != null) {
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())

                val sortedScanList = scanList.sortedWith(compareByDescending { scan ->
                    val dateTimeString = "${scan.ScanDate} ${scan.ScanTime}"
                    dateTimeFormat.parse(dateTimeString)
                })

                adapter.updateData(sortedScanList)
            } else {
                // Handle error
            }
        }
    }


    // ----------------- Fetch User Data -----------------
    private fun fetchUserData() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val firebaseReadService = FirebaseReadService()

            firebaseReadService.fetchCurrentUser(uid) { user, error ->
                if (user != null) {
                    currentUserData = user
                    userType = if (user is User) "Patients" else "Doctors"


                    val userUID = when (val userData = currentUserData) {
                        is User -> userData.UID
                        is Doctor -> userData.UID
                        else -> null
                    }

                    if (userUID != null && userType != null) {
                        fetchScanHistory(userUID, userType!!)
                    } else {
                        Log.e("ScanResultsActivity", "User UID or userType is null")
                    }


                } else {
                    if (error != null) {
                        Log.e("ScanResultsActivity", "Error fetching user: $error")
                    } else {
                        Log.e("ScanResultsActivity", "Unknown error occurred while fetching user.")
                    }
                }
            }
        } else {
            Log.e("ScanResultsActivity", "No current user")
        }
    }

}