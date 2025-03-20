package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService

class DoctorProfileActivity : AppCompatActivity() {
    private lateinit var doctor: Doctor

    private lateinit var backButton: FrameLayout
    private lateinit var doctorImage: ImageView
    private lateinit var doctorName: TextView
    private lateinit var doctorSpecialization: TextView
    private lateinit var doctorExperience: TextView


    private lateinit var locName: TextView
    private lateinit var locAddress: TextView
    private lateinit var locPhone: TextView
    private lateinit var locWebsite: TextView

    private lateinit var shareButton: ImageView
    private lateinit var directionButton: TextView


    val firbaseReadService = FirebaseReadService()
    val firebaseWriteService = FirebaseWriteService()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_profile)
        setStatusBarColor(this)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        doctor = intent.getSerializableExtra("doctor") as Doctor

        backButton = findViewById(R.id.backButton)
        doctorImage = findViewById(R.id.DoctorImage)

        backButton.setOnClickListener {
            finish()
        }

        // display image
        Glide.with(this)
            .load(doctor.ProfilePic)
            .into(doctorImage)

        doctorName = findViewById(R.id.DoctorName)
        doctorName.text = doctor.Name

        doctorSpecialization = findViewById(R.id.DoctorSpeciality)
        doctorSpecialization.text = doctor.Specialization

        doctorExperience = findViewById(R.id.DoctorExperience)
        doctorExperience.text = "${doctor.Experience} Years"



        firbaseReadService.fetchDoctorLocation(doctor.UID) { loc ->
            if (loc == null) {
                Toast.makeText(this, "Failed to fetch location data", Toast.LENGTH_SHORT).show()
                return@fetchDoctorLocation
            }

            locName = findViewById(R.id.locName)
            locAddress = findViewById(R.id.locAddress)
            locPhone = findViewById(R.id.locPhone)
            locWebsite = findViewById(R.id.locWebsite)
            shareButton = findViewById(R.id.shareButton)
            directionButton = findViewById(R.id.directionButton)

            locName.text =
                if (loc.LocName.isNullOrEmpty() || loc.LocName == "N/A") "-" else loc.LocName
            locAddress.text =
                if (loc.LocAddress.isNullOrEmpty() || loc.LocAddress == "N/A") "-" else loc.LocAddress
            locPhone.text =
                if (loc.LocPhone.isNullOrEmpty() || loc.LocPhone == "N/A") "-" else loc.LocPhone
            locWebsite.text =
                if (loc.LocWebsite.isNullOrEmpty() || loc.LocWebsite == "N/A") "-" else loc.LocWebsite

            shareButton.setOnClickListener{
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this doctor:\n\n${doctor.Name} \nat ${loc.LocName} \n${loc.LocAddress} \n${loc.LocURL}")
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            // Set up the direction button click listener
            directionButton.setOnClickListener {
                val locUrl = loc.LocURL
                if (!locUrl.isNullOrEmpty() || locUrl != "N/A") {
                    val gmmIntentUri = Uri.parse(locUrl)
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    // Check if Google Maps is available
                    if (mapIntent.resolveActivity(packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        // Fallback to a browser if Google Maps is not installed
                        val browserIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        startActivity(browserIntent)
                    }
                } else {
                    Toast.makeText(this, "Invalid location URL", Toast.LENGTH_SHORT).show()
                }
            }

        }


    }
}