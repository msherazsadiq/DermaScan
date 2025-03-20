package com.sherazsadiq.dermascan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.DocLocation
import com.sherazsadiq.dermascan.firebase.FirebaseReadService

class SearchActivity : AppCompatActivity() {

    private lateinit var doctorAdapter: DoctorSearchAdapter
    private lateinit var recyclerViewDoctors: RecyclerView
    private var allDoctors: List<Pair<Doctor, DocLocation?>> = listOf()

    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setStatusBarColor(this)

        recyclerViewDoctors = findViewById(R.id.searchDoctor)
        recyclerViewDoctors.layoutManager = LinearLayoutManager(this)

        val backButton= findViewById<LinearLayout>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val firebaseReadService = FirebaseReadService()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firebaseReadService.fetchApprovedAndCompleteProfileDoctors { doctors, error ->
            if (doctors != null) {
                val filteredDoctors = doctors.filter { it.UID != currentUserId }
                fetchLocationsForDoctors(filteredDoctors)
            } else {
                // Handle error
            }
        }

        searchEditText = findViewById(R.id.searchEditText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDoctors(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchLocationsForDoctors(doctors: List<Doctor>) {
        val firebaseReadService = FirebaseReadService()
        val doctorLocationPairs = mutableListOf<Pair<Doctor, DocLocation?>>()

        doctors.forEach { doctor ->
            firebaseReadService.fetchDoctorLocation(doctor.UID) { location ->
                doctorLocationPairs.add(Pair(doctor, location))
                if (doctorLocationPairs.size == doctors.size) {
                    allDoctors = doctorLocationPairs
                    doctorAdapter = DoctorSearchAdapter(allDoctors)
                    recyclerViewDoctors.adapter = doctorAdapter
                }
            }
        }
    }

    private fun filterDoctors(query: String) {
        val filteredDoctors = allDoctors.filter { (doctor, location) ->
            doctor.Email.contains(query, ignoreCase = true) ||
                    doctor.Name.contains(query, ignoreCase = true) ||
                    doctor.Specialization.contains(query, ignoreCase = true) ||
                    doctor.Phone.contains(query, ignoreCase = true) ||
                    location?.LocName?.contains(query, ignoreCase = true) == true ||
                    location?.LocAddress?.contains(query, ignoreCase = true) == true ||
                    location?.LocPhone?.contains(query, ignoreCase = true) == true ||
                    location?.LocWebsite?.contains(query, ignoreCase = true) == true
        }
        doctorAdapter.updateList(filteredDoctors)
    }
}