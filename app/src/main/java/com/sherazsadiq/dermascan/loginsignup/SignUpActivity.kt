package com.sherazsadiq.dermascan.loginsignup

import android.annotation.SuppressLint
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
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor
import java.text.SimpleDateFormat
import java.util.Date

class SignUpActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val registerOptionBtn = findViewById<TextView>(R.id.registerOptionButton)
        registerOptionBtn.setOnClickListener {
            val intent = Intent(this, RegisterOptionsActivity::class.java)
            startActivity(intent)
            finish()
        }


        val signUpBtn = findViewById<TextView>(R.id.signUpButton)
        signUpBtn.setOnClickListener {
            val nameTxt = findViewById<EditText>(R.id.nameEditText).text.toString()
            val emailTxt = findViewById<EditText>(R.id.emailEditText).text.toString()
            val passwordTxt = findViewById<EditText>(R.id.passwordEditText).text.toString()

            if (nameTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                signUpUser(nameTxt, emailTxt, passwordTxt)
            }
        }

    }

    // ------------------- Sign Up User -------------------
    private fun signUpUser(name: String, email: String, password: String) {
        val user = User()
        user.Name = name
        user.Email = email

        val firebaseWriteService = FirebaseWriteService()
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing up...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        firebaseWriteService.createUser(email, password) { success, message ->
            if (success) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                user.UID = currentUser?.uid ?: ""

                val date : Date = Date()
                val sdf = SimpleDateFormat("dd MMMM yyyy")
                val currentDate = sdf.format(date)
                user.CreatedAt = currentDate

                firebaseWriteService.saveUserData(user) { saveSuccess ->
                    progressDialog.dismiss()
                    if (saveSuccess) {
                        Toast.makeText(this, "User created and data saved successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RegisterOptionsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "User creation failed: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}