package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.sherazsadiq.dermascan.firebase.Doctor

class DoctorProfileActivity : AppCompatActivity() {
    private lateinit var doctor: Doctor

    private lateinit var backButton: FrameLayout
    private lateinit var doctorImage: ImageView
    private lateinit var doctorName: TextView
    private lateinit var doctorSpecialization: TextView
    private lateinit var doctorExperience: TextView

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


    }
}