package com.sherazsadiq.dermascan.loginsignup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.FirebaseManager
import com.sherazsadiq.dermascan.setStatusBarColor

class ForgotPasswordActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<LinearLayout>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        val resetBtn = findViewById<TextView>(R.id.resetPasswordButton)
        resetBtn.setOnClickListener {
            sendVerificationEmailAndResetPassword()
        }

    }

    private fun sendVerificationEmailAndResetPassword() {
        val firebaseManager = FirebaseManager()
        val email = findViewById<EditText>(R.id.emailEditText).text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseManager.sendPasswordResetEmail(email) { success, error ->
            if (success) {
                Toast.makeText(this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Error sending verification email: ${error ?: "Unknown error."}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}