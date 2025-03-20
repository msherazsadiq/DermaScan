package com.sherazsadiq.dermascan.loginsignup

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor

class SignUpDoctorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_doctor)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nextBtn = findViewById<TextView>(R.id.nextButton)
        nextBtn.setOnClickListener {
            val nameTxt = findViewById<EditText>(R.id.nameEditText).text.toString()
            val emailTxt = findViewById<EditText>(R.id.emailEditText).text.toString()
            val passwordTxt = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (nameTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {

                val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"
                if (passwordTxt.matches(passwordPattern.toRegex())) {
                    signUpDoctor(nameTxt, emailTxt, passwordTxt)
                } else {
                    Toast.makeText(this, "Password must contain at least one lowercase letter, one uppercase letter, and one number", Toast.LENGTH_SHORT).show()
                }

            }


        }

        val registerOptionBtn = findViewById<TextView>(R.id.registerOptionButton)
        registerOptionBtn.setOnClickListener {
            val intent = Intent(this, RegisterOptionsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // ------------------- Sign Up Doctor -------------------
    private fun signUpDoctor(name: String, email: String, password: String) {
        val doctor = Doctor()
        doctor.Name = name
        doctor.Email = email

        val firebaseWriteService = FirebaseWriteService()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing up...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firebaseWriteService.createUser(email, password) { success, message ->
            if (success) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                doctor.UID = currentUser?.uid ?: ""
                progressDialog.dismiss()

                Toast.makeText(this, "User created", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DoctorUploadDocsActivity::class.java)
                intent.putExtra("doctor", doctor)
                startActivity(intent)

            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "User creation failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}