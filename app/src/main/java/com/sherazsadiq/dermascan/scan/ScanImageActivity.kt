package com.sherazsadiq.dermascan.scan

import android.annotation.SuppressLint
import android.content.Intent
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
import com.sherazsadiq.dermascan.DisplayModelActivity
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.setStatusBarColor

class ScanImageActivity : AppCompatActivity() {

    private lateinit var uploadBtn: FrameLayout
    private lateinit var imgCap: ImageView
    private lateinit var scanInstructions: ImageView

    private var imageUri: Uri? = null
    private lateinit var continueBtn: TextView

    private var selectedBodyPart: String? = null

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val REQUEST_IMAGE_CAPTURE = 1001
    }



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor(this)
        setContentView(R.layout.activity_scan_image)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        uploadBtn = findViewById(R.id.uploadButton)
        imgCap = findViewById(R.id.imageCaptured)
        scanInstructions = findViewById(R.id.scanInstructions)
        continueBtn = findViewById(R.id.continueButton)

        findViewById<FrameLayout>(R.id.backButton).setOnClickListener {
            finish()
        }

        uploadBtn.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)

        }


        continueBtn.setOnClickListener {
            if (imageUri != null) {
                val intent = Intent(this, DisplayModelActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }






    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1000 -> { // Image upload from gallery
                    try {
                        val selectedImageUri: Uri? = data?.data
                        if (selectedImageUri != null) {
                            imgCap.setImageURI(selectedImageUri)
                            imgCap.visibility = ImageView.VISIBLE
                            scanInstructions.visibility = ImageView.GONE
                            continueBtn.visibility = TextView.VISIBLE
                            imageUri = selectedImageUri
                        } else {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "An error occurred while loading the image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

}