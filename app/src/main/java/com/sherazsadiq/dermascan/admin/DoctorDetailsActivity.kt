package com.sherazsadiq.dermascan.admin

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.setStatusBarColor

class DoctorDetailsActivity : AppCompatActivity() {

    lateinit var doctor: Doctor
    private val firebaseWriteService = FirebaseWriteService()
    private lateinit var approveBtn : TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_details)
        setStatusBarColor(this)

        // Get the Doctor instance from the intent
        doctor = intent.getSerializableExtra("doctor") as Doctor

        val backbtn = findViewById<LinearLayout>(R.id.backButton)
        backbtn.setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.doctorNameTextView).text = doctor.Name
        findViewById<TextView>(R.id.doctorEmailTextView).text = doctor.Email
        findViewById<TextView>(R.id.doctorDateTextView).text = doctor.CreatedAt



        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        viewPager.adapter = DocumentPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Degree Certificate"
                1 -> "Government ID"
                2 -> "Medical License"
                else -> null
            }
        }.attach()


        approveBtn = findViewById(R.id.approveButton)

        if(doctor.Approved){
            approveBtn.text = "Unapprove Doctor"

        }else{
            approveBtn.text = "Approve Doctor"
        }

        approveBtn.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Changing Doctor Status...")
            progressDialog.setCancelable(false)
            progressDialog.show()

            if(doctor.Approved){
                doctor.Approved = false
            }else{
                doctor.Approved = true
            }

            // Update the doctor in the database
            firebaseWriteService.saveDoctorData(doctor) { saveSuccess ->
                progressDialog.dismiss()
                if (saveSuccess) {
                    finish()
                } else {
                    // Show error message

                }

            }
        }
    }
}