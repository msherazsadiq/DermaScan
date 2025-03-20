package com.sherazsadiq.dermascan.scan

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.dermascan.DisplayModelActivity
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.setStatusBarColor
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ScanImageActivity : AppCompatActivity() {

    private lateinit var uploadBtn: FrameLayout
    private lateinit var captureBtn: FrameLayout
    private lateinit var imgCap: ImageView
    private lateinit var scanInstructions: ImageView

    private var imageUri: Uri? = null
    private lateinit var continueBtn: TextView

    private var selectedBodyPart: String? = null

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val REQUEST_IMAGE_CAPTURE = 1001
    }

    private lateinit var currentPhotoPath: String

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

        captureBtn = findViewById(R.id.captureButton)
        uploadBtn = findViewById(R.id.uploadButton)
        imgCap = findViewById(R.id.imageCaptured)
        scanInstructions = findViewById(R.id.scanInstructions)
        continueBtn = findViewById(R.id.continueButton)

        findViewById<FrameLayout>(R.id.backButton).setOnClickListener {
            finish()
        }

        captureBtn.setOnClickListener {
            dispatchTakePictureIntent()
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
                REQUEST_IMAGE_CAPTURE -> { // Image capture from camera
                    addImageToGallery()
                    imgCap.setImageURI(imageUri)
                    imgCap.visibility = ImageView.VISIBLE
                    scanInstructions.visibility = ImageView.GONE
                    continueBtn.visibility = TextView.VISIBLE
                }
            }
        } else {
            Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.sherazsadiq.dermascan.fileprovider",
                        it
                    )
                    imageUri = photoURI

                    Log.e("Image URI", "URI${imageUri.toString()}")
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun addImageToGallery() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, File(currentPhotoPath).name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                File(currentPhotoPath).inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream!!)
                }
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            imageUri = uri
        }
    }
}