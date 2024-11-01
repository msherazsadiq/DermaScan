package com.sherazsadiq.dermascan.loginsignup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.loginsignup.SignInActivity
import com.sherazsadiq.dermascan.loginsignup.SignUpActivity
import com.sherazsadiq.dermascan.loginsignup.SignUpDoctorActivity
import com.sherazsadiq.dermascan.setStatusBarColor

class RegisterOptionsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_options)
        setStatusBarColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signInButton = findViewById<TextView>(R.id.signinButton)
        signInButton.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val signUpButton = findViewById<TextView>(R.id.signupButton)
        signUpButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


        val signUpDoctor = findViewById<TextView>(R.id.signupDoctorButton)
        signUpDoctor.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null)

            val agreeButton = dialogView.findViewById<TextView>(R.id.agreeButton)
            val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            val dialog = builder.create()

            agreeButton.setOnClickListener {
                val intent = Intent(this, SignUpDoctorActivity::class.java)
                startActivity(intent)
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
