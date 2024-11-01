package com.sherazsadiq.dermascan.loginsignup

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sherazsadiq.dermascan.MainActivity
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.admin.ApproveDoctorActivity
import com.sherazsadiq.dermascan.setStatusBarColor
import kotlinx.coroutines.delay

class SignInActivity : AppCompatActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var showPasswordButton: ImageView
    private var isPasswordVisible: Boolean = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setStatusBarColor(this)

        passwordEditText = findViewById(R.id.passwordEditText)
        showPasswordButton = findViewById(R.id.showPasswordButton)

        showPasswordButton.setOnClickListener {
            togglePasswordVisibility()
        }

        val forgotPasswordBtn = findViewById<TextView>(R.id.forgotPasswordButton)
        forgotPasswordBtn.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        val registerOptionBtn = findViewById<TextView>(R.id.registerOptionButton)
        registerOptionBtn.setOnClickListener {
            val intent = Intent(this, RegisterOptionsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val signInBtn = findViewById<TextView>(R.id.signInButton)
        signInBtn.setOnClickListener {
            val emailTxt = findViewById<EditText>(R.id.emailEditText).text.toString()
            val passwordTxt = findViewById<EditText>(R.id.passwordEditText).text.toString()
            if (emailTxt.isEmpty() || passwordTxt.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {

                // ------------------ Admin Sign In ------------------
                if (emailTxt == "1" && passwordTxt == "1") {
                    val intent = Intent(this, ApproveDoctorActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@setOnClickListener
                }

                signInUser(emailTxt, passwordTxt)
            }
        }

    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            showPasswordButton.setImageResource(R.drawable.ic_show_password_vector)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showPasswordButton.setImageResource(R.drawable.ic_hide_password_vector)
        }
        isPasswordVisible = !isPasswordVisible
        passwordEditText.setSelection(passwordEditText.text.length) // Move cursor to the end
    }

    // ------------------- Sign In User -------------------
    private fun signInUser(email: String, password: String) {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Signing in...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    // Sign in success
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Thread.sleep(2000)
                }
            }
    }
}