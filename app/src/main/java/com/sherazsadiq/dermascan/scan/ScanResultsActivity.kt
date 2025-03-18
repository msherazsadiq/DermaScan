package com.sherazsadiq.dermascan.scan

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.sherazsadiq.dermascan.HomeActivity
import com.sherazsadiq.dermascan.R
import com.sherazsadiq.dermascan.firebase.Doctor
import com.sherazsadiq.dermascan.firebase.FirebaseManager
import com.sherazsadiq.dermascan.firebase.FirebaseReadService
import com.sherazsadiq.dermascan.firebase.FirebaseWriteService
import com.sherazsadiq.dermascan.firebase.ScanData
import com.sherazsadiq.dermascan.firebase.User
import com.sherazsadiq.dermascan.setStatusBarColor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log


data class ApiResponse(
    val probabilities: Map<String, Double>
)

class ScanResultsActivity : AppCompatActivity() {

    private lateinit var scannedImage: ImageView
    private lateinit var firstDName: TextView
    private lateinit var firstDPercentage: TextView
    private lateinit var secondDName: TextView
    private lateinit var secondDPercentage: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var scanAgain: LinearLayout
    private lateinit var docDownload: FrameLayout

    private var currentUserData: Any? = null
    private var userType: String? = null

    private lateinit var imageUri: Uri
    private var selectedBodyPart: String? = null

    private var isDataUploaded : Boolean = false


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setStatusBarColor(this)

        setContentView(R.layout.activity_scan_results)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        findViewById<FrameLayout>(R.id.backButton).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        scannedImage = findViewById(R.id.scannedImage)
        firstDName = findViewById(R.id.firstDiseaseName)
        firstDPercentage = findViewById(R.id.firstDiseasePercentage)
        secondDName = findViewById(R.id.secondDiseaseName)
        secondDPercentage = findViewById(R.id.secondDiseasePercentage)

        selectedBodyPart = intent.getStringExtra("selectedBodyPart")
        val imageUriString = intent.getStringExtra("imageUri")


        if (!imageUriString.isNullOrEmpty()) {
            imageUri = Uri.parse(imageUriString)
            Glide.with(this)
                .load(imageUri)
                .into(scannedImage)

            getScanResults(imageUri)
        }

        scanAgain = findViewById(R.id.scanAgainButton)
        scanAgain.setOnClickListener {

            getScanResults(imageUri)

        }



        docDownload = findViewById(R.id.docDownloadButton)
        docDownload.setOnClickListener {
            // Convert URI to Bitmap
            val userImageBitmap = uriToBitmap(this, imageUri)

            if (userImageBitmap != null) {
                val pdfPath = generateSkinScanPDF(
                    this,
                    userImageBitmap,  // Pass the converted Bitmap
                    "Psoriasis",
                    "85%",
                    "Eczema",
                    "60%",
                    "Forearm",
                    "Lahore, Pakistan"
                )

                if (pdfPath != null) {
                    Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show()
                }

            } else {
                Toast.makeText(this, "Failed to convert image", Toast.LENGTH_SHORT).show()
            }
        }


    }

    fun uriToBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }






    // ----------------- Fetch User Data -----------------
    private fun fetchUserDataAndUpload() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val firebaseReadService = FirebaseReadService()

            firebaseReadService.fetchCurrentUser(uid) { user, error ->
                if (user != null) {
                    currentUserData = user
                    userType = if (user is User) "Patients" else "Doctors"

                    // Now we have user data, call uploadData()
                    uploadData(currentUserData, userType)
                } else {
                    if (error != null) {
                        Log.e("ScanResultsActivity", "Error fetching user: $error")
                    } else {
                        Log.e("ScanResultsActivity", "Unknown error occurred while fetching user.")
                    }
                }
            }
        } else {
            Log.e("ScanResultsActivity", "No current user")
        }
    }

    private fun uploadData(currentUser: Any?, userType: String?) {
        // Ensure that the TextViews are properly initialized
        if (!::firstDName.isInitialized || !::firstDPercentage.isInitialized ||
            !::secondDName.isInitialized || !::secondDPercentage.isInitialized) {
            Log.e("ScanResultsActivity", "TextViews are not initialized")
            return
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        // Create and initialize scanData
        val scanData = ScanData().apply {
            ScanDate = currentDate
            ScanTime = currentTime
            FirstDName = firstDName.text.toString()
            FirstDPercentage = firstDPercentage.text.toString()
            SecondDName = secondDName.text.toString()
            SecondDPercentage = secondDPercentage.text.toString()
            ScanBodyPart = selectedBodyPart ?: ""
        }

        val firebaseWriteService = FirebaseWriteService()
        val firebaseManager = FirebaseManager()

        val uid = when (currentUser) {
            is User -> currentUser.UID
            is Doctor -> currentUser.UID
            else -> null
        }

        if (uid != null && userType != null) {
            firebaseWriteService.uploadScannedImage(uid, userType, scanData) { imageUrl ->
                if (imageUrl != null) {
                    Log.d("ScanResultsActivity", "Data Saved successfully: $imageUrl")
                } else {
                    Log.e("ScanResultsActivity", "Error uploading Data")
                }
            }

            // Upload the scanned image to Firebase Storage
            val combinedDateTime = "$currentDate $currentTime"
            firebaseManager.uploadScanedImage(imageUri, uid, userType, combinedDateTime) { imageUrl ->
                if (imageUrl != null) {
                    Log.d("ScanResultsActivity", "Image uploaded successfully: $imageUrl")
                } else {
                    Log.e("ScanResultsActivity", "Error uploading image")
                }
            }

        } else {
            Log.e("ScanResultsActivity", "Invalid user data")
        }
    }



    // Function to get the path of the image from the URI
    private fun getPathFromUri(uri: Uri): String? {
        var path: String? = null
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex =
                    it.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
                path = it.getString(columnIndex)
            }
        }
        return path
    }


    // Function to get the scan results from the API
    private fun getScanResults(imageUri: Uri) {
        // get url from strings
        val url = getString(R.string.ImageModel)
        Toast.makeText(this, url, Toast.LENGTH_SHORT).show()
        val client = OkHttpClient()

        val imagePath = getPathFromUri(imageUri)
        if (imagePath == null) {
            Log.e("UploadImage", "Unable to resolve file path from URI: $imageUri")
            return
        }

        Log.d("UploadImage", "Resolved file path: $imagePath")

        val file = File(imagePath)
        val mimeType = contentResolver.getType(imageUri) ?: "application/octet-stream"
        Log.d("UploadImage", "Determined MIME type: $mimeType")

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody(mimeType.toMediaTypeOrNull())
            )
            .build()

        Log.d("UploadImage", "Request body built successfully")

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        Log.d("UploadImage", "Request created: ${request.url}")

        progressDialog = ProgressDialog(this, R.style.CustomProgressDialog)
        progressDialog.setMessage("Scanning...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val window = progressDialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),  // 90% of screen width
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UploadImage", "Request failed: ${e.message}", e)
                runOnUiThread {
                    progressDialog.dismiss()
                    firstDName.text = "---"
                    secondDName.text = "---"

                    firstDPercentage.text = "-- %"
                    secondDPercentage.text = "-- %"

                    Toast.makeText(this@ScanResultsActivity, "Connection Error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("UploadImage", "Response received: ${response.code}")
                runOnUiThread {
                    progressDialog.dismiss()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("UploadImage", "Response body: $responseBody")

                    if (responseBody != null) {
                        try {
                            val apiResponse = Gson().fromJson(responseBody, ApiResponse::class.java)
                            val sortedProbabilities = apiResponse.probabilities.entries.sortedByDescending { it.value }
                            runOnUiThread {
                                if (sortedProbabilities.isNotEmpty()) {
                                    Log.d("UploadImage", "First disease: ${sortedProbabilities[0].key} - ${sortedProbabilities[0].value}")
                                    firstDName.text = sortedProbabilities[0].key
                                    firstDPercentage.text = "${(sortedProbabilities[0].value * 100).toInt()}%"
                                }
                                if (sortedProbabilities.size > 1) {
                                    Log.d("UploadImage", "Second disease: ${sortedProbabilities[1].key} - ${sortedProbabilities[1].value}")
                                    secondDName.text = sortedProbabilities[1].key
                                    secondDPercentage.text = "${(sortedProbabilities[1].value * 100).toInt()}%"
                                }

                                // Upload the data to Firebase
                                if(!isDataUploaded) {

                                    fetchUserDataAndUpload()
                                    isDataUploaded = true
                                }

                            }
                        } catch (e: Exception) {
                            Log.e("UploadImage", "Error parsing response JSON: ${e.message}", e)
                        }
                    }
                } else {
                    Log.e("UploadImage", "Unsuccessful response: ${response.code} - ${response.message}")
                    runOnUiThread {
                        // Handle unsuccessful response
                    }
                }
            }
        })
    }
}