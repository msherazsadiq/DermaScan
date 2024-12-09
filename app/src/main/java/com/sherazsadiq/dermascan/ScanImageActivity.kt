package com.sherazsadiq.dermascan

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanImageActivity : AppCompatActivity() {

    private lateinit var uploadBtn: FrameLayout
    private lateinit var captureBtn: FrameLayout
    private lateinit var imgCap: ImageView
    private lateinit var scanInstructions: ImageView
    private lateinit var currentPhotoPath: String
    private var imageUri: Uri? = null

    private lateinit var continueBtn : TextView

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentPhotoPath", currentPhotoPath)
        outState.putParcelable("imageUri", imageUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentPhotoPath = savedInstanceState.getString("currentPhotoPath", "")
        imageUri = savedInstanceState.getParcelable("imageUri")
        if (currentPhotoPath.isNotEmpty()) {
            loadImageFromFile()
        } else if (imageUri != null) {
            imgCap.setImageURI(imageUri)
            imgCap.visibility = ImageView.VISIBLE
            scanInstructions.visibility = ImageView.GONE
        }
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
        captureBtn = findViewById(R.id.captureButton)
        imgCap = findViewById(R.id.imageCaptured)
        scanInstructions = findViewById(R.id.scanInstructions)
        continueBtn = findViewById(R.id.continueButton)

        uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }

        captureBtn.setOnClickListener {
            dispatchTakePictureIntent()
        }


        continueBtn.setOnClickListener {
            if (imageUri != null) {
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(this, "com.sherazsadiq.dermascan.fileprovider", it)
                currentPhotoPath = it.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, 1001)
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir == null || !storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException("Failed to create directory")
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1000 -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        imgCap.setImageURI(selectedImageUri)
                        imgCap.visibility = ImageView.VISIBLE
                        scanInstructions.visibility = ImageView.GONE
                        continueBtn.visibility = TextView.VISIBLE
                        imageUri = selectedImageUri
                        // TODO: Store the selectedImageUri in the database
                    }
                }
                1001 -> {
                    loadImageFromFile()
                    imageUri = Uri.fromFile(File(currentPhotoPath))
                    // TODO: Store the captured image URI in the database
                }
            }
        } else {
            Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImageFromFile() {
        val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
        if (bitmap != null) {
            val rotatedBitmap = rotateImageIfRequired(bitmap, currentPhotoPath)
            imgCap.setImageBitmap(rotatedBitmap)
            imgCap.visibility = ImageView.VISIBLE
            continueBtn.visibility = TextView.VISIBLE
            scanInstructions.visibility = ImageView.GONE
        } else {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateImageIfRequired(img: Bitmap, photoPath: String): Bitmap {
        val ei = ExifInterface(photoPath)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }
}
