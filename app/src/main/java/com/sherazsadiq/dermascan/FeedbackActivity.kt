package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.sherazsadiq.dermascan.firebase.AppFeedback
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService

class FeedbackActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feedback)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userType = intent.getStringExtra("userType") ?: ""
        val userUID = FirebaseAuth.getInstance().currentUser?.uid


        val backButton = findViewById<FrameLayout>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val feedbackEditText = findViewById<EditText>(R.id.feedbackEditText)
        val submitFeedbackButton = findViewById<TextView>(R.id.submitFeedbackButton)

        submitFeedbackButton.setOnClickListener {
            val starRating = ratingBar.rating
            val feedbackText = feedbackEditText.text.toString()

            // Use the starRating and feedbackText variables as needed
            if(starRating == 0.0f) {
                Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show()
            } else if (feedbackText == "") {
                Toast.makeText(this, "Please provide feedback", Toast.LENGTH_SHORT).show()
            } else {

                val appFeedback = AppFeedback(starRating, feedbackText, userType, userUID ?: "")

                val firebaseWriteService = FirebaseWriteService()
                firebaseWriteService.saveAppFeedback(appFeedback) { success ->
                    if (success) {
                        Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }

    }
}